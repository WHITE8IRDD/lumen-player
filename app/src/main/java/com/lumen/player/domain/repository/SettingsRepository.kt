package com.lumen.player.domain.repository

import com.lumen.player.domain.model.AudioEffectsState
import com.lumen.player.domain.model.IntroConfig
import kotlinx.coroutines.flow.StateFlow

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
    val onboardingCompleted: Boolean = false,
    val accentColorHex: String = "#FFC857",
    val fontSize: String = "DEFAULT",
    val aspectRatio: String = "AUTO",
    val brightnessMode: String = "SYSTEM",
    val subtitleColor: String = "WHITE",
    val subtitleLanguage: String = "English",
    val opensubtitlesApiKey: String = "",
    val autoScanOnStartup: Boolean = true,
    val showAudio: Boolean = true,
    val replayGain: Boolean = false,
    val fontFamilyChoice: String = "SERIF_DISPLAY",
    val textCaseChoice: String = "LOWERCASE",
    val weightOffset: Int = 0,
    val sectionAppearance: Boolean = true,
    val sectionPlayback: Boolean = true,
    val sectionVideo: Boolean = false,
    val sectionSubtitles: Boolean = false,
    val sectionLibrary: Boolean = false,
    val sectionAudio: Boolean = false,
    val sectionNetwork: Boolean = false,
    val sectionAdvanced: Boolean = false,
    val sectionAbout: Boolean = false,
)

interface SettingsRepository {
    val settings: StateFlow<UserSettings>
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
    suspend fun setOnboardingCompleted(enabled: Boolean)
    suspend fun setFontSize(value: String)
    suspend fun setAccentColorHex(value: String)
    suspend fun setAspectRatio(value: String)
    suspend fun setBrightnessMode(value: String)
    suspend fun setSubtitleColor(value: String)
    suspend fun setSubtitleLanguage(value: String)
    suspend fun setOpensubtitlesApiKey(value: String)
    suspend fun setAutoScanOnStartup(enabled: Boolean)
    suspend fun setShowAudio(enabled: Boolean)
    suspend fun setReplayGain(enabled: Boolean)
    suspend fun setSectionExpanded(section: String, expanded: Boolean)
    suspend fun setFontFamilyChoice(value: String)
    suspend fun setTextCaseChoice(value: String)
    suspend fun setWeightOffset(value: Int)
    suspend fun saveSkipIntroConfig(config: IntroConfig)
    suspend fun clearSkipIntroConfig(showName: String)
    suspend fun getSkipIntroConfig(showName: String): IntroConfig?
}
