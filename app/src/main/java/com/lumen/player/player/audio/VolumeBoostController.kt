package com.lumen.player.player.audio

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Shared software volume boost for both the music and video players.
 *
 * Levels use the same scale as the UI:
 *  - `1.0f` = 100 % (normal, no boost)
 *  - up to [maxBoost] (100 %..200 %, chosen in Settings > Volume) = VLC-style boost
 *    past the device maximum
 *
 * The boost is off unless the user enables it in Settings. While it is off,
 * [maxLevel] is 1.0, so every volume control behaves exactly as before.
 *
 * The gain is applied to the decoded PCM samples by [VolumeBoostProcessor],
 * so it works even when the system media volume is already at its maximum.
 * Samples that exceed full scale are clipped, so very high levels can add
 * audible distortion, which is the same trade-off VLC makes.
 *
 * [engineState] reports whether the processor is actually in the audio path
 * for the current track, so Settings can tell the user when the boost is not
 * being applied (for example, for an audio encoding the processor cannot handle).
 *
 * The gain is kept in memory only. [com.lumen.player.LumenApplication] feeds
 * the saved settings into [applySettings].
 */
@Singleton
class VolumeBoostController @Inject constructor() {

    /** What the audio processor reports about the track that is playing. */
    enum class EngineState {
        /** No audio has reached the processor yet. */
        IDLE,
        /** The processor is applying the gain to the current track. */
        ACTIVE,
        /** The current track uses an encoding the processor cannot change. */
        UNSUPPORTED
    }

    @Volatile
    var enabled: Boolean = false
        private set

    /** Highest boost the user allowed in Settings, between [MIN_LEVEL] and [MAX_LEVEL]. */
    @Volatile
    var maxBoost: Float = MAX_LEVEL
        private set

    @Volatile
    var gain: Float = MIN_LEVEL
        private set

    /** Highest level any volume control may reach. 1.0 when the boost is off. */
    val maxLevel: Float
        get() = if (enabled) maxBoost else MIN_LEVEL

    private val _engineState = MutableStateFlow(EngineState.IDLE)
    val engineState: StateFlow<EngineState> = _engineState.asStateFlow()

    private val _formatName = MutableStateFlow("")
    val formatName: StateFlow<String> = _formatName.asStateFlow()

    /** Applies the saved Settings. Lowering the cap also lowers the current gain. */
    fun applySettings(enabled: Boolean, maxPercent: Int) {
        this.enabled = enabled
        maxBoost = (maxPercent / 100f).coerceIn(MIN_LEVEL, MAX_LEVEL)
        gain = gain.coerceIn(MIN_LEVEL, maxLevel)
    }

    /** Sets the boost gain. Values are clamped to [MIN_LEVEL]..[maxLevel]. */
    fun setGain(value: Float) {
        gain = value.coerceIn(MIN_LEVEL, maxLevel)
    }

    /** Called by [VolumeBoostProcessor] whenever it is configured for a track. */
    fun reportFormat(formatName: String, supported: Boolean) {
        _formatName.value = formatName
        _engineState.value = if (supported) EngineState.ACTIVE else EngineState.UNSUPPORTED
    }

    companion object {
        /** 100 %: no boost. */
        const val MIN_LEVEL = 1f

        /** 200 %: the largest boost the app offers. */
        const val MAX_LEVEL = 2f

        /** Default cap for the boost, in percent. */
        const val DEFAULT_MAX_PERCENT = 200

        /** Caps offered in Settings, in percent. */
        val MAX_PERCENT_OPTIONS = listOf(120, 150, 175, 200)
    }
}
