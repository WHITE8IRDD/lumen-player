package com.lumen.player.domain.repository

import com.lumen.player.domain.model.MediaMetadata

interface MetadataResolver {
    suspend fun resolve(rawFilename: String): MediaMetadata
}
