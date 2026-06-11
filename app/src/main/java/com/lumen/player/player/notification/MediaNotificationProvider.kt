package com.lumen.player.player.notification

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaNotification
import androidx.media3.session.MediaSession
import com.lumen.player.MainActivity

@UnstableApi
class LumenMediaNotificationProvider(
    private val context: Context
) : MediaNotification.Provider {

    private val default = androidx.media3.session.DefaultMediaNotificationProvider.Builder(context)
        .setChannelId(com.lumen.player.LumenApplication.PLAYBACK_CHANNEL_ID)
        .setChannelName(com.lumen.player.R.string.playback_channel)
        .setNotificationId(com.lumen.player.LumenApplication.PLAYBACK_NOTIFICATION_ID)
        .build()

    override fun createNotification(
        mediaSession: MediaSession,
        customLayout: com.google.common.collect.ImmutableList<androidx.media3.session.CommandButton>,
        actionFactory: MediaNotification.ActionFactory,
        onNotificationChangedCallback: MediaNotification.Provider.Callback
    ): MediaNotification {
        val mn = default.createNotification(mediaSession, customLayout, actionFactory, onNotificationChangedCallback)
        val player = mediaSession.player
        if (player.videoSize.height > 0) {
            val metadata = player.mediaMetadata
            val title = metadata.title?.toString() ?: "Video"
            val sessionIntent = PendingIntent.getActivity(
                context, 0,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            val builder = androidx.core.app.NotificationCompat.Builder(context, com.lumen.player.LumenApplication.PLAYBACK_CHANNEL_ID)
                .setContentTitle(title)
                .setSubText("Video")
                .setContentIntent(sessionIntent)
                .setSmallIcon(com.lumen.player.R.drawable.ic_notification)
                .setOngoing(player.isPlaying)
                .setVisibility(androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC)
                .setSilent(true)
            mn.notification.contentIntent = sessionIntent
            return MediaNotification(mn.notificationId, builder.build())
        }
        return mn
    }

    override fun handleCustomCommand(
        session: MediaSession,
        action: String,
        extras: android.os.Bundle
    ): Boolean = default.handleCustomCommand(session, action, extras)
}