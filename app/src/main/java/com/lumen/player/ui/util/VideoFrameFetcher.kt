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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import okio.Buffer
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.concurrent.Callable
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

@UnstableApi
data class VideoThumbnail(
    val id: Long,
    val uri: Uri,
    val timeUs: Long = 1_000_000L
)

@UnstableApi
class VideoFrameFetcher(
    private val data: VideoThumbnail,
    private val options: Options
) : Fetcher {

    override suspend fun fetch(): FetchResult {
        val ctx = options.context
        return try {
            val cacheFile = ThumbnailExtractor.cacheFileFor(ctx, data.id, data.uri, data.timeUs)
            val bitmap: Bitmap = withContext(Dispatchers.IO) {
                if (cacheFile != null && cacheFile.exists() && cacheFile.length() > 0L) {
                    val cached = BitmapFactory.decodeFile(cacheFile.absolutePath)
                    if (cached != null) return@withContext cached
                }
                val frame = extractWithRetry(ctx, data.uri)
                if (frame != null && cacheFile != null) {
                    ThumbnailExtractor.writeCache(cacheFile, frame)
                }
                frame ?: ThumbnailExtractor.fallbackBitmap()
            }

            val out = java.io.ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
            bitmap.recycle()
            val bytes = out.toByteArray()

            SourceResult(
                source = ImageSource(Buffer().write(bytes), ctx),
                mimeType = "image/jpeg",
                dataSource = DataSource.DISK
            )
        } catch (e: Exception) {
            Log.w("VideoFrameFetcher", "fetch failed, using fallback: ${e.message}")
            val fallback = ThumbnailExtractor.fallbackBitmap()
            val out = java.io.ByteArrayOutputStream()
            fallback.compress(Bitmap.CompressFormat.JPEG, 85, out)
            fallback.recycle()
            SourceResult(
                source = ImageSource(Buffer().write(out.toByteArray()), ctx),
                mimeType = "image/jpeg",
                dataSource = DataSource.DISK
            )
        }
    }

    private suspend fun extractWithRetry(ctx: Context, uri: Uri): Bitmap? = withContext(Dispatchers.IO) {
        runCatching {
            withTimeout(8_000L) {
                extractExecutor.submit(
                    Callable { ThumbnailExtractor.extractWithFallback(ctx, uri) }
                ).get(8L, TimeUnit.SECONDS)
            }
        }.getOrNull()
    }

    class Factory : Fetcher.Factory<VideoThumbnail> {
        override fun create(
            data: VideoThumbnail,
            options: Options,
            imageLoader: ImageLoader
        ): Fetcher = VideoFrameFetcher(data, options)
    }
}

private val extractExecutor: ThreadPoolExecutor = ThreadPoolExecutor(
    4, 4, 30L, TimeUnit.SECONDS,
    LinkedBlockingQueue(64),
    { r ->
        Thread(r, "lumen-thumb-${THREAD_ID.incrementAndGet()}").apply {
            priority = Thread.NORM_PRIORITY - 1
            isDaemon = true
        }
    }
).also { it.allowCoreThreadTimeOut(false) }

private val THREAD_ID = AtomicInteger(0)

object ThumbnailExtractor {

    private const val TAG = "ThumbnailExtractor"
    private const val CACHE_SUBDIR = "lumen_thumb_cache"

    fun fallbackBitmap(): Bitmap {
        return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888).apply {
            eraseColor(android.graphics.Color.rgb(30, 30, 42))
        }
    }

    fun isMostlyBlack(bitmap: Bitmap, threshold: Float = 0.95f): Boolean {
        val stepX = maxOf(1, bitmap.width / 50)
        val stepY = maxOf(1, bitmap.height / 50)
        var blackPixels = 0
        var totalPixels = 0
        for (x in 0 until bitmap.width step stepX) {
            for (y in 0 until bitmap.height step stepY) {
                val pixel = bitmap.getPixel(x, y)
                if (android.graphics.Color.red(pixel) < 25 &&
                    android.graphics.Color.green(pixel) < 25 &&
                    android.graphics.Color.blue(pixel) < 25
                ) blackPixels++
                totalPixels++
            }
        }
        return totalPixels > 0 && blackPixels.toFloat() / totalPixels >= threshold
    }

    fun extractBestFrame(context: Context, uri: Uri): Bitmap? {
        val timestamps = listOf(1_000_000L, 5_000_000L, 30_000_000L, 60_000_000L, 120_000_000L, 0L)
        val candidates = timestamps.mapNotNull { ts ->
            runCatching { extractFrame(context, uri, ts) }.getOrNull()
        }
        return candidates.firstOrNull { !isMostlyBlack(it) } ?: candidates.firstOrNull()
    }

    private fun cacheDir(context: Context): File =
        File(context.cacheDir, CACHE_SUBDIR).apply { mkdirs() }

    fun cacheFileFor(context: Context, id: Long, uri: Uri, timeUs: Long): File? {
        val modStamp = try {
            val path = resolveFilePath(context, uri)
            if (path != null) File(path).lastModified() else 0L
        } catch (_: Exception) { 0L }
        val key = sha256("$id|$modStamp|$timeUs")
        return File(cacheDir(context), "$key.jpg")
    }

    fun writeCache(file: File, bitmap: Bitmap) {
        runCatching {
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
            }
        }
    }

    private fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Multi-strategy extraction:
     * 1. ContentResolver.loadThumbnail() — OS-cached thumbnail, instant
     * 2. MediaMetadataRetriever with timestamps: 1s → 5s → 30s → mid
     * 3. File-path-based fallback (existing strategy)
     */
    fun extractWithFallback(context: Context, uri: Uri): Bitmap? {
        return runCatching {
            // Strategy 1: OS-cached thumbnail
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                try {
                    val cached = context.contentResolver.loadThumbnail(uri, Size(512, 288), null)
                    if (cached != null && !isMostlyBlack(cached)) return cached
                } catch (_: Exception) {}
            }

            // Strategy 2: extractBestFrame with multi-timestamp + black frame rejection
            extractBestFrame(context, uri)
        }.getOrNull()
    }

    private fun extractFrame(context: Context, uri: Uri, timeUs: Long): Bitmap? {
        runCatching {
            val retriever = MediaMetadataRetriever()
            try {
                var ok = runCatching { retriever.setDataSource(context, uri) }.isSuccess
                var frame: Bitmap? = if (ok) getFrameBestEffort(retriever, timeUs) else null
                if (frame != null) return frame

                runCatching { retriever.release() }

                val retriever2 = MediaMetadataRetriever()
                try {
                    val path = resolveFilePath(context, uri)
                    if (path != null) {
                        ok = runCatching { retriever2.setDataSource(path) }.isSuccess
                        if (ok) return getFrameBestEffort(retriever2, timeUs)
                    }
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
        // Try OPTION_CLOSEST first (less strict sync)
        val apiLevel = Build.VERSION.SDK_INT
        if (apiLevel >= Build.VERSION_CODES.O_MR1) {
            val frame = retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST)
            if (frame != null) return frame
        } else {
            @Suppress("DEPRECATION")
            val frame = retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
            if (frame != null) return frame
        }
        // Fallback to OPTION_CLOSEST_SYNC
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
            )?.use { c ->
                if (c.moveToFirst()) c.getString(0) else null
            }
        } catch (t: Throwable) {
            Log.w(TAG, "resolveFilePath failed: ${t.message}")
            null
        }
    }
}
