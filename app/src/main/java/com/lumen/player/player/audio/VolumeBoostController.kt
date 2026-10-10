package com.lumen.player.player.audio

import android.os.Build
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.pow

/**
 * Shared volume boost for both the music and video players. It follows VLC for
 * Android (libvlc `aout` volume code):
 *
 *  - [level] is the UI scale: `1.0` = 100 %, up to [maxBoost] (120 %..200 %,
 *    chosen in Settings > Volume). The volume bar and percentage use this value.
 *  - Above 100 % the gain is the level cubed ([levelToGain]). VLC does this, so
 *    200 % is about +18 dB. A plain 2x (+6 dB) is much quieter than VLC at the top.
 *  - The gain is applied in one of two ways, as in VLC:
 *      1. Android 12+: a [DeviceBoostEffect] (DynamicsProcessing input gain)
 *         on the player's audio session. This is used when it has control.
 *      2. Otherwise: software gain in [VolumeBoostProcessor], which runs in the
 *         ExoPlayer audio pipeline.
 *    The software gain is 1.0 while the device effect is active, so the boost is
 *    never applied twice.
 *
 * While the boost is off, [maxLevel] is 1.0, so every volume control behaves
 * exactly as before. The level is kept in memory only.
 * [com.lumen.player.LumenApplication] feeds the saved settings into [applySettings].
 */
@Singleton
class VolumeBoostController @Inject constructor() {

    /** What the audio processor reports about the track that is playing. */
    enum class EngineState {
        /** No audio has reached the processor yet. */
        IDLE,
        /** The processor is in the audio path for the current track. */
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

    /** Requested level on the UI scale (1.0 = 100 %). */
    @Volatile
    var level: Float = MIN_LEVEL
        private set

    /** Gain for [level] after the VLC curve. 1.0 when there is no boost. */
    @Volatile
    var linearGain: Float = MIN_LEVEL
        private set

    /** True while the device audio effect is carrying the boost. */
    @Volatile
    private var deviceActive: Boolean = false

    private val _deviceEffectActive = MutableStateFlow(false)
    val deviceEffectActive: StateFlow<Boolean> = _deviceEffectActive.asStateFlow()

    /** Gain the software processor must apply. 1.0 while the device effect is active. */
    val softwareGain: Float
        get() = if (deviceActive) MIN_LEVEL else linearGain

    /** Highest level any volume control may reach. 1.0 when the boost is off. */
    val maxLevel: Float
        get() = if (enabled) maxBoost else MIN_LEVEL

    private val _engineState = MutableStateFlow(EngineState.IDLE)
    val engineState: StateFlow<EngineState> = _engineState.asStateFlow()

    private val _formatName = MutableStateFlow("")
    val formatName: StateFlow<String> = _formatName.asStateFlow()

    private var deviceEffect: DeviceBoostEffect? = null

    /** Applies the saved Settings. Lowering the cap also lowers the current level. */
    @Synchronized
    fun applySettings(enabled: Boolean, maxPercent: Int) {
        this.enabled = enabled
        maxBoost = (maxPercent / 100f).coerceIn(MIN_LEVEL, MAX_LEVEL)
        level = level.coerceIn(MIN_LEVEL, maxLevel)
        refreshGain()
    }

    /** Sets the level. Values are clamped to [MIN_LEVEL]..[maxLevel]. */
    @Synchronized
    fun setGain(value: Float) {
        level = value.coerceIn(MIN_LEVEL, maxLevel)
        refreshGain()
    }

    /**
     * Attaches the device effect to the player's audio session. Call this when the
     * player is created. It is a no-op below Android 12, where the software gain is used.
     */
    @Synchronized
    fun attachSession(sessionId: Int) {
        releaseDeviceEffect()
        if (sessionId != 0 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            deviceEffect = runCatching { DeviceBoostEffect(sessionId) }.getOrNull()
        }
        refreshGain()
    }

    /** Releases the device effect, for example when the player is destroyed. */
    @Synchronized
    fun detachSession() {
        releaseDeviceEffect()
        refreshGain()
    }

    /** Called by [VolumeBoostProcessor] whenever it is configured for a track. */
    fun reportFormat(formatName: String, supported: Boolean) {
        _formatName.value = formatName
        _engineState.value = if (supported) EngineState.ACTIVE else EngineState.UNSUPPORTED
    }

    private fun refreshGain() {
        linearGain = levelToGain(level)
        val accepted = deviceEffect?.setGain(linearGain) ?: false
        deviceActive = accepted && linearGain > MIN_LEVEL
        _deviceEffectActive.value = deviceActive
    }

    private fun releaseDeviceEffect() {
        deviceEffect?.release()
        deviceEffect = null
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

        /**
         * Maps a UI level to the audio gain, the way VLC does above 100 %:
         * the gain is the level cubed. At or below 100 % there is no boost.
         */
        fun levelToGain(level: Float): Float =
            if (level <= MIN_LEVEL) MIN_LEVEL else level.pow(3)
    }
}
