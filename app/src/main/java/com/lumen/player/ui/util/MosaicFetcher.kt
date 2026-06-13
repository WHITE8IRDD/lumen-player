package com.lumen.player.ui.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
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
import okio.Buffer
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest

private const val TAG = "MosaicFetcher"

data class MosaicFetcherInput(
    val folderPath: String,
    val videoIds: List<Long>,
    val uris: List<Uri>,
    val timeUs: Long = 1_000_000L
)

@UnstableApi
class MosaicFetcher(
    private val data: MosaicFetcherInput,
    private val options: Options
) : Fetcher {

    override suspend fun fetch(): FetchResult {
        val ctx = options.context
        return try {
            val cacheFile = cacheFileFor(ctx, data.folderPath, data.videoIds)

            val bitmap: Bitmap = withContext(Dispatchers.IO) {
                if (cacheFile != null && cacheFile.exists() && cacheFile.length() > 0L) {
                    val cached = BitmapFactory.decodeFile(cacheFile.absolutePath)
                    if (cached != null) {
                        Log.d(TAG, "Cache HIT for ${data.folderPath}")
                        return@withContext cached
                    }
                }
                Log.d(TAG, "Cache MISS for ${data.folderPath}, composing mosaic")
                val frames = data.uris.take(4).map { uri ->
                    ThumbnailExtractor.extractWithFallback(ctx, uri) ?: ThumbnailExtractor.fallbackBitmap()
                }
                val composed = MosaicComposer.compose(frames, 512)
                if (cacheFile != null) {
                    runCatching {
                        FileOutputStream(cacheFile).use { out ->
                            composed.compress(Bitmap.CompressFormat.JPEG, 85, out)
                        }
                    }
                }
                frames.forEachIndexed { i, bmp ->
                    if (i > 0 && bmp.width > 1) bmp.recycle()
                }
                composed
            }

            val out = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
            bitmap.recycle()
            val bytes = out.toByteArray()

            SourceResult(
                source = ImageSource(Buffer().write(bytes), ctx),
                mimeType = "image/jpeg",
                dataSource = DataSource.DISK
            )
        } catch (e: Exception) {
            Log.w(TAG, "mosaic fetch failed: ${e.message}")
            val fallback = ThumbnailExtractor.fallbackBitmap()
            val out = ByteArrayOutputStream()
            fallback.compress(Bitmap.CompressFormat.JPEG, 85, out)
            fallback.recycle()
            SourceResult(
                source = ImageSource(Buffer().write(out.toByteArray()), ctx),
                mimeType = "image/jpeg",
                dataSource = DataSource.DISK
            )
        }
    }

    class Factory : Fetcher.Factory<MosaicFetcherInput> {
        override fun create(
            data: MosaicFetcherInput,
            options: Options,
            imageLoader: ImageLoader
        ): Fetcher = MosaicFetcher(data, options)
    }
}

private const val CACHE_SUBDIR = "lumen_mosaic_cache"

private fun cacheDir(context: Context): File =
    File(context.cacheDir, CACHE_SUBDIR).apply { mkdirs() }

private fun cacheFileFor(context: Context, folderPath: String, videoIds: List<Long>): File? {
    val key = sha256("$folderPath|${videoIds.joinToString("_")}")
    return File(cacheDir(context), "$key.jpg")
}

private fun sha256(input: String): String {
    val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
    return bytes.joinToString("") { "%02x".format(it) }
}
