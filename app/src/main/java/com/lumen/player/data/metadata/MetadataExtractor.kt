package com.lumen.player.data.metadata

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Best-effort tag extraction for files that MediaStore didn't index well —
 * direct URI imports, network streams, etc.
 */
class MetadataExtractor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    data class Tags(
        val title: String? = null,
        val artist: String? = null,
        val album: String? = null,
        val durationMs: Long = 0L,
        val mimeType: String? = null,
        val hasVideo: Boolean = false
    )

    suspend fun extract(uri: Uri): Tags = withContext(Dispatchers.IO) {
        runCatching {
            MediaMetadataRetriever().use { mmr ->
                mmr.setDataSource(context, uri)
                Tags(
                    title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE),
                    artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST),
                    album = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM),
                    durationMs = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                        ?.toLongOrNull() ?: 0L,
                    mimeType = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE),
                    hasVideo = mmr.extractMetadata(
                        MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO
                    ) == "yes"
                )
            }
        }.getOrDefault(Tags())
    }
}

private inline fun <R> MediaMetadataRetriever.use(block: (MediaMetadataRetriever) -> R): R {
    try {
        return block(this)
    } finally {
        try { release() } catch (_: Throwable) {}
    }
}
