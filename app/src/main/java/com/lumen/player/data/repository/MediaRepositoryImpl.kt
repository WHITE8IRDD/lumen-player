package com.lumen.player.data.repository

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.lumen.player.data.local.dao.FolderDao
import com.lumen.player.data.local.dao.MediaDao
import com.lumen.player.data.local.dao.TmdbCacheDao
import com.lumen.player.data.mapper.MediaMapper
import com.lumen.player.data.mediastore.MediaStoreScanner
import com.lumen.player.data.worker.ThumbnailPreGenWorker
import com.lumen.player.data.worker.TmdbEnrichmentWorker
import com.lumen.player.domain.model.Folder
import com.lumen.player.domain.model.MediaItem
import com.lumen.player.domain.model.MediaType
import com.lumen.player.domain.model.ScanProgress
import com.lumen.player.domain.repository.MediaRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@Singleton
class MediaRepositoryImpl @Inject constructor(
    private val mediaDao: MediaDao,
    private val folderDao: FolderDao,
    private val tmdbCacheDao: TmdbCacheDao,
    private val scanner: MediaStoreScanner,
    private val mapper: MediaMapper,
    @ApplicationContext private val context: Context
) : MediaRepository {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _scanProgress = MutableStateFlow<ScanProgress?>(null)
    val scanProgress: Flow<ScanProgress?> = _scanProgress

    init {
        val audioObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) { triggerAutoScan() }
        }
        val videoObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) { triggerAutoScan() }
        }
        context.contentResolver.registerContentObserver(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, true, audioObserver
        )
        context.contentResolver.registerContentObserver(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI, true, videoObserver
        )
    }

    private var lastAutoScan = 0L
    private val autoScanInterval = 5000L

    private fun triggerAutoScan() {
        val now = System.currentTimeMillis()
        if (now - lastAutoScan < autoScanInterval) return
        lastAutoScan = now
        scope.launch { executeScan() }
    }

    private suspend fun executeScan(): ScanProgress {
        _scanProgress.value = ScanProgress.Scanning(0, 0)
        val excluded = folderDao.excludedPaths().toSet()
        val audio = scanner.scanAudio().filter { it.folderPath !in excluded }
        val videos = scanner.scanVideo().filter { it.folderPath !in excluded }
        val all = audio + videos
        _scanProgress.value = ScanProgress.Scanning(0, all.size)
        mediaDao.upsertAll(all)
        mediaDao.deleteRemoved(all.map { it.id })
        triggerThumbnailPreGen()
        val done = ScanProgress.Done(all.size)
        _scanProgress.value = done
        return done
    }

    private fun triggerThumbnailPreGen() {
        val constraints = Constraints.Builder().setRequiresBatteryNotLow(true).build()
        val request = OneTimeWorkRequestBuilder<ThumbnailPreGenWorker>()
            .setConstraints(constraints).build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            "thumbnail_post_scan",
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    override fun observeByType(type: MediaType): Flow<List<MediaItem>> =
        mediaDao.observeByType(type.name).mapLatest { rows -> mapper.toDomain(rows) }

    override fun observeVideo(): Flow<List<MediaItem>> =
        mediaDao.observeVideo().debounce(500).mapLatest { mapper.toDomain(it) }

    override fun observeAudio(): Flow<List<MediaItem>> =
        mediaDao.observeAudio().debounce(500).mapLatest { mapper.toDomain(it) }

    override fun observeFavorites(): Flow<List<MediaItem>> =
        mediaDao.observeFavorites().mapLatest { mapper.toDomain(it) }

    override fun observeFolders(): Flow<List<Folder>> =
        mediaDao.observeFolderSummaries().map { rows ->
            rows.map {
                Folder(
                    path = it.path,
                    displayName = File(it.path).name.ifEmpty { it.path },
                    mediaCount = it.mediaCount,
                    firstItemArtUri = it.firstArt
                )
            }
        }

    override fun observeVideoFolders(): Flow<List<Folder>> =
        mediaDao.observeVideoFolderSummaries().map { rows ->
            rows.map { row ->
                val progress = if (row.firstVideoDurationMs > 0L)
                    (row.firstVideoLastPositionMs.toFloat() / row.firstVideoDurationMs).coerceIn(0f, 1f)
                else 0f
                Folder(
                    path = row.path,
                    displayName = File(row.path).name.ifEmpty { row.path },
                    mediaCount = row.mediaCount,
                    firstItemArtUri = row.firstVideoUri,
                    dateAdded = row.dateAdded,
                    totalDurationMs = row.totalDurationMs,
                    totalSizeBytes = row.totalSizeBytes,
                    firstVideoId = row.firstVideoId,
                    firstVideoUri = row.firstVideoUri?.let { Uri.parse(it) },
                    firstVideoTitle = row.firstVideoTitle,
                    firstVideoDurationMs = row.firstVideoDurationMs,
                    firstVideoSizeBytes = row.firstVideoSizeBytes,
                    firstVideoProgress = progress,
                    firstVideoPosterUrl = row.firstVideoPosterUrl
                )
            }
        }

    override fun observeExcludedFolders(): Flow<List<String>> = callbackFlow {
        val job = scope.launch {
            folderDao.observeAll().collect { folders ->
                trySend(folders.filter { it.isExcluded }.map { it.path })
            }
        }
        awaitClose { job.cancel() }
    }

    override fun observeByFolder(path: String): Flow<List<MediaItem>> =
        mediaDao.observeByFolder(path).mapLatest { mapper.toDomain(it) }

    override suspend fun rescan(): Flow<ScanProgress> = flow {
        val result = executeScan()
        emit(result)
    }

    override suspend fun updateResumePosition(id: Long, positionMs: Long) {
        mediaDao.updatePosition(id, positionMs)
    }

    override suspend fun setFavorite(id: Long, favorite: Boolean) {
        mediaDao.setFavorite(id, favorite)
    }

    override suspend fun incrementPlayCount(id: Long) {
        mediaDao.incrementPlayCount(id)
    }

    override suspend fun setFolderExcluded(path: String, excluded: Boolean) {
        folderDao.upsert(com.lumen.player.data.local.entity.FolderEntity(path = path, isExcluded = excluded))
    }

    override fun observeContinueWatching(): Flow<List<MediaItem>> =
        mediaDao.observeContinueWatching().mapLatest { mapper.toDomain(it) }

    override fun observeRecentlyAdded(): Flow<List<MediaItem>> {
        val thirtyDaysAgo = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000
        return mediaDao.observeRecentlyAdded(thirtyDaysAgo).mapLatest { mapper.toDomain(it) }
    }

    override fun observeRecentAudio(): Flow<List<MediaItem>> =
        mediaDao.observeRecentAudio().mapLatest { mapper.toDomain(it) }

    override fun observeTopFolders(): Flow<List<Folder>> =
        mediaDao.observeTopFolders().map { rows ->
            rows.map {
                Folder(
                    path = it.path,
                    displayName = java.io.File(it.path).name.ifEmpty { it.path },
                    mediaCount = it.mediaCount,
                    firstItemArtUri = it.firstArt
                )
            }
        }

    override suspend fun updateLastPlayedAt(id: Long) {
        mediaDao.updateLastPlayedAt(id, System.currentTimeMillis())
    }

    override suspend fun getAllVideos(): List<MediaItem> =
        mapper.toDomain(mediaDao.getAllVideos())

    override suspend fun searchVideos(query: String): List<MediaItem> =
        mapper.toDomain(mediaDao.searchVideos(query))

    override suspend fun searchAudio(query: String): List<MediaItem> =
        mapper.toDomain(mediaDao.searchAudio(query))

    override suspend fun searchFolders(query: String): List<Folder> =
        mediaDao.searchFolderPaths(query).map { path ->
            Folder(
                path = path,
                displayName = java.io.File(path).name.ifEmpty { path },
                mediaCount = 0
            )
        }

    override suspend fun setHiddenFromHome(id: Long, hidden: Boolean) {
        mediaDao.setHiddenFromHome(id, hidden)
    }

    override suspend fun setHiddenFromContinueWatching(id: Long, hidden: Boolean) {
        mediaDao.setHiddenFromContinueWatching(id, hidden)
    }

    override fun observeHiddenItems(): Flow<List<MediaItem>> =
        mediaDao.observeHiddenItems().mapLatest { mapper.toDomain(it) }

    override suspend fun clearAllHiddenFlags() {
        mediaDao.clearAllHiddenFlags()
    }

    override suspend fun getVideosWithNullPoster(): List<MediaItem> =
        mapper.toDomain(mediaDao.getVideosWithNullPoster())

    override suspend fun updatePosterUrl(id: Long, posterUrl: String?) {
        mediaDao.updatePosterUrl(id, posterUrl)
    }

    override suspend fun clearMetadataCache() {
        tmdbCacheDao.deleteAll()
        val allVideos = mediaDao.getAllVideos()
        for (video in allVideos) {
            mediaDao.updatePosterUrl(video.id, null)
        }
    }

    override fun triggerMetadataEnrichment() {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()
        val request = OneTimeWorkRequestBuilder<TmdbEnrichmentWorker>()
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            "tmdb_enrichment",
            ExistingWorkPolicy.REPLACE,
            request
        )
    }
}