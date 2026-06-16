package com.lumen.player.core.util

object Constants {
    const val DATABASE_NAME = "lumen.db"
    const val PREFS_NAME = "lumen_settings"

    /** Update tick for the player position slider. */
    const val PLAYER_POSITION_TICK_MS = 200L

    /** Default seek nudge in milliseconds. */
    const val SEEK_INCREMENT_MS = 10_000L

    val AVAILABLE_PLAYBACK_SPEEDS = floatArrayOf(
        0.25f, 0.5f, 0.75f, 1f, 1.25f, 1.5f, 1.75f, 2f, 2.5f, 3f, 4f
    )
}
