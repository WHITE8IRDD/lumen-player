package com.lumen.player.data.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.lumen.player.domain.repository.MediaRepository
import com.lumen.player.domain.repository.MetadataResolver
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.delay

class TmdbEnrichmentWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d(TAG, "Starting TMDB metadata enrichment")

        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            EnrichmentEntryPoint::class.java
        )
        val mediaRepository = entryPoint.mediaRepository()
        val resolver = entryPoint.metadataResolver()

        var videos = mediaRepository.getVideosWithNullPoster()
        var attempts = 0
        while (videos.isEmpty() && attempts < MAX_SCAN_WAIT_ATTEMPTS) {
            val total = mediaRepository.getAllVideos()
            if (total.isEmpty()) {
                Log.d(TAG, "Scan still in progress, waiting ${SCAN_WAIT_MS}ms (attempt ${attempts + 1})")
                delay(SCAN_WAIT_MS)
                attempts++
                videos = mediaRepository.getVideosWithNullPoster()
            } else {
                break
            }
        }
        Log.d(TAG, "Found ${videos.size} videos without poster")

        var enriched = 0
        var failed = 0

        for ((i, video) in videos.withIndex()) {
            if (i > 0 && i % BATCH_SIZE == 0) {
                delay(BATCH_DELAY_MS)
            }
            try {
                val meta = resolver.resolve(video.title)
                if (meta.posterUrl != null) {
                    mediaRepository.updatePosterUrl(video.id, meta.posterUrl)
                    enriched++
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to enrich ${video.id}: ${e.message}")
                failed++
            }
        }

        Log.d(TAG, "Enrichment done: $enriched enriched, $failed failed")
        return Result.success()
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface EnrichmentEntryPoint {
        fun mediaRepository(): MediaRepository
        fun metadataResolver(): MetadataResolver
    }

    companion object {
        private const val TAG = "TmdbEnrichment"
        private const val BATCH_SIZE = 10
        private const val BATCH_DELAY_MS = 200L
        private const val SCAN_WAIT_MS = 5000L
        private const val MAX_SCAN_WAIT_ATTEMPTS = 24
    }
}
