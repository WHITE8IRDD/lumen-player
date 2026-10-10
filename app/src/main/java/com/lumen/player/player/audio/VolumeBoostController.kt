package com.lumen.player.player.audio

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Shared software volume boost for both the music and video players.
 *
 * Levels use the same scale as the UI:
 *  - `1.0f` = 100 % (normal, no boost)
 *  - up to [MAX_LEVEL] = 200 % (VLC-style boost past the device maximum)
 *
 * The gain is applied to the decoded PCM samples by [VolumeBoostProcessor],
 * so it works even when the system media volume is already at its maximum.
 * Samples that exceed full scale are clipped, so very high levels can add
 * audible distortion, which is the same trade-off VLC makes.
 *
 * The boost is kept in memory only, so it resets to 100 % when the app
 * process restarts.
 */
@Singleton
class VolumeBoostController @Inject constructor() {

    @Volatile
    var gain: Float = MIN_LEVEL
        private set

    /** Sets the boost gain. Values are clamped to [MIN_LEVEL]..[MAX_LEVEL]. */
    fun setGain(value: Float) {
        gain = value.coerceIn(MIN_LEVEL, MAX_LEVEL)
    }

    companion object {
        /** 100 %: no boost. */
        const val MIN_LEVEL = 1f

        /** 200 %: the largest boost the app offers. */
        const val MAX_LEVEL = 2f
    }
}
