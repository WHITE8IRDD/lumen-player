package com.lumen.player.ui.folders

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import com.lumen.player.core.util.toMedia3Item
import com.lumen.player.core.util.toMedia3Items
import com.lumen.player.data.local.dao.MediaDao
import com.lumen.player.domain.model.MediaItem
import com.lumen.player.domain.repository.MediaRepository
import com.lumen.player.player.PlayerController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import java.io.File
import javax.inject.Inject

@UnstableApi
@HiltViewModel
class FoldersViewModel @Inject constructor(
    getFolders: com.lumen.player.domain.usecase.GetFoldersUseCase
) : ViewModel() {

    val folders = getFolders()
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList<com.lumen.player.domain.model.Folder>())
}

@UnstableApi
@HiltViewModel
class FolderDetailViewModel @Inject constructor(
    private val repository: MediaRepository,
    savedState: SavedStateHandle,
    private val controller: PlayerController,
    private val mediaDao: MediaDao
) : ViewModel() {

    private val encoded: String = savedState["path"] ?: ""
    val path: String = java.net.URLDecoder.decode(encoded, "utf-8")
    val folderName: String = File(path).name.ifBlank { path }

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val items = repository.observeByFolder(path)
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList<MediaItem>())

    init {
        viewModelScope.launch {
            repository.observeByFolder(path).first {
                _isLoading.value = false
                true
            }
        }
    }

    fun play(items: List<MediaItem>, startIndex: Int) {
        if (items.isEmpty() || startIndex !in items.indices) return
        val item = items[startIndex]
        viewModelScope.launch {
            val saved = runCatching { mediaDao.get(item.id)?.lastPositionMs ?: 0L }.getOrDefault(0L)
            val pos = if (saved > 5_000L) saved else 0L
            controller.setQueue(items.toMedia3Items(), startIndex, pos)
            runCatching { repository.incrementPlayCount(item.id) }
        }
    }

    fun playNext(selected: List<MediaItem>) {
        if (selected.isEmpty()) return
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
}
