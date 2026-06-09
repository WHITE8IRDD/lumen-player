package com.lumen.player.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumen.player.domain.model.MediaItem
import com.lumen.player.domain.repository.MediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsHiddenItemsViewModel @Inject constructor(
    private val repository: MediaRepository
) : ViewModel() {

    val hiddenItems: StateFlow<List<MediaItem>> =
        repository.observeHiddenItems()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun unhideItem(id: Long) {
        viewModelScope.launch {
            repository.setHiddenFromHome(id, false)
            repository.setHiddenFromContinueWatching(id, false)
        }
    }

    fun restoreAll() {
        viewModelScope.launch {
            repository.clearAllHiddenFlags()
        }
    }
}
