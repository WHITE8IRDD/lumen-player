package com.lumen.player.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "media",
    indices = [
        Index("type"),
        Index("isVideo"),
        Index("folderPath"),
        Index("isFavorite")
    ]
)
data class MediaEntity(
    @PrimaryKey val id: Long,
    val uri: String,
    val title: String,
    val artist: String?,
    val album: String?,
    val albumArtUri: String?,
    val durationMs: Long,
    val mimeType: String,
    val sizeBytes: Long,
    val folderPath: String,
    val type: String,
    val dateAdded: Long,
    val trackNumber: Int?,
    val lastPositionMs: Long = 0L,
    val playCount: Int = 0,
    val isFavorite: Boolean = false,
    val isExcluded: Boolean = false,
    val isVideo: Boolean = false,
    val lastPlayedAt: Long = 0L,
    val isHiddenFromHome: Boolean = false,
    val isHiddenFromContinueWatching: Boolean = false,
    val posterUrl: String? = null
)
