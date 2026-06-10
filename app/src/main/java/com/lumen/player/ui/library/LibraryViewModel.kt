package com.lumen.player.ui.library

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import com.lumen.player.core.util.toMedia3Item
import com.lumen.player.core.util.toMedia3Items
import com.lumen.player.data.local.dao.MediaDao
import com.lumen.player.data.local.dao.PlaylistDao
import com.lumen.player.data.local.entity.PlaylistItemEntity
import com.lumen.player.domain.model.MediaItem
import com.lumen.player.domain.repository.MediaRepository
import com.lumen.player.domain.repository.SettingsRepository
import com.lumen.player.domain.usecase.ScanMediaUseCase
import com.lumen.player.player.PlayerController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@UnstableApi
@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val scanMedia: ScanMediaUseCase,
    private val controller: PlayerController,
    private val repository: MediaRepository,
    private val settingsRepository: SettingsRepository,
    private val mediaDao: MediaDao,
    private val playlistDao: PlaylistDao
) : ViewModel() {

    private val _isScanning = MutableStateFlow(false)
    val isScanning = _isScanning.asStateFlow()

    init {
        rescan()
    }

    val videoItems = repository.observeVideo()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val audioItems = repository.observeAudio()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val sortedVideoItems = videoItems
        .map { list -> list.sortedByDescending { it.dateAdded } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val videoFolders = repository.observeVideoFolders()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private var initialScanDone = false

    fun onPermissionGranted() {
        if (initialScanDone) return
        initialScanDone = true
        rescan()
    }

    fun rescan() {
        viewModelScope.launch {
            _isScanning.value = true
            runCatching { scanMedia().collect { } }
            _isScanning.value = false
        }
    }

    fun play(items: List<MediaItem>, startIndex: Int) {
        if (items.isEmpty()) return
        controller.setQueue(items.toMedia3Items(), startIndex)
    }

    fun playAtIndex(items: List<MediaItem>, index: Int) {
        if (items.isEmpty() || index !in items.indices) return
        val item = items[index]
        viewModelScope.launch {
            val saved = runCatching { mediaDao.get(item.id)?.lastPositionMs ?: 0L }.getOrDefault(0L)
            val pos = if (saved > 5_000L) saved else 0L
            controller.setQueue(items.toMedia3Items(), index, pos)
            runCatching { repository.incrementPlayCount(item.id) }
        }
    }

    fun shuffleAll() {
        val list = audioItems.value
        if (list.isEmpty()) return
        play(list.shuffled(), 0)
    }

    fun playNext(selected: List<MediaItem>) {
        if (selected.isEmpty()) return
        android.util.Log.d("ACTION", "playNext: ${selected.size} items")
        val idx = controller.currentQueueIndex()
        val items = selected.toMedia3Items()
        if (idx < 0) {
            controller.setQueue(items, 0)
        } else {
            items.forEachIndexed { i, item -> controller.addMediaItem(idx + 1 + i, item) }
        }
    }

    fun addToQueue(selected: List<MediaItem>) {
        selected.forEach { controller.addToQueue(it.toMedia3Item()) }
    }

    fun share(context: Context, selected: List<MediaItem>) {
        val uris = ArrayList<Uri>(selected.map { it.uri })
        val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = "video/*"
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, null))
    }

    fun createDeleteSender(context: Context, selected: List<MediaItem>): IntentSenderRequest? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val uris = selected.map { it.uri }
            val pendingIntent = MediaStore.createDeleteRequest(context.contentResolver, uris)
            return IntentSenderRequest.Builder(pendingIntent).build()
        }
        return null
    }

    fun requestDelete(context: Context, selected: List<MediaItem>) {
        selected.forEach { item ->
            runCatching { context.contentResolver.delete(item.uri, null, null) }
        }
    }

    fun addToPlaylist(playlistId: Long, mediaIds: List<Long>) {
        viewModelScope.launch {
            mediaIds.forEachIndexed { index, mediaId ->
                playlistDao.addItem(PlaylistItemEntity(playlistId, mediaId, index))
            }
        }
    }

    fun removeFromPlaylist(playlistId: Long, mediaIds: List<Long>) {
        viewModelScope.launch {
            mediaIds.forEach { mediaId ->
                playlistDao.removeItem(playlistId, mediaId)
            }
        }
    }

    fun hideFromHome(item: MediaItem) {
        viewModelScope.launch {
            repository.setHiddenFromHome(item.id, true)
        }
    }

    fun unhideFromHome(item: MediaItem) {
        viewModelScope.launch {
            repository.setHiddenFromHome(item.id, false)
        }
    }

}
