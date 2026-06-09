package com.lumen.player.domain.model

data class Playlist(
    val id: Long,
    val name: String,
    val itemCount: Int,
    val createdAt: Long
)
