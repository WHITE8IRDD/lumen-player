package com.lumen.player.player.audio

import android.media.audiofx.DynamicsProcessing
import androidx.annotation.RequiresApi
import kotlin.math.log10

/**
 * Applies the boost as Android's built-in [DynamicsProcessing] input gain on the
 * player's audio session. This is how VLC for Android amplifies above 100 %:
 * the linear gain is converted to decibels and set on the effect, and the
 * effect is enabled only while the gain is above 1.0.
 *
 * It needs Android 12 (API 31). VLC restricts it to that version because older
 * releases could crash, so older devices use the software gain instead.
 */
@RequiresApi(31)
internal class DeviceBoostEffect(sessionId: Int) {

    private val effect = DynamicsProcessing(sessionId)

    /**
     * Sets the linear gain (1.0 = no change) and enables the effect when it is above 1.0.
     * Returns true only when the effect accepted the change and still has control of it.
     */
    fun setGain(linearGain: Float): Boolean = runCatching {
        val db = if (linearGain <= 0f) -144f else 20f * log10(linearGain)
        effect.setInputGainAllChannelsTo(db)
        effect.setEnabled(linearGain > 1f)
        effect.hasControl()
    }.getOrDefault(false)

    fun release() {
        runCatching {
            effect.setEnabled(false)
            effect.release()
        }
    }
}
