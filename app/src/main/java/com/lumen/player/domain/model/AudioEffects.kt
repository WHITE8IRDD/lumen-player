package com.lumen.player.domain.model

/** Snapshot of audio effect settings — what we persist & restore. */
data class AudioEffectsState(
    val enabled: Boolean = false,
    val preset: Int = 0,
    val bandLevels: List<Short> = emptyList(),
    val bassBoost: Int = 0,
    val virtualizer: Int = 0,
    val loudnessGainMb: Int = 0
)
