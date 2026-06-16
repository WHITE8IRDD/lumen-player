package com.lumen.player.data.repository

import com.lumen.player.data.prefs.SettingsDataStore
import com.lumen.player.domain.model.AudioEffectsState
import com.lumen.player.domain.model.IntroConfig
import com.lumen.player.domain.repository.SettingsRepository
import com.lumen.player.domain.repository.ThemeMode
import com.lumen.player.domain.repository.UserSettings
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val store: SettingsDataStore
) : SettingsRepository {

    override val settings: StateFlow<UserSettings> = store.sharedFlow

    override suspend fun setThemeMode(mode: ThemeMode) = store.setThemeMode(mode)
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
    override suspend fun setOnboardingCompleted(enabled: Boolean) = store.setOnboardingCompleted(enabled)
    override suspend fun setFontSize(value: String) = store.setFontSize(value)
    override suspend fun setAccentColorHex(value: String) = store.setAccentColorHex(value)
    override suspend fun setAspectRatio(value: String) = store.setAspectRatio(value)
    override suspend fun setBrightnessMode(value: String) = store.setBrightnessMode(value)
    override suspend fun setSubtitleColor(value: String) = store.setSubtitleColor(value)
    override suspend fun setSubtitleLanguage(value: String) = store.setSubtitleLanguage(value)
    override suspend fun setAutoScanOnStartup(enabled: Boolean) = store.setAutoScanOnStartup(enabled)
    override suspend fun setShowAudio(enabled: Boolean) = store.setShowAudio(enabled)
    override suspend fun setReplayGain(enabled: Boolean) = store.setReplayGain(enabled)
    override suspend fun setSectionExpanded(section: String, expanded: Boolean) = store.setSectionExpanded(section, expanded)
    override suspend fun setFontFamilyChoice(value: String) = store.setFontFamilyChoice(value)
    override suspend fun setTextCaseChoice(value: String) = store.setTextCaseChoice(value)
    override suspend fun setWeightOffset(value: Int) = store.setWeightOffset(value)
    override suspend fun saveSkipIntroConfig(config: IntroConfig) = store.saveSkipIntroConfig(config)
    override suspend fun clearSkipIntroConfig(showName: String) = store.clearSkipIntroConfig(showName)
    override suspend fun getSkipIntroConfig(showName: String): IntroConfig? = store.getSkipIntroConfig(showName)
}
