package com.lumen.player.ui.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import androidx.media3.common.util.UnstableApi
import coil.ImageLoader
import coil.decode.DataSource
import coil.decode.ImageSource
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.fetch.SourceResult
import coil.request.Options
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import okio.Buffer
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.concurrent.Callable
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

@UnstableApi
data class FolderThumbnail(
    val folderPath: String,
    val folderId: Long = 0L,
    val firstVideoUri: Uri,
    val timeUs: Long = 1_000_000L
)

@UnstableApi
class FolderThumbnailFetcher(
    private val data: FolderThumbnail,
    private val options: Options
) : Fetcher {

    override suspend fun fetch(): FetchResult {
        val ctx = options.context
        val cacheFile = FolderThumbnailCache.cacheFileFor(ctx, data.folderPath, data.folderId, data.firstVideoUri)

        val bitmap: Bitmap = withContext(Dispatchers.IO) {
            if (cacheFile != null && cacheFile.exists() && cacheFile.length() > 0L) {
                val cached = BitmapFactory.decodeFile(cacheFile.absolutePath)
                if (cached != null) return@withContext cached
            }
            val frame = extractWithRetry(ctx, data.firstVideoUri)
            if (frame != null && cacheFile != null) {
                FolderThumbnailCache.writeCache(cacheFile, frame)
            }
            frame ?: FolderThumbnailCache.fallbackBitmap()
        }

        val out = java.io.ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 82, out)
        bitmap.recycle()
        val bytes = out.toByteArray()

        return SourceResult(
            source = ImageSource(Buffer().write(bytes), ctx),
            mimeType = "image/jpeg",
            dataSource = DataSource.DISK
        )
    }

    private suspend fun extractWithRetry(ctx: Context, uri: Uri): Bitmap? = withContext(Dispatchers.IO) {
        runCatching {
            withTimeout(8_000L) {
                extractExecutor.submit(
                    Callable { FolderThumbnailExtractor.extractWithFallback(ctx, uri) }
                ).get(8L, TimeUnit.SECONDS)
            }
        }.getOrNull()
    }

    class Factory : Fetcher.Factory<FolderThumbnail> {
        override fun create(
            data: FolderThumbnail,
            options: Options,
            imageLoader: ImageLoader
        ): Fetcher = FolderThumbnailFetcher(data, options)
    }
}

private val extractExecutor: ThreadPoolExecutor = ThreadPoolExecutor(
    4, 4, 30L, TimeUnit.SECONDS,
    LinkedBlockingQueue(64),
    { r ->
        Thread(r, "lumen-folder-thumb-${THREAD_ID.incrementAndGet()}").apply {
            priority = Thread.NORM_PRIORITY - 1
            isDaemon = true
        }
    }
).also { it.allowCoreThreadTimeOut(false) }

private val THREAD_ID = AtomicInteger(0)

private object FolderThumbnailCache {
    private const val TAG = "FolderThumbCache"
    private const val SUBDIR = "lumen_folder_thumb_cache"

    private fun dir(context: Context): File =
        File(context.cacheDir, SUBDIR).apply { mkdirs() }

    fun cacheFileFor(context: Context, folderPath: String, folderId: Long, firstVideoUri: Uri): File? {
        val mtime = try {
            val path = resolveFilePath(context, firstVideoUri) ?: return null
            File(path).lastModified()
        } catch (_: Exception) { 0L }
        val key = sha256("$folderId|$mtime")
        return File(dir(context), "$key.jpg")
    }

    fun writeCache(file: File, bitmap: Bitmap) {
        runCatching {
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 82, out)
            }
        }.onFailure { Log.w(TAG, "writeCache failed: ${it.message}") }
    }

    fun fallbackBitmap(): Bitmap =
        Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888).apply {
            eraseColor(android.graphics.Color.rgb(30, 30, 42))
        }

    private fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun resolveFilePath(context: Context, uri: Uri): String? {
        if (uri.scheme == "file") return uri.path
        return try {
            context.contentResolver.query(
                uri,
                arrayOf(android.provider.MediaStore.Video.VideoColumns.DATA),
                null, null, null
            )?.use { c -> if (c.moveToFirst()) c.getString(0) else null }
        } catch (t: Throwable) {
            Log.w(TAG, "resolveFilePath failed: ${t.message}")
            null
        }
    }
}

private object FolderThumbnailExtractor {

    fun extractWithFallback(context: Context, uri: Uri): Bitmap? {
        // Strategy 1: OS-cached thumbnail
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                val cached = context.contentResolver.loadThumbnail(uri, Size(512, 288), null)
                if (cached != null) return cached
            } catch (_: Exception) { }
        }

        // Strategy 2: MediaMetadataRetriever with multi-timestamp
        val timestamps = listOf(1_000_000L, 5_000_000L, 30_000_000L, 0L)
        for (ts in timestamps) {
            val frame = extractFrame(context, uri, ts)
            if (frame != null) return frame
        }
        return null
    }

    private fun extractFrame(context: Context, uri: Uri, timeUs: Long): Bitmap? {
        runCatching {
            val retriever = MediaMetadataRetriever()
            try {
                val ok1 = runCatching { retriever.setDataSource(context, uri) }.isSuccess
                var frame: Bitmap? = if (ok1) getFrameBestEffort(retriever, timeUs) else null
                if (frame != null) return frame

                runCatching { retriever.release() }
                val retriever2 = MediaMetadataRetriever()
                return try {
                    val path = resolveFilePath(context, uri)
                    if (path != null) {
                        val ok2 = runCatching { retriever2.setDataSource(path) }.isSuccess
                        if (ok2) getFrameBestEffort(retriever2, timeUs) else null
                    } else null
                } finally {
                    runCatching { retriever2.release() }
                }
            } catch (_: Exception) {
                runCatching { retriever.release() }
            }
        }
        return null
    }

    private fun getFrameBestEffort(retriever: MediaMetadataRetriever, timeUs: Long): Bitmap? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            val frame = retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST)
            if (frame != null) return frame
        } else {
            @Suppress("DEPRECATION")
            val frame = retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
            if (frame != null) return frame
        }
        @Suppress("DEPRECATION")
        return retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
    }

    private fun resolveFilePath(context: Context, uri: Uri): String? {
        if (uri.scheme == "file") return uri.path
        return try {
            context.contentResolver.query(
                uri,
                arrayOf(android.provider.MediaStore.Video.VideoColumns.DATA),
                null, null, null
            )?.use { c -> if (c.moveToFirst()) c.getString(0) else null }
        } catch (_: Exception) { null }
    }
}

object FolderThumbnailJobRegistry {
    private val jobs = ConcurrentHashMap<String, Job>()
    private val scopes = ConcurrentHashMap<String, CoroutineScope>()

    fun start(folderPath: String, action: suspend () -> Unit) {
        cancel(folderPath)
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
        val job = scope.launch { action() }
        scopes[folderPath] = scope
        jobs[folderPath] = job
        job.invokeOnCompletion { remove(folderPath) }
    }

    fun cancel(folderPath: String) {
        jobs.remove(folderPath)?.cancel()
        scopes.remove(folderPath)?.cancel()
    }

    private fun remove(folderPath: String) {
        jobs.remove(folderPath)
        scopes.remove(folderPath)
    }
}
