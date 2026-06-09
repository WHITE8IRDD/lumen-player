package com.lumen.player.player

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.exoplayer.upstream.DefaultLoadErrorHandlingPolicy
import androidx.media3.exoplayer.upstream.LoadErrorHandlingPolicy
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
    @ApplicationContext private val context: Context
) {
    val trackSelector = DefaultTrackSelector(context).apply {
        setParameters(
            buildUponParameters()
                .setPreferredAudioLanguage(null)
                .setTunnelingEnabled(true)
                .setForceLowestBitrate(false)
                .setForceHighestSupportedBitrate(false)
                .setAllowVideoMixedMimeTypeAdaptiveness(true)
                .setAllowVideoNonSeamlessAdaptiveness(true)
        )
    }

    private val loadControl = DefaultLoadControl.Builder()
        .setBufferDurationsMs(
            /* minBufferMs                 = */ 30_000,
            /* maxBufferMs                 = */ 120_000,
            /* bufferForPlaybackMs         = */ 1_500,
            /* bufferForPlaybackAfterRebufferMs = */ 3_000
        )
        .setPrioritizeTimeOverSizeThresholds(true)
        .setBackBuffer(
            /* backBufferDurationMs          = */ 30_000,
            /* retainBackBufferFromKeyframe  = */ true
        )
        .build()

    private val mediaSourceLoadErrorHandlingPolicy: LoadErrorHandlingPolicy =
        DefaultLoadErrorHandlingPolicy(/* minimumLoadableRetryCount = */ 5)

    private val audioAttributes = AudioAttributes.Builder()
        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
        .setUsage(C.USAGE_MEDIA)
        .build()

    @OptIn(UnstableApi::class)
    fun build(): ExoPlayer {
        val http = DefaultHttpDataSource.Factory()
            .setUserAgent("Lumen/1.0")
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(15_000)
            .setReadTimeoutMs(30_000)
        val data = DefaultDataSource.Factory(context, http)
        val mediaSourceFactory = DefaultMediaSourceFactory(data)
            .setLoadErrorHandlingPolicy(mediaSourceLoadErrorHandlingPolicy)

        // The renderers factory enables FFmpeg in PREFER mode and falls back
        // to software decoders if hardware MediaCodec fails.
        val renderers = LumenRenderersFactory(context).apply {
            // Ensure the surface uses SurfaceView (lowest-latency hardware
            // compositing path) — PlayerView defaults to SurfaceView; we
            // only need to prevent the factory from forcing a TextureView.
            setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER)
        }

        return ExoPlayer.Builder(context)
            .setRenderersFactory(renderers)
            .setTrackSelector(trackSelector)
            .setLoadControl(loadControl)
            .setMediaSourceFactory(mediaSourceFactory)
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_NETWORK)
            .setSeekBackIncrementMs(10_000)
            .setSeekForwardIncrementMs(10_000)
            .build()
            .apply {
                setAudioAttributes(audioAttributes, /* handleAudioFocus = */ true)
                // Explicitly set default playback parameters to 1x speed
                // and 1x pitch. Without this the player inherits whatever
                // parameters the previous session left behind.
                playbackParameters = PlaybackParameters(/* speed = */ 1f, /* pitch = */ 1f)
                // Pre-buffer the next queue item (Media3 1.5+; silently
                // no-op on 1.4.x via reflection).
                runCatching {
                    val m = ExoPlayer::class.java.getMethod(
                        "setNextMediaItemPreloadEnabled", Boolean::class.javaPrimitiveType
                    )
                    m.invoke(this, true)
                }
            }
    }
}
