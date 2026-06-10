package com.lumen.player.ui.library

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumen.player.core.util.M3uParser
import com.lumen.player.core.util.toMedia3Item
import com.lumen.player.data.local.dao.StreamDao
import com.lumen.player.data.local.entity.StreamEntity
import com.lumen.player.domain.model.MediaItem
import com.lumen.player.domain.model.MediaType
import com.lumen.player.domain.model.NetworkStream
import com.lumen.player.player.PlayerController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StreamGroup(
    val name: String,
    val streams: List<NetworkStream>
)

enum class StreamViewMode { LIST, GRID }

sealed class M3uImportState {
    data object Idle : M3uImportState()
    data object Importing : M3uImportState()
    data class Success(val count: Int, val fileName: String) : M3uImportState()
    data class Error(val message: String) : M3uImportState()
}

@HiltViewModel
class StreamsViewModel @Inject constructor(
    private val streamDao: StreamDao,
    private val controller: PlayerController
) : ViewModel() {

    private val allStreams = streamDao.observeAll().map { entities ->
        entities.map { it.toDomain() }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _importState = MutableStateFlow<M3uImportState>(M3uImportState.Idle)
    val importState: StateFlow<M3uImportState> = _importState.asStateFlow()

    fun dismissImportState() { _importState.value = M3uImportState.Idle }

    val streams get() = allStreams

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    fun setQuery(q: String) { _query.value = q }

    private val _viewMode = MutableStateFlow(StreamViewMode.LIST)
    val viewMode: StateFlow<StreamViewMode> = _viewMode.asStateFlow()

    fun setViewMode(mode: StreamViewMode) { _viewMode.value = mode }

    val favorites = streamDao.observeFavorites().map { entities ->
        entities.map { it.toDomain() }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val groupedStreams = combine(allStreams, _query) { streams, query ->
        val filtered = if (query.isBlank()) streams
            else streams.filter { it.title.contains(query, ignoreCase = true) }
        val favorites = filtered.filter { it.isFavorite }
        val others = filtered.filter { !it.isFavorite }
        val groups = mutableListOf<StreamGroup>()
        if (favorites.isNotEmpty()) {
            groups.add(StreamGroup("\u2605 Favorites", favorites))
        }
        others.groupBy { it.category ?: "Uncategorized" }.forEach { (cat, list) ->
            groups.add(StreamGroup(cat, list))
        }
        groups
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun toggleFavorite(id: Long) {
        viewModelScope.launch { streamDao.toggleFavorite(id) }
    }

    fun addStream(url: String, title: String) {
        viewModelScope.launch {
            val entity = StreamEntity(
                url = url,
                title = title.ifBlank { url },
                dateAdded = System.currentTimeMillis()
            )
            streamDao.insert(entity)
        }
    }

    fun addAndPlay(url: String, title: String, onPlay: (MediaItem) -> Unit) {
        viewModelScope.launch {
            val entity = StreamEntity(
                url = url,
                title = title.ifBlank { url },
                dateAdded = System.currentTimeMillis()
            )
            val id = streamDao.insert(entity)
            val mime = inferMimeType(url)
            val item = MediaItem(
                id = id,
                uri = Uri.parse(url),
                title = title.ifBlank { url },
                artist = null, album = null, albumArtUri = null,
                durationMs = 0L,
                mimeType = mime ?: "video/unknown",
                sizeBytes = 0L, folderPath = "",
                type = MediaType.VIDEO,
                dateAdded = entity.dateAdded,
                isVideo = true, lastPositionMs = 0L
            )
            controller.setQueue(listOf(item.toMedia3Item()), 0, 0L)
            onPlay(item)
        }
    }

    private fun inferMimeType(url: String): String? {
        val u = url.lowercase()
        return when {
            u.contains(".m3u8") || u.contains(".m3u8?") -> "application/x-mpegurl"
            u.contains(".mpd") -> "application/dash+xml"
            u.contains(".ts") || u.contains(".ts?") -> "video/mp2t"
            u.contains(".mp4") || u.contains(".mp4?") -> "video/mp4"
            u.contains(".aac") || u.contains(".aac?") -> "audio/aac"
            u.contains(".mp3") || u.contains(".mp3?") -> "audio/mpeg"
            u.contains(".ogg") || u.contains(".ogg?") -> "audio/ogg"
            u.contains(".flac") || u.contains(".flac?") -> "audio/flac"
            u.contains(".webm") || u.contains(".webm?") -> "video/webm"
            u.contains(".m3u") -> "application/x-mpegurl"
            u.contains(":8080") || u.contains(":554") -> "video/mp2t"
            else -> null
        }
    }

    fun playStream(stream: NetworkStream, onPlay: (MediaItem) -> Unit) {
        Log.d("STREAM_PLAY", "Tapped stream: name=${stream.title} url=${stream.url}")
        val item = toMediaItem(stream)
        val media3Item = item.toMedia3Item()
        controller.setQueue(listOf(media3Item), 0, stream.lastPositionMs)
        onPlay(item)
    }

    fun importM3uFile(uri: Uri, ctx: android.content.Context) {
        viewModelScope.launch {
            _importState.value = M3uImportState.Importing
            try {
                val input = ctx.contentResolver.openInputStream(uri)
                val content = input?.bufferedReader()?.readText() ?: ""
                input?.close()
                val fileName = android.net.Uri.decode(uri.lastPathSegment ?: "playlist").substringAfterLast('/')
                val groupName = fileName.removeSuffix(".m3u").removeSuffix(".m3u8")
                val entries = M3uParser.parse(content, groupName)
                if (entries.isEmpty()) {
                    _importState.value = M3uImportState.Error("No channels found in $fileName")
                    return@launch
                }
                val entities = entries.map { entry ->
                    StreamEntity(
                        url = entry.url,
                        title = entry.name,
                        playlistName = groupName,
                        category = entry.groupTitle,
                        logoUrl = entry.logo,
                        dateAdded = System.currentTimeMillis()
                    )
                }
                streamDao.insertAll(entities)
                val domainStreams = entities.map { it.toDomain() }
                controller.setStreamPlaylistContext(domainStreams)
                _importState.value = M3uImportState.Success(entities.size, fileName)
            } catch (e: Exception) {
                Log.e("StreamsVM", "Import failed", e)
                _importState.value = M3uImportState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun importM3u(content: String, fileName: String, onPlay: (MediaItem) -> Unit) {
        viewModelScope.launch {
            try {
                val groupName = fileName.removeSuffix(".m3u").removeSuffix(".m3u8")
                val entries = M3uParser.parse(content, groupName)
                if (entries.isEmpty()) {
                    Log.w("StreamsVM", "No entries found in $fileName")
                    return@launch
                }
                val entities = entries.map { entry ->
                    StreamEntity(
                        url = entry.url,
                        title = entry.name,
                        playlistName = groupName,
                        category = entry.groupTitle,
                        logoUrl = entry.logo,
                        dateAdded = System.currentTimeMillis()
                    )
                }
                streamDao.insertAll(entities)
                val domainStreams = entities.map { it.toDomain() }
                controller.setStreamPlaylistContext(domainStreams)
                Log.d("StreamsVM", "Imported ${entities.size} channels from $fileName")
                if (entities.isNotEmpty()) {
                    val first = entities.first()
                    val mime = inferMimeType(first.url)
                    val item = MediaItem(
                        id = first.id,
                        uri = Uri.parse(first.url),
                        title = first.title,
                        artist = null, album = null, albumArtUri = null,
                        durationMs = 0L,
                        mimeType = mime ?: "video/unknown",
                        sizeBytes = 0L, folderPath = "",
                        type = MediaType.VIDEO,
                        dateAdded = first.dateAdded,
                        isVideo = true, lastPositionMs = 0L
                    )
                    controller.setQueue(listOf(item.toMedia3Item()), 0, 0L)
                    onPlay(item)
                }
            } catch (e: Exception) {
                Log.e("StreamsVM", "Import failed", e)
            }
        }
    }

    fun deleteStream(id: Long) {
        viewModelScope.launch { streamDao.delete(id) }
    }

    fun toMediaItem(stream: NetworkStream): MediaItem {
        val mime = inferMimeType(stream.url)
        return MediaItem(
            id = stream.id,
            uri = Uri.parse(stream.url),
            title = stream.title,
            artist = null, album = null, albumArtUri = null,
            durationMs = 0L,
            mimeType = mime ?: "video/unknown",
            sizeBytes = 0L, folderPath = "",
            type = MediaType.VIDEO,
            dateAdded = stream.dateAdded,
            isVideo = true, lastPositionMs = stream.lastPositionMs
        )
    }

    fun getAllChannels(): List<NetworkStream> = allStreams.value

    /** Switch to a different channel in the currently playing context */
    fun switchToChannel(stream: NetworkStream, onPlay: (MediaItem) -> Unit) {
        viewModelScope.launch {
            val item = toMediaItem(stream)
            val media3Item = item.toMedia3Item()
            controller.setQueue(listOf(media3Item), 0, 0L)
            onPlay(item)
        }
    }
}

private fun StreamEntity.toDomain() = NetworkStream(
    id = id,
    url = url,
    title = title,
    playlistName = playlistName,
    dateAdded = dateAdded,
    lastPositionMs = lastPositionMs,
    isFavorite = isFavorite,
    category = category,
    logoUrl = logoUrl
)
