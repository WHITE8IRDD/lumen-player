package com.lumen.player.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "streams")
data class StreamEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val url: String,
    val title: String,
    val playlistName: String? = null,
    val dateAdded: Long = System.currentTimeMillis(),
    val lastPositionMs: Long = 0L,
    val isFavorite: Boolean = false,
    val category: String? = null,
    val logoUrl: String? = null
)
