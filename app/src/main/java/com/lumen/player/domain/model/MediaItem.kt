package com.lumen.player.domain.model

import android.net.Uri

enum class MediaType { AUDIO, VIDEO, PODCAST, AUDIOBOOK }

data class MediaItem(
    val id: Long,
    val uri: Uri,
    val title: String,
    val cleanTitle: String = "",
    val year: Int? = null,
    val seasonEpisode: String? = null,
    val artist: String?,
    val album: String?,
    val albumArtUri: Uri?,
    val durationMs: Long,
    val mimeType: String,
    val sizeBytes: Long,
    val folderPath: String,
    val type: MediaType,
    val dateAdded: Long,
    val trackNumber: Int? = null,
    val lastPositionMs: Long = 0L,
    val playCount: Int = 0,
    val isFavorite: Boolean = false,
    val isVideo: Boolean = false,
    val isHiddenFromHome: Boolean = false,
    val isHiddenFromContinueWatching: Boolean = false,
    val posterUrl: String? = null
)
