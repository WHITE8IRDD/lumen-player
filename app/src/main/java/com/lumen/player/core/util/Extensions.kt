package com.lumen.player.core.util

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.MimeTypes
import com.lumen.player.domain.model.MediaItem as DomainMediaItem

private fun inferMimeType(url: String?): String? {
    val u = url?.lowercase() ?: return null
    return when {
        u.contains(".m3u8") || u.contains(".m3u8?") -> MimeTypes.APPLICATION_M3U8
        u.contains(".mpd") -> MimeTypes.APPLICATION_MPD
        u.contains(".ts") || u.contains(".ts?") -> "video/mp2t"
        u.contains(".mp4") || u.contains(".mp4?") -> MimeTypes.VIDEO_MP4
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
fun DomainMediaItem.toMedia3Item(): MediaItem {
    val resolvedMime = if (mimeType.isNullOrBlank() || mimeType == "video/unknown" || mimeType == "audio/unknown") {
        inferMimeType(uri.toString()) ?: mimeType
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
    Log.d("toMedia3Item", "url=${uri} mimeType=$mimeType resolved=$resolvedMime")
    return MediaItem.Builder()
        .setMediaId(id.toString())
        .setUri(uri)
        .setMediaMetadata(metadata)
        .apply { if (resolvedMime != null) setMimeType(resolvedMime) }
        .build()
}

fun List<DomainMediaItem>.toMedia3Items(): List<MediaItem> = map { it.toMedia3Item() }

/** Safe string -> Uri */
fun String?.toUriOrNull(): Uri? = this?.takeIf { it.isNotBlank() }?.let { Uri.parse(it) }

/** Returns app version name (or "" if not retrievable). */
fun Context.versionName(): String = runCatching {
    packageManager.getPackageInfo(packageName, 0).versionName.orEmpty()
}.getOrDefault("")
