package com.lumen.player.ui.util

import android.content.Context
import androidx.media3.common.util.UnstableApi
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import coil.util.DebugLogger
import com.lumen.player.BuildConfig
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * Application-wide Coil [ImageLoader] tuned for the Lumen Player UI.
 *
 * - Large in-memory + on-disk caches so video thumbnails scroll smoothly.
 * - Registers [VideoFrameFetcher] for the [VideoThumbnail] data type so
 *   every `AsyncImage(model = VideoThumbnail(...))` in the app pulls a
 *   real frame from the video file via `MediaMetadataRetriever`.
 * - OkHttp with sensible HTTP timeouts for remote artwork.
 *
 * Cache strategy:
 *  - Memory cache: 25% of available app memory (instant re-scrolls)
 *  - Disk cache: 256 MB JPEG thumbnails keyed by URI + timestamp
 *    (thumbnails survive app restarts and process death)
 *  - Network cache: enabled for remote artwork
 */
@UnstableApi
object LumenImageLoaderFactory {

    fun create(context: Context): ImageLoader {
        val cacheDir = context.cacheDir.resolve("lumen_image_cache")
        return ImageLoader.Builder(context)
            .components {
                // VideoThumbnail is a custom data type — Coil's built-in
                // UriFetcher never sees it, so this fetcher is guaranteed
                // to handle every video thumbnail request.
                add(VideoFrameFetcher.Factory())
                // FolderThumbnail is a custom data type used by the
                // folder-group cards in the Videos tab. Cache key is
                // folder-path + first-file mtime so a folder with many
                // videos always reuses one JPEG.
                add(FolderThumbnailFetcher.Factory())
            }
            .memoryCache {
                MemoryCache.Builder(context)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir)
                    .maxSizeBytes(512L * 1024 * 1024)
                    .build()
            }
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .networkCachePolicy(CachePolicy.ENABLED)
            .respectCacheHeaders(false)
            .crossfade(150)
            .okHttpClient {
                OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .build()
            }
            .apply { if (BuildConfig.DEBUG) logger(DebugLogger()) }
            .build()
    }
}
