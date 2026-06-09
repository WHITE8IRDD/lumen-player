package com.lumen.player.domain.model

data class MediaMetadata(
    val cleanTitle: String = "",
    val year: Int? = null,
    val seasonEpisode: String? = null,
    val posterUrl: String? = null,
    val overview: String? = null,
    val backdropUrl: String? = null
)
