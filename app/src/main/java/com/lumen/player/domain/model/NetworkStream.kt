package com.lumen.player.domain.model

data class NetworkStream(
    val id: Long,
    val url: String,
    val title: String,
    val playlistName: String? = null,
    val dateAdded: Long,
    val lastPositionMs: Long = 0L,
    val isFavorite: Boolean = false,
    val category: String? = null,
    val logoUrl: String? = null
)
