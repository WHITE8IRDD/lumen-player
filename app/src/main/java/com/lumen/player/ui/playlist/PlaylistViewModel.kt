package com.lumen.player.ui.playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumen.player.data.local.dao.PlaylistDao
import com.lumen.player.data.local.entity.PlaylistEntity
import com.lumen.player.data.local.entity.PlaylistItemEntity
import com.lumen.player.domain.model.Playlist
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistViewModel @Inject constructor(
    private val playlistDao: PlaylistDao
) : ViewModel() {

    val playlists = playlistDao.observeAllWithCount().map { entities ->
        entities.map { pwc ->
            Playlist(
                id = pwc.id,
                name = pwc.name,
                createdAt = pwc.createdAt,
                itemCount = pwc.itemCount
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun createPlaylist(name: String, onCreated: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = playlistDao.create(PlaylistEntity(name = name))
            onCreated(id)
        }
    }

    fun deletePlaylist(id: Long) {
        viewModelScope.launch {
            playlistDao.delete(id)
        }
    }

    fun renamePlaylist(id: Long, name: String) {
        viewModelScope.launch {
            playlistDao.create(PlaylistEntity(id = id, name = name))
        }
    }

    fun addItems(playlistId: Long, mediaIds: List<Long>) {
        viewModelScope.launch {
            mediaIds.forEachIndexed { index, mediaId ->
                playlistDao.addItem(PlaylistItemEntity(playlistId, mediaId, index))
            }
        }
    }

    fun removeItem(playlistId: Long, mediaId: Long) {
        viewModelScope.launch {
            playlistDao.removeItem(playlistId, mediaId)
        }
    }
}
