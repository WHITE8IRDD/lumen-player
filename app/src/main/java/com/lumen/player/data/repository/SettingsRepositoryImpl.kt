package com.lumen.player.data.repository

import com.lumen.player.data.prefs.SettingsDataStore
import com.lumen.player.domain.model.AudioEffectsState
import com.lumen.player.domain.repository.SettingsRepository
import com.lumen.player.domain.repository.ThemeMode
import com.lumen.player.domain.repository.UserSettings
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val store: SettingsDataStore
) : SettingsRepository {

    override val settings: Flow<UserSettings> = store.flow

    override suspend fun setThemeMode(mode: ThemeMode) = store.setThemeMode(mode)
    override suspend fun setDynamicColor(enabled: Boolean) = store.setDynamicColor(enabled)
    override suspend fun setResumePlayback(enabled: Boolean) = store.setResumePlayback(enabled)
    override suspend fun setRespectAudioFocus(enabled: Boolean) = store.setRespectAudioFocus(enabled)
    override suspend fun setSkipSilence(enabled: Boolean) = store.setSkipSilence(enabled)
    override suspend fun setGaplessPlayback(enabled: Boolean) = store.setGapless(enabled)
    override suspend fun setShowVideos(enabled: Boolean) = store.setShowVideos(enabled)
    override suspend fun setAudioEffects(state: AudioEffectsState) = store.setAudioEffects(state)
    override suspend fun setHardwareAcceleration(enabled: Boolean) = store.setHardwareAcceleration(enabled)
    override suspend fun setSubtitleDelayUs(delayUs: Long) = store.setSubtitleDelayUs(delayUs)
    override suspend fun setSubtitleTextSize(size: Float) = store.setSubtitleTextSize(size)
    override suspend fun setFetchOnlineMetadata(enabled: Boolean) = store.setFetchOnlineMetadata(enabled)
    override suspend fun setMetadataWifiOnly(enabled: Boolean) = store.setMetadataWifiOnly(enabled)
}
