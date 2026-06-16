package com.lumen.player.core.util

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.MimeTypes
import com.lumen.player.domain.model.MediaItem as DomainMediaItem
import java.io.File

private val CONTENT_SCHEME = "content"

/** Resolve a content:// URI to a file:// URI for faster direct I/O. */
fun Uri.resolveToFileUri(context: Context): Uri {
    if (scheme != CONTENT_SCHEME) return this
    try {
        context.contentResolver.query(this, arrayOf(MediaStore.MediaColumns.DATA),
            null, null, null)?.use { c ->
            if (c.moveToFirst()) {
                val path = c.getString(0)
                if (!path.isNullOrBlank()) return Uri.fromFile(File(path))
            }
        }
    } catch (_: Exception) { }
    return this
}

private fun inferMimeType(url: String?): String? {
    val u = url?.lowercase() ?: return null
    return when {
        u.contains(".m3u8") || u.contains(".m3u8?") -> MimeTypes.APPLICATION_M3U8
        u.contains(".mpd") -> MimeTypes.APPLICATION_MPD
        u.contains(".ts") || u.contains(".ts?") -> "video/mp2t"
        u.contains(".mp4") || u.contains(".mp4?") -> MimeTypes.VIDEO_MP4
        u.contains(".mkv") || u.contains(".mkv?") -> "video/x-matroska"
        u.contains(".avi") || u.contains(".avi?") -> "video/avi"
        u.contains(".mov") || u.contains(".mov?") -> "video/quicktime"
        u.contains(".wmv") || u.contains(".wmv?") -> "video/x-ms-wmv"
        u.contains(".flv") || u.contains(".flv?") -> "video/x-flv"
        u.contains(".aac") || u.contains(".aac?") -> "audio/aac"
        u.contains(".mp3") || u.contains(".mp3?") -> "audio/mpeg"
        u.contains(".ogg") || u.contains(".ogg?") -> "audio/ogg"
        u.contains(".flac") || u.contains(".flac?") -> "audio/flac"
        u.contains(".webm") || u.contains(".webm?") -> "video/webm"
        u.contains(".ism") || u.contains(".ism/manifest") -> "application/vnd.ms-sstr+xml"
        u.contains(".m3u") -> MimeTypes.APPLICATION_M3U8
        else -> null
    }
}

@OptIn(UnstableApi::class)
fun DomainMediaItem.toMedia3Item(context: Context? = null): MediaItem {
    val resolvedUri = if (context != null) uri.resolveToFileUri(context) else uri
    val resolvedMime = if (mimeType.isNullOrBlank() || mimeType == "video/unknown" || mimeType == "audio/unknown"
        || mimeType == "video/*" || mimeType == "audio/*") {
        inferMimeType(resolvedUri.toString()) ?: mimeType
    } else mimeType
    val metadata = MediaMetadata.Builder()
        .setTitle(title)
        .setArtist(artist)
        .setAlbumTitle(album)
        .setArtworkUri(albumArtUri)
        .setDurationMs(durationMs)
        .setIsBrowsable(false)
        .setIsPlayable(true)
        .setMediaType(
            if (type == com.lumen.player.domain.model.MediaType.VIDEO)
                MediaMetadata.MEDIA_TYPE_VIDEO
            else MediaMetadata.MEDIA_TYPE_MUSIC
        )
        .build()
    Log.d("toMedia3Item", "url=${resolvedUri} (was=${uri}) mimeType=$mimeType resolved=$resolvedMime")
    return MediaItem.Builder()
        .setMediaId(id.toString())
        .setUri(resolvedUri)
        .setMediaMetadata(metadata)
        .apply { if (resolvedMime != null) setMimeType(resolvedMime) }
        .build()
}

fun List<DomainMediaItem>.toMedia3Items(context: Context? = null): List<MediaItem> =
    map { it.toMedia3Item(context) }

/** Safe string -> Uri */
fun String?.toUriOrNull(): Uri? = this?.takeIf { it.isNotBlank() }?.let { Uri.parse(it) }

/** Returns app version name (or "" if not retrievable). */
fun Context.versionName(): String = runCatching {
    packageManager.getPackageInfo(packageName, 0).versionName.orEmpty()
}.getOrDefault("")

object PerfTimer {
    private var startMs: Long = 0

    fun start(label: String) {
        startMs = System.currentTimeMillis()
        android.util.Log.d("SlowStart", "═══ START: $label @ ${startMs}")
    }

    fun mark(label: String) {
        val elapsed = System.currentTimeMillis() - startMs
        android.util.Log.d("SlowStart", "[+${elapsed}ms] $label")
    }

    fun reset() {
        startMs = System.currentTimeMillis()
    }
}
