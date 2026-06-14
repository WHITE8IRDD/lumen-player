package com.lumen.player.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumen.player.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val repository: SettingsRepository
) : ViewModel() {

    fun complete(tmdbEnabled: Boolean, hwAccel: Boolean) {
        viewModelScope.launch {
            repository.setOnboardingCompleted(true)
            repository.setFetchOnlineMetadata(tmdbEnabled)
            repository.setHardwareAcceleration(hwAccel)
        }
    }
}
