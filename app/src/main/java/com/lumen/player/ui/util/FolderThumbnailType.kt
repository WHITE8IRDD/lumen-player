package com.lumen.player.ui.util

import android.net.Uri

sealed class FolderThumbnailType {
    data class TmdbPoster(val url: String) : FolderThumbnailType()
    data class Mosaic(val videoIds: List<Long>, val thumbnailUris: List<Uri>) : FolderThumbnailType()
    data class Single(val videoUri: Uri) : FolderThumbnailType()
    data object Placeholder : FolderThumbnailType()
}
