package com.lumen.player.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumen.player.BuildConfig
import com.lumen.player.domain.repository.MediaRepository
import com.lumen.player.domain.repository.SettingsRepository
import com.lumen.player.domain.repository.ThemeMode
import com.lumen.player.domain.repository.UserSettings
import com.lumen.player.player.audio.VolumeBoostController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepository,
    private val mediaRepository: MediaRepository,
    private val volumeBoost: VolumeBoostController
) : ViewModel() {

    val settings = repository.settings

    /** One-line description of whether the boost is reaching the audio output. */
    val boostEngineStatus: StateFlow<String> = combine(
        repository.settings,
        volumeBoost.engineState,
        volumeBoost.formatName
    ) { s, state, format ->
        boostStatusText(s.volumeBoostEnabled, state, format)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    init {
        viewModelScope.launch {
            repository.settings.collect { _isReady.value = true }
        }
    }

    val isTmdbApiAvailable = BuildConfig.TMDB_API_KEY.isNotEmpty()

    fun setThemeMode(mode: ThemeMode) = viewModelScope.launch { repository.setThemeMode(mode) }
    fun setResumePlayback(v: Boolean) = viewModelScope.launch { repository.setResumePlayback(v) }
    fun setRespectAudioFocus(v: Boolean) = viewModelScope.launch { repository.setRespectAudioFocus(v) }
    fun setSkipSilence(v: Boolean) = viewModelScope.launch { repository.setSkipSilence(v) }
    fun setGapless(v: Boolean) = viewModelScope.launch { repository.setGaplessPlayback(v) }
    fun setShowVideos(v: Boolean) = viewModelScope.launch { repository.setShowVideos(v) }
    fun setHardwareAcceleration(v: Boolean) = viewModelScope.launch { repository.setHardwareAcceleration(v) }
    fun setFetchOnlineMetadata(v: Boolean) = viewModelScope.launch {
        repository.setFetchOnlineMetadata(v)
        if (v) {
            val existing = mediaRepository.getAllVideos()
            if (existing.isEmpty()) mediaRepository.rescan().collect { }
            mediaRepository.triggerMetadataEnrichment()
        }
        }
    fun setMetadataWifiOnly(v: Boolean) = viewModelScope.launch { repository.setMetadataWifiOnly(v) }
    fun clearMetadataCache() = viewModelScope.launch {
        mediaRepository.clearMetadataCache()
    }

    fun setOnboardingCompleted() = viewModelScope.launch { repository.setOnboardingCompleted(true) }
    fun setFontSize(v: String) = viewModelScope.launch { repository.setFontSize(v) }
    fun setAccentColorHex(v: String) = viewModelScope.launch { repository.setAccentColorHex(v) }
    fun setAspectRatio(v: String) = viewModelScope.launch { repository.setAspectRatio(v) }
    fun setBrightnessMode(v: String) = viewModelScope.launch { repository.setBrightnessMode(v) }
    fun setSubtitleColor(v: String) = viewModelScope.launch { repository.setSubtitleColor(v) }
    fun setSubtitleTextSize(v: Float) = viewModelScope.launch { repository.setSubtitleTextSize(v) }
    fun setSubtitleDelayUs(v: Long) = viewModelScope.launch { repository.setSubtitleDelayUs(v) }
    fun setSubtitleLanguage(v: String) = viewModelScope.launch { repository.setSubtitleLanguage(v) }
    fun setFontFamilyChoice(v: String) = viewModelScope.launch { repository.setFontFamilyChoice(v) }
    fun setTextCaseChoice(v: String) = viewModelScope.launch { repository.setTextCaseChoice(v) }
    fun setWeightOffset(v: Int) = viewModelScope.launch { repository.setWeightOffset(v) }
    fun setAutoScanOnStartup(v: Boolean) = viewModelScope.launch { repository.setAutoScanOnStartup(v) }
    fun setShowAudio(v: Boolean) = viewModelScope.launch { repository.setShowAudio(v) }
    fun setReplayGain(v: Boolean) = viewModelScope.launch { repository.setReplayGain(v) }
    fun setVolumeBoostEnabled(v: Boolean) = viewModelScope.launch { repository.setVolumeBoostEnabled(v) }
    fun setVolumeBoostMaxPercent(v: Int) = viewModelScope.launch { repository.setVolumeBoostMaxPercent(v) }
    fun setSectionExpanded(section: String, expanded: Boolean) = viewModelScope.launch {
        repository.setSectionExpanded(section, expanded)
    }

    fun resetAllSettings() = viewModelScope.launch {
        repository.setThemeMode(ThemeMode.SYSTEM)
        repository.setResumePlayback(true)
        repository.setRespectAudioFocus(true)
        repository.setSkipSilence(false)
        repository.setGaplessPlayback(true)
        repository.setShowVideos(true)
        repository.setHardwareAcceleration(true)
        repository.setFetchOnlineMetadata(false)
        repository.setMetadataWifiOnly(true)
        repository.setFontSize("DEFAULT")
        repository.setAspectRatio("AUTO")
        repository.setBrightnessMode("SYSTEM")
        repository.setSubtitleColor("WHITE")
        repository.setSubtitleLanguage("English")
        repository.setAutoScanOnStartup(true)
        repository.setShowAudio(true)
        repository.setReplayGain(false)
        repository.setFontFamilyChoice("SERIF_DISPLAY")
        repository.setTextCaseChoice("LOWERCASE")
        repository.setWeightOffset(0)
        repository.setVolumeBoostEnabled(false)
        repository.setVolumeBoostMaxPercent(VolumeBoostController.DEFAULT_MAX_PERCENT)
    }

    fun resetOnboarding() = viewModelScope.launch {
        repository.setOnboardingCompleted(false)
    }

    private fun boostStatusText(
        enabled: Boolean,
        state: VolumeBoostController.EngineState,
        format: String
    ): String = when {
        !enabled -> "Off. Turn on the boost to go above 100%."
        state == VolumeBoostController.EngineState.IDLE ->
            "Waiting for audio. Play a song or video to check."
        state == VolumeBoostController.EngineState.ACTIVE ->
            "Working. The boost is applied to $format audio."
        else ->
            "Not available for this audio ($format). Playback stays at 100%."
    }
}
