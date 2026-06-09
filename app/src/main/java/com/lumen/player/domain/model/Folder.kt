package com.lumen.player.domain.model

import android.net.Uri

data class Folder(
    val path: String,
    val displayName: String,
    val mediaCount: Int,
    val firstItemArtUri: String? = null,
    // Extended metadata for the Videos tab (Problem 2: folder grouping).
    val dateAdded: Long = 0L,
    val totalDurationMs: Long = 0L,
    val totalSizeBytes: Long = 0L,
    val firstVideoId: Long? = null,
    val firstVideoUri: Uri? = null,
    val firstVideoTitle: String? = null,
    val firstVideoDurationMs: Long = 0L,
    val firstVideoSizeBytes: Long = 0L,
    val firstVideoProgress: Float = 0f
)
