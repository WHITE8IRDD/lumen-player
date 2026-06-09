package com.lumen.player.data.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.lumen.player.domain.repository.MediaRepository
import com.lumen.player.ui.util.ThumbnailExtractor
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ThumbnailPreGenWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d(TAG, "Starting thumbnail pre-generation")
        val mediaRepository = EntryPointAccessors.fromApplication(
            applicationContext,
            MediaRepositoryEntryPoint::class.java
        ).mediaRepository()

        val videos = mediaRepository.getAllVideos()
        Log.d(TAG, "Found ${videos.size} videos to process")

        var processed = 0
        var cached = 0
        var failed = 0

        for (video in videos) {
            val cacheFile = ThumbnailExtractor.cacheFileFor(
                applicationContext, video.id, video.uri, DEFAULT_TIME_US
            )
            if (cacheFile != null && cacheFile.exists() && cacheFile.length() > 0L) {
                cached++
                continue
            }

            val bitmap = withContext(Dispatchers.IO) {
                ThumbnailExtractor.extractWithFallback(applicationContext, video.uri)
            }
            if (bitmap != null && cacheFile != null) {
                ThumbnailExtractor.writeCache(cacheFile, bitmap)
                bitmap.recycle()
                processed++
            } else {
                failed++
            }
        }

        Log.d(TAG, "Pre-generation done: $processed extracted, $cached already cached, $failed failed")
        return Result.success()
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface MediaRepositoryEntryPoint {
        fun mediaRepository(): MediaRepository
    }

    companion object {
        private const val TAG = "ThumbnailPreGen"
        const val WORK_NAME = "thumbnail_pregen"
        private const val DEFAULT_TIME_US = 1_000_000L
    }
}
