package com.lumen.player.domain.model

data class SubtitleResult(
    val releaseName: String?,
    val language: String?,
    val rating: String?,
    val downloadCount: Int?,
    val uploadDate: String?,
    val format: String,
    val downloadUrl: String,
    val isZip: Boolean,
    val source: String,
    val isHashMatch: Boolean = false,
    val fileName: String? = null,
    val author: String? = null,
    val isHearingImpaired: Boolean = false
)
