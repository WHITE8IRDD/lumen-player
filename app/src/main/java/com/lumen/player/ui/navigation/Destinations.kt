package com.lumen.player.ui.navigation

object Destinations {
    const val HOME = "home"
    const val LIBRARY = "library"
    const val SETTINGS = "settings"

    const val FOLDER_DETAIL = "folder/{path}"
    fun folderDetail(path: String) = "folder/${java.net.URLEncoder.encode(path, "utf-8")}"

    const val PLAYER = "player"
    const val VIDEO = "video"
    const val EQUALIZER = "equalizer"
    const val QUEUE = "queue"

    const val PLAYLIST_DETAIL = "playlist/{id}"
    fun playlistDetail(id: Long) = "playlist/$id"

    const val HIDDEN_ITEMS = "hidden_items"
}
