package com.lumen.player.player.equalizer

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.media.audiofx.Virtualizer
import com.lumen.player.domain.model.AudioEffectsState
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Thin wrapper around the platform [AudioEffect] APIs. The service attaches
 * effects to the ExoPlayer's [audioSessionId] when playback starts.
 */
@Singleton
@Suppress("DEPRECATION")
class EqualizerController @Inject constructor() {

    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    private var loudness: LoudnessEnhancer? = null

    // Cached strengths so setEnabled(false) can re-enable each effect at
    // exactly the same level when the user flips the master switch back on.
    private var bbRmsStrength: Int = 0
    private var virtRmsStrength: Int = 0
    private var loudRmsGain: Int = 0

    val bandCount: Int get() = equalizer?.numberOfBands?.toInt() ?: 0

    val bandLevelRange: Pair<Short, Short>
        get() = equalizer?.bandLevelRange?.let { it[0] to it[1] }
            ?: ((-1500).toShort() to 1500.toShort())

    @Suppress("DEPRECATION")
    fun attach(audioSessionId: Int) {
        if (audioSessionId == 0) return
        release()
        runCatching {
            equalizer = Equalizer(0, audioSessionId).apply { enabled = false }
            bassBoost = BassBoost(0, audioSessionId).apply { enabled = false }
            virtualizer = Virtualizer(0, audioSessionId).apply { enabled = false }
            loudness = LoudnessEnhancer(audioSessionId)
        }
    }

    fun centerFreqHz(band: Int): Int =
        equalizer?.getCenterFreq(band.toShort())?.div(1000) ?: 0

    fun setBandLevel(band: Int, millibels: Short) {
        equalizer?.setBandLevel(band.toShort(), millibels)
    }

    fun getBandLevel(band: Int): Short =
        equalizer?.getBandLevel(band.toShort()) ?: 0

    fun presets(): List<String> {
        val eq = equalizer ?: return emptyList()
        return (0 until eq.numberOfPresets).map { eq.getPresetName(it.toShort()) }
    }

    fun usePreset(index: Int) { equalizer?.usePreset(index.toShort()) }

    fun setBassBoost(strength: Int) {
        bbRmsStrength = strength
        bassBoost?.apply {
            setStrength(strength.toShort())
            enabled = strength > 0
        }
    }

    @Suppress("DEPRECATION")
    fun setVirtualizer(strength: Int) {
        virtRmsStrength = strength
        virtualizer?.apply {
            setStrength(strength.toShort())
            enabled = strength > 0
        }
    }

    fun setLoudnessGainMb(mB: Int) {
        loudRmsGain = mB
        loudness?.apply {
            setTargetGain(mB)
            enabled = mB > 0
        }
    }

    /**
     * Master switch for the entire audio effect chain. When [enabled] is
     * `false` every attached effect is released from the signal — not just
     * the equalizer, but also the bass boost, virtualizer and loudness
     * enhancer. When the user re-enables we restore the previously-set
     * strengths on the equalizer + bass/virt/loudness, so toggling is
     * seamless and reversible.
     */
    fun setEnabled(enabled: Boolean) {
        val eq = equalizer
        val bb = bassBoost
        val vr = virtualizer
        val le = loudness
        if (eq != null) {
            runCatching { eq.enabled = enabled }
        }
        if (bb != null) {
            runCatching { bb.enabled = enabled && bbRmsStrength > 0 }
        }
        if (vr != null) {
            runCatching { vr.enabled = enabled && virtRmsStrength > 0 }
        }
        if (le != null) {
            runCatching { le.enabled = enabled && loudRmsGain > 0 }
        }
    }

    fun applyState(state: AudioEffectsState) {
        if (state.bandLevels.isNotEmpty()) {
            state.bandLevels.forEachIndexed { idx, level ->
                equalizer?.setBandLevel(idx.toShort(), level)
            }
        }
        if (state.preset > 0) equalizer?.usePreset(state.preset.toShort())
        setBassBoost(state.bassBoost)
        setVirtualizer(state.virtualizer)
        setLoudnessGainMb(state.loudnessGainMb)
        setEnabled(state.enabled)
    }

    fun currentState(): AudioEffectsState = AudioEffectsState(
        enabled = equalizer?.enabled ?: false,
        preset = 0,
        bandLevels = if (equalizer != null) (0 until bandCount).map { equalizer!!.getBandLevel(it.toShort()) } else emptyList(),
        bassBoost = bbRmsStrength,
        virtualizer = virtRmsStrength,
        loudnessGainMb = loudRmsGain
    )

    fun release() {
        runCatching { equalizer?.release() }
        runCatching { bassBoost?.release() }
        runCatching { virtualizer?.release() }
        runCatching { loudness?.release() }
        equalizer = null
        bassBoost = null
        virtualizer = null
        loudness = null
    }
}
