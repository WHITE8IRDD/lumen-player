package com.lumen.player.player

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.lumen.player.MainActivity
import com.lumen.player.data.repository.MediaRepositoryImpl
import com.lumen.player.domain.repository.MediaRepository
import com.lumen.player.player.equalizer.EqualizerController
import com.lumen.player.player.notification.LumenMediaNotificationProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Foreground media-session service. Owns the [ExoPlayer] instance and exposes
 * it to controllers (UI, system, Android Auto, etc.) via [MediaSession].
 *
 * Problem 7: Keep the service alive in the background so playback continues
 * when the user navigates away. We start as a foreground service of type
 * `mediaPlayback` as soon as the first media item is prepared, and we only
 * stop ourselves when the queue is empty AND the user removes the task
 * (i.e. they explicitly dismissed the app from recents).
 */
@UnstableApi
@AndroidEntryPoint
class PlaybackService : MediaSessionService() {

    @Inject lateinit var playerManager: PlayerManager
    @Inject lateinit var equalizerController: EqualizerController
    @Inject lateinit var mediaRepository: MediaRepository

    private var mediaSession: MediaSession? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        val player: ExoPlayer = playerManager.build()
        equalizerController.attach(player.audioSessionId)

        val sessionActivityPendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        mediaSession = MediaSession.Builder(this, player)
            .setSessionActivity(sessionActivityPendingIntent)
            .setCallback(LumenSessionCallback())
            .build()

        setMediaNotificationProvider(LumenMediaNotificationProvider(this))

        // Periodically save resume position while playing.
        player.addListener(object : Player.Listener {
            private var lastSaved = 0L
            override fun onEvents(player: Player, events: Player.Events) {
                if (events.contains(Player.EVENT_POSITION_DISCONTINUITY) ||
                    events.contains(Player.EVENT_MEDIA_ITEM_TRANSITION)) {
                    saveResume(player)
                    lastSaved = player.currentPosition
                }
            }
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_ENDED || state == Player.STATE_IDLE) {
                    saveResume(player)
                }
            }
        })
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player
        if (player != null) {
            saveResume(player)
            // For video, the user "leaving" means going home or switching
            // apps — pause immediately. Audio keeps playing in the
            // background by design.
            if (isCurrentMediaVideo(player) && player.isPlaying) {
                player.pause()
            }
        }
        // If we're not actively playing, nothing for the service to do —
        // stop ourselves so the system can reclaim resources.
        if (player?.playWhenReady != true || player.mediaItemCount == 0) {
            stopSelf()
        }
        super.onTaskRemoved(rootIntent)
    }

    private fun saveResume(p: Player) {
        val id = p.currentMediaItem?.mediaId?.toLongOrNull() ?: return
        val pos = p.currentPosition
        if (pos <= 0) return
        scope.launch {
            runCatching {
                mediaRepository.updateResumePosition(id, pos)
                mediaRepository.updateLastPlayedAt(id)
            }
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? =
        mediaSession

    /**
     * Returns true when the currently playing media item is a video. Detection
     * is MIME-based, not track-based — a `.mp4` *audio-only* extract still
     * starts with `video/` and gets the no-notification treatment, which is
     * the correct behaviour (the user picked a file from the Videos tab and
     * expects the screen to stay on, not the lock screen).
     */
    private fun isCurrentMediaVideo(player: Player): Boolean {
        val mime = player.currentMediaItem?.localConfiguration?.mimeType
        return mime != null && mime.startsWith("video")
    }

    override fun onUpdateNotification(mediaSession: MediaSession) {
        super.onUpdateNotification(mediaSession)
    }

    override fun onDestroy() {
        val player = mediaSession?.player
        if (player != null) saveResume(player)
        mediaSession?.run {
            equalizerController.release()
            this.player.release()
            release()
        }
        mediaSession = null
        super.onDestroy()
    }
}
