package com.lumen.player

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.media3.common.util.UnstableApi
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import coil.Coil
import com.lumen.player.data.worker.ThumbnailPreGenWorker
import com.lumen.player.ui.util.LumenImageLoaderFactory
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit

@HiltAndroidApp
class LumenApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        createPlaybackChannel()
        installCoil()
        scheduleThumbnailPreGen()
    }

    @UnstableApi
    private fun installCoil() {
        Coil.setImageLoader(LumenImageLoaderFactory.create(this))
    }

    private fun createPlaybackChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            PLAYBACK_CHANNEL_ID,
            getString(R.string.playback_channel),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(R.string.playback_channel_description)
            setShowBadge(false)
            setSound(null, null)
        }
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(channel)
    }

    private fun scheduleThumbnailPreGen() {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()

        val request = PeriodicWorkRequestBuilder<ThumbnailPreGenWorker>(
            24, TimeUnit.HOURS
        ).setConstraints(constraints).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            ThumbnailPreGenWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            request
        )
    }

    companion object {
        const val PLAYBACK_CHANNEL_ID = "lumen.playback"
        const val PLAYBACK_NOTIFICATION_ID = 1001
    }
}
