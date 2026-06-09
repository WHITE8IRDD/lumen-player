package com.lumen.player.core.util

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.lumen.player.domain.model.MediaItem as DomainMediaItem

/** Converts a domain item into a Media3 MediaItem with metadata. */
fun DomainMediaItem.toMedia3Item(): MediaItem {
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
    return MediaItem.Builder()
        .setMediaId(id.toString())
        .setUri(uri)
        .setMediaMetadata(metadata)
        .setMimeType(mimeType)
        .build()
}

fun List<DomainMediaItem>.toMedia3Items(): List<MediaItem> = map { it.toMedia3Item() }

/** Safe string -> Uri */
fun String?.toUriOrNull(): Uri? = this?.takeIf { it.isNotBlank() }?.let { Uri.parse(it) }

/** Returns app version name (or "" if not retrievable). */
fun Context.versionName(): String = runCatching {
    packageManager.getPackageInfo(packageName, 0).versionName.orEmpty()
}.getOrDefault("")
