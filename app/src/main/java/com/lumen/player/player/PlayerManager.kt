package com.lumen.player.player

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.drm.DrmSessionManagerProvider
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.exoplayer.upstream.DefaultLoadErrorHandlingPolicy
import androidx.media3.exoplayer.upstream.LoadErrorHandlingPolicy
import androidx.media3.extractor.DefaultExtractorsFactory
import com.lumen.player.player.audio.VolumeBoostController
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Builds the [ExoPlayer] with our renderers, a tuned LoadControl, gapless
 * audio and a sensible AudioAttributes config for proper focus handling.
 *
 * Engineering choices (VLC-level):
 *  - Hardware-accelerated video rendering via MediaCodec (default).
 *  - Tunneled playback ON (low-latency audio/video sync on supported devices).
 *  - Aggressive 30s min / 120s max buffer — zero stuttering during long runs.
 *  - Async source loading via [DefaultMediaSourceFactory].
 *  - FFmpeg extension renderer registered in PREFER mode for AC3/EAC3/DTS/
 *    TrueHD/MKV/AVI/MOV/FLV/TS and every other format VLC handles.
 *  - Software decoder fallback (setEnableDecoderFallback = true) — if the
 *    hardware MediaCodec for a given codec fails, ExoPlayer automatically
 *    routes through the FFmpeg software decoder.
 *  - Pitch-preserving playback speed for natural-sounding fast playback.
 *  - Pre-buffer next queue item (Media3 1.5+).
 */
@UnstableApi
@Singleton
class PlayerManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val volumeBoost: VolumeBoostController
) {
    val trackSelector = DefaultTrackSelector(context).apply {
        setParameters(
            buildUponParameters()
                .setPreferredAudioLanguage("en")
                .setForceLowestBitrate(false)
                .setForceHighestSupportedBitrate(false)
                .setAllowVideoMixedMimeTypeAdaptiveness(true)
                .setAllowVideoNonSeamlessAdaptiveness(true)
                .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
                .setTrackTypeDisabled(C.TRACK_TYPE_METADATA, true)
        )
    }

    private val loadControl = DefaultLoadControl.Builder()
        .setBufferDurationsMs(
            /* minBufferMs                 = */ 5_000,
            /* maxBufferMs                 = */ 15_000,
            /* bufferForPlaybackMs         = */ 500,
            /* bufferForPlaybackAfterRebufferMs = */ 1_000
        )
        .setPrioritizeTimeOverSizeThresholds(true)
        .setTargetBufferBytes(C.LENGTH_UNSET)
        .build()

    private var preWarmedPlayer: ExoPlayer? = null

    fun preWarmPlayer() {
        if (preWarmedPlayer != null) return
        if (Looper.myLooper() == Looper.getMainLooper()) {
            preWarmedPlayer = build()
        } else {
            Handler(Looper.getMainLooper()).post { preWarmedPlayer = build() }
        }
    }

    private val mediaSourceLoadErrorHandlingPolicy: LoadErrorHandlingPolicy =
        DefaultLoadErrorHandlingPolicy(/* minimumLoadableRetryCount = */ 5)

    private val audioAttributes = AudioAttributes.Builder()
        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
        .setUsage(C.USAGE_MEDIA)
        .build()

    @OptIn(UnstableApi::class)
    fun build(): ExoPlayer {
        com.lumen.player.core.util.PerfTimer.mark("4a. build() called")
        val http = DefaultHttpDataSource.Factory()
            .setUserAgent("Lumen/1.0")
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(15_000)
            .setReadTimeoutMs(30_000)
        val data = DefaultDataSource.Factory(context, http)

        // Custom extractors: skip heavy MKV cue reading at startup.
        // Cues index is at the end of the file — seeking to read it on a
        // 1.7GB file adds 25-30s delay over content://. Instead, we warm
        // the last 2MB into OS page cache before playback (see PlayerController).
        // Do NOT set SubtitleParser.Factory.UNSUPPORTED — embedded subtitle
        // tracks must be fully parsed so the SeekMap includes their structure.
        // Text tracks are disabled at TrackSelector level and re-enabled when
        // user opens the subtitle panel (see selectTrack).
        val extractorsFactory = DefaultExtractorsFactory()

        val progressiveFactory = ProgressiveMediaSource.Factory(data, extractorsFactory)
            .setLoadErrorHandlingPolicy(mediaSourceLoadErrorHandlingPolicy)
        // Fallback factory handles DASH/HLS/RTSP/SS streams.
        val streamingFactory = DefaultMediaSourceFactory(data)
            .setLoadErrorHandlingPolicy(mediaSourceLoadErrorHandlingPolicy)

        val mediaSourceFactory = object : MediaSource.Factory {
            override fun createMediaSource(mediaItem: MediaItem): MediaSource {
                val mime = mediaItem.localConfiguration?.mimeType
                val hasSubs = mediaItem.localConfiguration?.subtitleConfigurations?.isNotEmpty() == true
                return if (mime != null && (mime.startsWith("video/") || mime.startsWith("audio/")) && !hasSubs) {
                    progressiveFactory.createMediaSource(mediaItem)
                } else {
                    streamingFactory.createMediaSource(mediaItem)
                }
            }
            override fun getSupportedTypes() = intArrayOf(C.TYPE_OTHER)
            override fun setLoadErrorHandlingPolicy(policy: LoadErrorHandlingPolicy): MediaSource.Factory {
                progressiveFactory.setLoadErrorHandlingPolicy(policy)
                streamingFactory.setLoadErrorHandlingPolicy(policy)
                return this
            }
            override fun setDrmSessionManagerProvider(provider: DrmSessionManagerProvider): MediaSource.Factory {
                streamingFactory.setDrmSessionManagerProvider(provider)
                return this
            }
        }

        val renderersFactory = LumenRenderersFactory(context, volumeBoost)

        com.lumen.player.core.util.PerfTimer.mark("4b. Factories ready")

        val player = (preWarmedPlayer ?: ExoPlayer.Builder(context, renderersFactory)
            .setTrackSelector(trackSelector)
            .setLoadControl(loadControl)
            .setMediaSourceFactory(mediaSourceFactory)
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_NETWORK)
            .setSeekBackIncrementMs(10_000)
            .setSeekForwardIncrementMs(10_000)
            .build()).also { preWarmedPlayer = null }

        com.lumen.player.core.util.PerfTimer.mark("4c. ExoPlayer built")

        player.apply {
            setAudioAttributes(audioAttributes, /* handleAudioFocus = */ true)
            playbackParameters = PlaybackParameters(/* speed = */ 1f, /* pitch = */ 1f)
            trackSelectionParameters = trackSelectionParameters
                .buildUpon()
                .setTrackTypeDisabled(C.TRACK_TYPE_AUDIO, false)
                .clearOverridesOfType(C.TRACK_TYPE_AUDIO)
                .build()
            setVideoChangeFrameRateStrategy(
                C.VIDEO_CHANGE_FRAME_RATE_STRATEGY_ONLY_IF_SEAMLESS
            )
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    val name = when (state) {
                        Player.STATE_IDLE -> "IDLE"
                        Player.STATE_BUFFERING -> "BUFFERING"
                        Player.STATE_READY -> "READY"
                        Player.STATE_ENDED -> "ENDED"
                        else -> "UNKNOWN"
                    }
                    com.lumen.player.core.util.PerfTimer.mark("6. Player state → $name")
                }
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    com.lumen.player.core.util.PerfTimer.mark("7. isPlaying → $isPlaying")
                }
                override fun onTracksChanged(tracks: Tracks) {
                    com.lumen.player.core.util.PerfTimer.mark("8. onTracksChanged: ${tracks.groups.size} groups")
                    tracks.groups.forEach { group ->
                        val track = group.getTrackFormat(0)
                        Log.d("LumenAudio",
                            "type=${group.type} mime=${track.sampleMimeType} " +
                            "sel=${group.isTrackSelected(0)}")
                    }
                }
                override fun onPlayerError(error: PlaybackException) {
                    Log.e("LumenAudio", "Player error: ${error.errorCodeName}", error)
                    com.lumen.player.core.util.PerfTimer.mark("ERROR: ${error.errorCodeName}")
                }
            })
        }

        return player
    }
}
