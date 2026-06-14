package com.lumen.player.player

import android.net.Uri
import androidx.media3.common.Player

data class PlaybackState(
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val playbackState: Int = Player.STATE_IDLE,
    val currentItemId: String? = null,
    val title: String = "",
    val artist: String = "",
    val album: String = "",
    val artworkUri: Uri? = null,
    val durationMs: Long = 0L,
    val positionMs: Long = 0L,
    val bufferedMs: Long = 0L,
    val playbackSpeed: Float = 1f,
    val repeatMode: Int = Player.REPEAT_MODE_OFF,
    val shuffle: Boolean = false,
    val isVideo: Boolean = false,
    val hasNext: Boolean = false,
    val hasPrevious: Boolean = false
)
