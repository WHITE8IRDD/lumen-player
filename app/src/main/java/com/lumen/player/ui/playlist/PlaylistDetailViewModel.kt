package com.lumen.player.ui.playlist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumen.player.core.util.toMedia3Items
import com.lumen.player.data.local.dao.PlaylistDao
import com.lumen.player.data.mapper.MediaMapper
import com.lumen.player.domain.model.MediaItem
import com.lumen.player.player.PlayerController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PlaylistDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val playlistDao: PlaylistDao,
    private val mapper: MediaMapper,
    private val controller: PlayerController
) : ViewModel() {

    val playlistId: Long = savedStateHandle["id"] ?: 0L

    val playlistName = playlistDao.observeById(playlistId).mapLatest { entity ->
        entity?.name ?: ""
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    val items = playlistDao.observeItems(playlistId).mapLatest { entities ->
        mapper.toDomain(entities)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun play(items: List<MediaItem>, startIndex: Int) {
        if (items.isEmpty()) return
        controller.setQueue(items.toMedia3Items(), startIndex)
    }

    fun playAll(items: List<MediaItem>) {
        play(items, 0)
    }

    fun shufflePlay(items: List<MediaItem>) {
        play(items.shuffled(), 0)
    }

    fun removeItem(playlistId: Long, mediaId: Long) {
        viewModelScope.launch {
            playlistDao.removeItem(playlistId, mediaId)
        }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            playlistDao.delete(playlistId)
        }
    }

    fun renamePlaylist(playlistId: Long, name: String) {
        viewModelScope.launch {
            playlistDao.create(com.lumen.player.data.local.entity.PlaylistEntity(id = playlistId, name = name))
        }
    }
}
