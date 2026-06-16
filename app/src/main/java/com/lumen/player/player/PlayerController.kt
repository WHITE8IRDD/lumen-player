package com.lumen.player.player

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.TrackGroup
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.common.Tracks
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import com.lumen.player.core.util.Constants
import com.lumen.player.domain.model.NetworkStream
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class LyricsLine(val timeMs: Long, val text: String)

@Singleton
class PlayerController @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var controller: MediaController? = null
    var directPlayer: ExoPlayer? = null
    val isConnected: Boolean get() = controller != null

    private val _state = MutableStateFlow(PlaybackState())
    val state: StateFlow<PlaybackState> = _state.asStateFlow()

    val isPlaying: Boolean get() = _state.value.isPlaying
    val currentPositionMs: Long get() = _state.value.positionMs
    val currentItemId: String? get() = _state.value.currentItemId
    val durationMs: Long get() = _state.value.durationMs

    private val _lyrics = MutableStateFlow<List<LyricsLine>>(emptyList())
    val lyrics: StateFlow<List<LyricsLine>> = _lyrics.asStateFlow()

    private val _tracks = MutableStateFlow<Tracks?>(null)
    val tracks: StateFlow<Tracks?> = _tracks.asStateFlow()

    private val _streamPlaylistContext = MutableStateFlow<List<NetworkStream>>(emptyList())
    val streamPlaylistContext: StateFlow<List<NetworkStream>> = _streamPlaylistContext.asStateFlow()

    fun setStreamPlaylistContext(channels: List<NetworkStream>) {
        _streamPlaylistContext.value = channels
    }

    fun isPlayingM3uStream(): Boolean {
        val c = controller ?: return false
        val uri = c.currentMediaItem?.localConfiguration?.uri ?: return false
        return uri.toString().startsWith("http") && _streamPlaylistContext.value.isNotEmpty()
    }

    fun initialize() {
        if (controller != null) return
        com.lumen.player.core.util.PerfTimer.mark("4d. initialize() connecting to PlaybackService")
        Log.d("PlayerCtrl", "initialize() — connecting to PlaybackService")
        val token = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        val future = MediaController.Builder(context, token).buildAsync()
        future.addListener({
            controller = future.get().also { c ->
                Log.d("PlayerCtrl", "initialize() — connected, controller=${System.identityHashCode(c)}")
                com.lumen.player.core.util.PerfTimer.mark("4e. MediaController connected")
                c.addListener(playerListener)
                pushState()
            }
        }, MoreExecutors.directExecutor())
    }

    private val playerListener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            pushState()
            _tracks.value = player.currentTracks
        }
        override fun onTracksChanged(tracks: Tracks) { _tracks.value = tracks }
    }

    fun refreshPosition() {
        val c = controller ?: return
        if (!c.isPlaying) return
        _state.value = _state.value.copy(
            positionMs = c.currentPosition.coerceAtLeast(0L),
            bufferedMs = c.bufferedPosition
        )
    }

    private fun pushState() {
        val c = controller ?: return
        val md = c.mediaMetadata
        val item = c.currentMediaItem
        val newState = PlaybackState(
            isPlaying = c.isPlaying,
            isBuffering = c.playbackState == Player.STATE_BUFFERING,
            playbackState = c.playbackState,
            currentItemId = item?.mediaId,
            title = md.title?.toString().orEmpty(),
            artist = md.artist?.toString().orEmpty(),
            album = md.albumTitle?.toString().orEmpty(),
            artworkUri = md.artworkUri,
            durationMs = c.duration.coerceAtLeast(0L),
            positionMs = c.currentPosition.coerceAtLeast(0L),
            bufferedMs = c.bufferedPosition,
            playbackSpeed = c.playbackParameters.speed,
            repeatMode = c.repeatMode,
            shuffle = c.shuffleModeEnabled,
            isVideo = c.videoSize.height > 0 || item?.mediaMetadata?.mediaType == MediaMetadata.MEDIA_TYPE_VIDEO,
            hasNext = c.hasNextMediaItem(),
            hasPrevious = c.hasPreviousMediaItem()
        )
        Log.d("PlayerCtrl", "pushState() → playing=${newState.isPlaying} buff=${newState.isBuffering} id=${newState.currentItemId} title='${newState.title}'")
        _state.value = newState
    }

    fun setQueue(items: List<MediaItem>, startIndex: Int, positionMs: Long = 0L) {
        com.lumen.player.core.util.PerfTimer.mark("5a. setQueue items=${items.size} start=$startIndex")
        Log.d("PlayerCtrl", "setQueue() items=${items.size} start=$startIndex controller=${controller != null}")
        val current = items.getOrNull(startIndex) ?: return

        // Storage location check
        val uri = current.localConfiguration?.uri?.toString() ?: ""
        val storageHint = when {
            uri.contains("/storage/emulated/0/") -> "Internal"
            uri.contains("/storage/") -> "SD/USB (could be slow)"
            uri.startsWith("http") -> "Network"
            else -> "Unknown: $uri"
        }
        com.lumen.player.core.util.PerfTimer.mark("STORAGE: $storageHint mime=${current.localConfiguration?.mimeType}")

        val player = directPlayer ?: controller
        player?.run {
            com.lumen.player.core.util.PerfTimer.mark("5b. calling setMediaItem")
            setMediaItem(current, positionMs)
            com.lumen.player.core.util.PerfTimer.mark("5c. setMediaItem done, calling prepare()")
            prepare()
            com.lumen.player.core.util.PerfTimer.mark("5d. prepare() returned")
            playWhenReady = true
            com.lumen.player.core.util.PerfTimer.mark("5e. playWhenReady=true")
            val after = items.subList(startIndex + 1, items.size)
            val before = items.subList(0, startIndex)
            addMediaItems(1, after)
            addMediaItems(0, before)
        }
        val newState = PlaybackState(
            isPlaying = true,
            playbackState = Player.STATE_READY,
            currentItemId = current.mediaId,
            title = current.mediaMetadata?.title?.toString().orEmpty(),
            artist = current.mediaMetadata?.artist?.toString().orEmpty(),
            album = current.mediaMetadata?.albumTitle?.toString().orEmpty(),
            artworkUri = current.mediaMetadata?.artworkUri,
            durationMs = (current.mediaMetadata?.durationMs ?: 0L).coerceAtLeast(0L),
            positionMs = positionMs,
            isVideo = current.mediaMetadata?.mediaType == MediaMetadata.MEDIA_TYPE_VIDEO,
            hasNext = items.size > startIndex + 1,
            hasPrevious = startIndex > 0
        )
        Log.d("PlayerCtrl", "setQueue() → playing=${newState.isPlaying} id=${newState.currentItemId} title='${newState.title}'")
        _state.value = newState
    }

    fun addToQueue(item: MediaItem) { controller?.addMediaItem(item) }
    fun playPause() = controller?.run { if (isPlaying) pause() else play() }
    fun play() { controller?.play() }
    fun pause() { controller?.pause() }
    fun stop() { controller?.stop() }
    fun stopAndClear() {
        controller?.run {
            stop()
            clearMediaItems()
        }
        _state.value = PlaybackState()
    }
    fun hasActiveMedia(): Boolean {
        return currentItemId != null && durationMs > 0
    }
    fun next() = controller?.seekToNext() ?: Unit
    fun previous() = controller?.seekToPrevious() ?: Unit
    fun seekTo(ms: Long) { controller?.seekTo(ms) }
    fun seekBy(deltaMs: Long) = controller?.run {
        seekTo((currentPosition + deltaMs).coerceIn(0, duration.coerceAtLeast(0)))
    } ?: Unit
    fun setSpeed(speed: Float) { controller?.setPlaybackSpeed(speed) }
    fun setSpeedPitched(speed: Float) {
        controller?.playbackParameters = PlaybackParameters(speed.coerceIn(0.25f, 4f), 1f)
    }
    fun setRepeatMode(mode: Int) { controller?.repeatMode = mode }
    fun toggleShuffle() { controller?.run { shuffleModeEnabled = !shuffleModeEnabled } }
    fun setSkipSilence(skip: Boolean) {
        // Media3 enables skip-silence at renderer-factory build time.
        // Persist preference in DataStore and re-apply on next service start.
    }

    fun getPlayer(): Player? = directPlayer ?: controller

    fun setVolume(v: Float) { controller?.volume = v.coerceIn(0f, 1f) }

    fun moveQueueItem(from: Int, to: Int) { controller?.moveMediaItem(from, to) }
    fun removeQueueItem(index: Int) { controller?.removeMediaItem(index) }
    fun jumpToQueueItem(index: Int) { controller?.seekTo(index, 0L) }
    fun queueSize(): Int = controller?.mediaItemCount ?: 0
    fun currentQueueIndex(): Int = controller?.currentMediaItemIndex ?: -1
    fun queueItemAt(index: Int): MediaItem? = controller?.getMediaItemAt(index)
    fun addMediaItem(index: Int, item: MediaItem) { controller?.addMediaItem(index, item) }

    fun selectTrack(groupId: String?, trackIndex: Int) {
        val c = controller ?: return
        if (groupId == null) {
            c.trackSelectionParameters = TrackSelectionParameters.Builder(context)
                .setTrackTypeDisabled(C.TRACK_TYPE_AUDIO, false)
                .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)
                .clearOverridesOfType(C.TRACK_TYPE_AUDIO)
                .clearOverridesOfType(C.TRACK_TYPE_TEXT)
                .build()
            return
        }
        val tg = c.currentTracks.groups.firstOrNull { it.mediaTrackGroup.id == groupId }?.mediaTrackGroup
        if (tg == null) return
        val params = c.trackSelectionParameters.buildUpon()
            .setOverrideForType(TrackSelectionOverride(tg, trackIndex))
        // Re-enable text tracks when user selects a subtitle — they're suppressed
        // at startup for faster preparation.
        if (tg.type == C.TRACK_TYPE_TEXT) {
            params.setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)
        }
        c.trackSelectionParameters = params.build()
    }

    /**
     * Adds a user-supplied subtitle file to the currently playing media item.
     * Supports SRT, SSA/ASS, VTT, TTML — any mime type that Media3's text
     * renderer can parse. Existing embedded subtitle tracks are preserved.
     */
    fun addSubtitle(uri: Uri, mimeType: String, language: String? = null) {
        val c = controller ?: return
        val current = c.currentMediaItem ?: return
        val existing = current.localConfiguration?.subtitleConfigurations.orEmpty()
        val newSub = MediaItem.SubtitleConfiguration.Builder(uri)
            .setMimeType(mimeType)
            .setLanguage(language)
            .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
            .build()
        val updated = current.buildUpon()
            .setSubtitleConfigurations(existing + newSub)
            .build()
        c.replaceMediaItem(c.currentMediaItemIndex, updated)
    }

    fun setSubtitleOffset(delayUs: Long) {
        // Subtitle offset requires Media3 1.5+ where TrackSelectionParameters.Builder
        // exposes setSubtitleOffsetUs. Stub for now; delay is preserved in UI state.
    }

    fun setLyrics(lines: List<LyricsLine>) { _lyrics.value = lines }

    fun release() {
        Log.d("PlayerCtrl", "release() — disconnecting, keeping last state")
        controller?.removeListener(playerListener)
        controller?.release()
        controller = null
    }
}
