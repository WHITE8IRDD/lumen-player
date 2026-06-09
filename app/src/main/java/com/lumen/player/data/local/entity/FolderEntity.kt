package com.lumen.player.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** User-defined or auto-discovered folder excluded/included from scans. */
@Entity(tableName = "folder_settings")
data class FolderEntity(
    @PrimaryKey val path: String,
    val isExcluded: Boolean = false,
    val displayName: String? = null
)
