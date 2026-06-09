package com.lumen.player.ui.subtitles

import android.net.Uri

/**
 * A subtitle file discovered on local storage that matches the currently
 * playing video by name or by `ShowName.S01E01` pattern.
 */
data class LocalSubtitle(
    val uri: Uri,
    val filePath: String,
    val mimeType: String,
    val language: String,
    val fileName: String
)
