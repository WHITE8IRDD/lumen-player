package com.lumen.player.ui.util

import android.net.Uri
import com.lumen.player.domain.model.Folder
import com.lumen.player.domain.model.MediaItem

object FolderThumbnailResolver {
    fun resolve(folder: Folder, videos: List<MediaItem>): FolderThumbnailType {
        val sorted = videos.sortedByDescending { it.dateAdded }

        val tmdbMatch = sorted.firstOrNull { it.posterUrl != null }
        if (tmdbMatch != null) return FolderThumbnailType.TmdbPoster(tmdbMatch.posterUrl!!)

        val withUris = sorted.filter { it.uri != Uri.EMPTY }
        if (withUris.size >= 4) {
            val first4 = withUris.take(4)
            return FolderThumbnailType.Mosaic(first4.map { it.id }, first4.map { it.uri })
        }

        val first = withUris.firstOrNull()
        if (first != null) return FolderThumbnailType.Single(first.uri)

        return FolderThumbnailType.Placeholder
    }
}
