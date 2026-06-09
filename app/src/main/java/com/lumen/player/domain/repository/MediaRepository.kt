package com.lumen.player.domain.repository

import com.lumen.player.domain.model.Folder
import com.lumen.player.domain.model.MediaItem
import com.lumen.player.domain.model.MediaType
import com.lumen.player.domain.model.ScanProgress
import kotlinx.coroutines.flow.Flow

interface MediaRepository {
    fun observeByType(type: MediaType): Flow<List<MediaItem>>
    fun observeVideo(): Flow<List<MediaItem>>
    fun observeAudio(): Flow<List<MediaItem>>
    fun observeFavorites(): Flow<List<MediaItem>>
    fun observeFolders(): Flow<List<Folder>>
    fun observeByFolder(path: String): Flow<List<MediaItem>>
    fun observeVideoFolders(): Flow<List<Folder>>
    fun observeExcludedFolders(): Flow<List<String>>

    suspend fun rescan(): Flow<ScanProgress>
    suspend fun updateResumePosition(id: Long, positionMs: Long)
    suspend fun setFavorite(id: Long, favorite: Boolean)
    suspend fun incrementPlayCount(id: Long)
    suspend fun setFolderExcluded(path: String, excluded: Boolean)

    fun observeContinueWatching(): Flow<List<MediaItem>>
    fun observeRecentlyAdded(): Flow<List<MediaItem>>
    fun observeRecentAudio(): Flow<List<MediaItem>>
    fun observeTopFolders(): Flow<List<Folder>>
    suspend fun updateLastPlayedAt(id: Long)

    suspend fun getAllVideos(): List<MediaItem>

    suspend fun searchVideos(query: String): List<MediaItem>
    suspend fun searchAudio(query: String): List<MediaItem>
    suspend fun searchFolders(query: String): List<Folder>

    suspend fun setHiddenFromHome(id: Long, hidden: Boolean)
    suspend fun setHiddenFromContinueWatching(id: Long, hidden: Boolean)
    fun observeHiddenItems(): Flow<List<MediaItem>>
    suspend fun clearAllHiddenFlags()
}
