package com.lumen.player.data.mapper

import android.net.Uri
import com.lumen.player.data.local.entity.MediaEntity
import com.lumen.player.data.metadata.MediaTitleParser
import com.lumen.player.domain.model.MediaItem
import com.lumen.player.domain.model.MediaType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaMapper @Inject constructor(
    private val titleParser: MediaTitleParser
) {
    suspend fun toDomain(entity: MediaEntity): MediaItem {
        val meta = titleParser.resolve(entity.title)
        return MediaItem(
            id = entity.id,
            uri = Uri.parse(entity.uri),
            title = entity.title,
            cleanTitle = meta.cleanTitle,
            year = meta.year,
            seasonEpisode = meta.seasonEpisode,
            posterUrl = entity.posterUrl,
            artist = entity.artist,
            album = entity.album,
            albumArtUri = entity.albumArtUri?.let { Uri.parse(it) },
            durationMs = entity.durationMs,
            mimeType = entity.mimeType,
            sizeBytes = entity.sizeBytes,
            folderPath = entity.folderPath,
            type = when (entity.type.uppercase()) {
                "VIDEO" -> MediaType.VIDEO
                "PODCAST" -> MediaType.PODCAST
                "AUDIOBOOK" -> MediaType.AUDIOBOOK
                else -> MediaType.AUDIO
            },
            dateAdded = entity.dateAdded,
            trackNumber = entity.trackNumber,
            lastPositionMs = entity.lastPositionMs,
            playCount = entity.playCount,
            isFavorite = entity.isFavorite,
            isVideo = entity.isVideo,
            isHiddenFromHome = entity.isHiddenFromHome,
            isHiddenFromContinueWatching = entity.isHiddenFromContinueWatching
        )
    }

    suspend fun toDomain(entities: List<MediaEntity>): List<MediaItem> {
        return entities.map { toDomain(it) }
    }
}
