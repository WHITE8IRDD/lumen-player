package com.lumen.player.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import com.lumen.player.core.util.Constants
import com.lumen.player.core.util.toMedia3Item
import com.lumen.player.domain.model.Folder
import com.lumen.player.domain.model.MediaItem
import com.lumen.player.domain.repository.MediaRepository
import com.lumen.player.domain.repository.SettingsRepository
import com.lumen.player.player.PlayerController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@UnstableApi
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val controller: PlayerController,
    private val repository: MediaRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val playbackState = controller.state

    val continueWatching: StateFlow<List<MediaItem>> =
        repository.observeContinueWatching()
            .distinctUntilChanged()
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val recentlyAdded: StateFlow<List<MediaItem>> =
        repository.observeRecentlyAdded()
            .distinctUntilChanged()
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val topFolders: StateFlow<List<Folder>> =
        repository.observeTopFolders()
            .distinctUntilChanged()
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val recentAudio: StateFlow<List<MediaItem>> =
        repository.observeRecentAudio()
            .distinctUntilChanged()
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        viewModelScope.launch {
            while (true) {
                delay(Constants.PLAYER_POSITION_TICK_MS)
                controller.refreshPosition()
            }
        }
        refresh()
    }

    fun playPause() = controller.playPause()
    fun stopPlayback() = controller.stopAndClear()

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            runCatching { repository.rescan().collect { } }
            _isRefreshing.value = false
        }
    }

    fun playItem(item: MediaItem) {
        controller.setQueue(listOf(item.toMedia3Item()), 0, item.lastPositionMs)
    }

    fun hideFromHome(item: MediaItem) {
        viewModelScope.launch {
            repository.setHiddenFromHome(item.id, true)
        }
    }

    fun hideFromContinueWatching(item: MediaItem) {
        viewModelScope.launch {
            repository.setHiddenFromContinueWatching(item.id, true)
        }
    }

    fun unhideFromHome(item: MediaItem) {
        viewModelScope.launch {
            repository.setHiddenFromHome(item.id, false)
        }
    }
}
