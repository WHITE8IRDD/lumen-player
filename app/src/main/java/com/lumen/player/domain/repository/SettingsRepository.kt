package com.lumen.player.domain.repository

import com.lumen.player.domain.model.AudioEffectsState
import kotlinx.coroutines.flow.Flow

enum class ThemeMode { SYSTEM, LIGHT, DARK }

data class UserSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = true,
    val resumePlayback: Boolean = true,
    val respectAudioFocus: Boolean = true,
    val skipSilence: Boolean = false,
    val gaplessPlayback: Boolean = true,
    val showVideos: Boolean = true,
    val hardwareAcceleration: Boolean = true,
    val subtitleDelayUs: Long = 0L,
    val subtitleTextSize: Float = 16f,
    val audioEffects: AudioEffectsState = AudioEffectsState(),
    val fetchOnlineMetadata: Boolean = false,
    val metadataWifiOnly: Boolean = true,
)

interface SettingsRepository {
    val settings: Flow<UserSettings>
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setDynamicColor(enabled: Boolean)
    suspend fun setResumePlayback(enabled: Boolean)
    suspend fun setRespectAudioFocus(enabled: Boolean)
    suspend fun setSkipSilence(enabled: Boolean)
    suspend fun setGaplessPlayback(enabled: Boolean)
    suspend fun setShowVideos(enabled: Boolean)
    suspend fun setHardwareAcceleration(enabled: Boolean)
    suspend fun setSubtitleDelayUs(delayUs: Long)
    suspend fun setSubtitleTextSize(size: Float)
    suspend fun setAudioEffects(state: AudioEffectsState)
    suspend fun setFetchOnlineMetadata(enabled: Boolean)
    suspend fun setMetadataWifiOnly(enabled: Boolean)
}
