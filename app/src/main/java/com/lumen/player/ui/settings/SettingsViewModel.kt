package com.lumen.player.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumen.player.BuildConfig
import com.lumen.player.domain.repository.MediaRepository
import com.lumen.player.domain.repository.SettingsRepository
import com.lumen.player.domain.repository.ThemeMode
import com.lumen.player.domain.repository.UserSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepository,
    private val mediaRepository: MediaRepository
) : ViewModel() {

    val settings = repository.settings

    val isTmdbApiAvailable = BuildConfig.TMDB_API_KEY.isNotEmpty()

    fun setThemeMode(mode: ThemeMode) = viewModelScope.launch { repository.setThemeMode(mode) }
    fun setDynamicColor(v: Boolean) = viewModelScope.launch { repository.setDynamicColor(v) }
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
}
