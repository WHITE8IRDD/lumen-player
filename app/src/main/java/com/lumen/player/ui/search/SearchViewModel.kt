package com.lumen.player.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumen.player.data.local.dao.PlaylistDao
import com.lumen.player.data.prefs.SearchDataStore
import com.lumen.player.domain.model.Folder
import com.lumen.player.domain.model.MediaItem
import com.lumen.player.domain.model.Playlist
import com.lumen.player.domain.repository.MediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchResults(
    val videos: List<MediaItem> = emptyList(),
    val audio: List<MediaItem> = emptyList(),
    val folders: List<Folder> = emptyList(),
    val playlists: List<Playlist> = emptyList()
)

@OptIn(ExperimentalCoroutinesApi::class, kotlinx.coroutines.FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val mediaRepository: MediaRepository,
    private val playlistDao: PlaylistDao,
    private val searchDataStore: SearchDataStore
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    val recentSearches: StateFlow<List<String>> = searchDataStore.observeRecentSearches()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _searchResults = MutableStateFlow(SearchResults())
    val searchResults: StateFlow<SearchResults> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    init {
        viewModelScope.launch {
            _query
                .debounce(200)
                .filter { it.length >= 2 || it.isEmpty() }
                .collect { q ->
                    if (q.isEmpty()) {
                        _searchResults.value = SearchResults()
                        _isSearching.value = false
                    } else {
                        _isSearching.value = true
                        val videos = mediaRepository.searchVideos(q)
                        val audio = mediaRepository.searchAudio(q)
                        val folders = mediaRepository.searchFolders(q)
                        val playlistEntities = playlistDao.search(q)
                        val playlists = playlistEntities.map { e ->
                            Playlist(id = e.id, name = e.name, itemCount = 0, createdAt = e.createdAt)
                        }
                        _searchResults.value = SearchResults(videos, audio, folders, playlists)
                        _isSearching.value = false
                    }
                }
        }
    }

    fun setQuery(query: String) {
        _query.value = query
    }

    fun submitSearch(query: String) {
        viewModelScope.launch {
            searchDataStore.addRecentSearch(query.trim())
        }
    }

    fun clearRecentSearches() {
        viewModelScope.launch {
            searchDataStore.clearRecentSearches()
        }
    }
}
