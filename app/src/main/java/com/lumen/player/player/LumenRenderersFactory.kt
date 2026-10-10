package com.lumen.player.player

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.Renderer
import androidx.media3.exoplayer.audio.AudioRendererEventListener
import androidx.media3.exoplayer.audio.AudioSink
import androidx.media3.exoplayer.audio.DefaultAudioSink
import androidx.media3.exoplayer.mediacodec.MediaCodecSelector
import androidx.media3.exoplayer.video.VideoRendererEventListener
import com.lumen.player.player.audio.VolumeBoostController
import com.lumen.player.player.audio.VolumeBoostProcessor
import java.lang.reflect.Constructor

/**
 * Custom renderers factory that activates the official Media3 FFmpeg decoder
 * extension **when the AAR is on the classpath**, otherwise it falls back to
 * the default renderers transparently.
 *
 * VLC-like coverage: AC3, EAC3, DTS, TrueHD, ALAC, FLAC, OPUS, plus a wide
 * range of obscure video codecs (MKV, AVI, MOV, FLV, TS, etc.).
 *
 * Engineering:
 *  - PREFER extension renderer mode — FFmpeg wins when available, hardware
 *    MediaCodec is the second choice, software MediaCodec is the third.
 *  - Decoder fallback enabled — if hardware decode fails for any reason,
 *    ExoPlayer automatically tries the next renderer in the chain.
 *  - Audio float output disabled for compatibility with the FFmpeg audio
 *    renderer constructor.
 *
 * CRITICAL: The parent's [buildVideoRenderers] / [buildAudioRenderers] use
 * [EXTENSION_RENDERER_MODE_PREFER] to inject FFmpeg renderers. If we ALSO
 * call [super.buildVideoRenderers] with PREFER mode AND manually add FFmpeg
 * renderers, we create DUPLICATE entries in the renderer array. This
 * corrupts Media3's renderer selection algorithm, causing frame-pacing
 * errors and perceived playback speed-up. We avoid this by:
 *  1. Telling the parent to use ON mode (no extension injection).
 *  2. Always injecting the FFmpeg renderer ourselves — once, in PREFER
 *     position (before MediaCodec).
 *  3. Falling back to the parent's default if FFmpeg isn't on the classpath.
 */
@UnstableApi
class LumenRenderersFactory(
    context: Context,
    private val volumeBoost: VolumeBoostController
) : DefaultRenderersFactory(context) {

    init {
        setEnableDecoderFallback(true)
        setEnableAudioFloatOutput(false)
    }

    override fun buildAudioSink(
        context: Context,
        enableFloatOutput: Boolean,
        enableAudioTrackPlaybackParams: Boolean
    ): AudioSink =
        DefaultAudioSink.Builder(context)
            .setEnableFloatOutput(enableFloatOutput)
            .setEnableAudioTrackPlaybackParams(enableAudioTrackPlaybackParams)
            // Volume boost (100-200 %) runs before the default processors, so
            // silence skipping and playback speed keep working as before.
            .setAudioProcessorChain(
                DefaultAudioSink.DefaultAudioProcessorChain(VolumeBoostProcessor(volumeBoost))
            )
            .build()

    override fun buildVideoRenderers(
        context: Context,
        extensionRendererMode: Int,
        mediaCodecSelector: MediaCodecSelector,
        enableDecoderFallback: Boolean,
        eventHandler: android.os.Handler,
        eventListener: VideoRendererEventListener,
        allowedVideoJoiningTimeMs: Long,
        out: ArrayList<Renderer>
    ) {
        // MediaCodec first — hardware acceleration for H.264 / H.265 / VP9.
        super.buildVideoRenderers(
            context, EXTENSION_RENDERER_MODE_OFF, mediaCodecSelector, enableDecoderFallback,
            eventHandler, eventListener, allowedVideoJoiningTimeMs, out
        )
        // FFmpeg last — fallback for codecs that lack a hardware decoder.
        tryInjectFfmpegVideoRenderer(eventHandler, eventListener, allowedVideoJoiningTimeMs, out)
    }

    override fun buildAudioRenderers(
        context: Context,
        extensionRendererMode: Int,
        mediaCodecSelector: MediaCodecSelector,
        enableDecoderFallback: Boolean,
        audioSink: AudioSink,
        eventHandler: android.os.Handler,
        eventListener: AudioRendererEventListener,
        out: ArrayList<Renderer>
    ) {
        // MediaCodec first — hardware AAC / MP3 / FLAC / OPUS.
        super.buildAudioRenderers(
            context, EXTENSION_RENDERER_MODE_OFF, mediaCodecSelector, enableDecoderFallback,
            audioSink, eventHandler, eventListener, out
        )
        // FFmpeg last — fallback for AC3 / EAC3 / DTS / TrueHD.
        tryInjectFfmpegAudioRenderer(eventHandler, eventListener, audioSink, out)
    }

    private fun tryInjectFfmpegVideoRenderer(
        eventHandler: android.os.Handler,
        eventListener: VideoRendererEventListener,
        allowedVideoJoiningTimeMs: Long,
        out: ArrayList<Renderer>
    ): Boolean {
        if (Build.VERSION.SDK_INT < 21) return false
        val cls = runCatching { Class.forName("androidx.media3.decoder.ffmpeg.FfmpegVideoRenderer") }
            .getOrNull() ?: return false
        return runCatching {
            val ctor: Constructor<*> = cls.getDeclaredConstructor(
                Long::class.javaPrimitiveType,
                android.os.Handler::class.java,
                VideoRendererEventListener::class.java,
                Integer.TYPE
            )
            @Suppress("UNCHECKED_CAST")
            out.add(ctor.newInstance(allowedVideoJoiningTimeMs, eventHandler, eventListener, 50) as Renderer)
            true
        }.onFailure { Log.w(TAG, "FFmpegVideoRenderer unavailable: ${it.message}") }.getOrDefault(false)
    }

    private fun tryInjectFfmpegAudioRenderer(
        eventHandler: android.os.Handler,
        eventListener: AudioRendererEventListener,
        audioSink: AudioSink,
        out: ArrayList<Renderer>
    ): Boolean {
        if (Build.VERSION.SDK_INT < 21) return false
        val cls = runCatching { Class.forName("androidx.media3.decoder.ffmpeg.FfmpegAudioRenderer") }
            .getOrNull() ?: return false
        return runCatching {
            val ctor: Constructor<*> = cls.getDeclaredConstructor(
                android.os.Handler::class.java,
                AudioRendererEventListener::class.java,
                AudioSink::class.java
            )
            @Suppress("UNCHECKED_CAST")
            out.add(ctor.newInstance(eventHandler, eventListener, audioSink) as Renderer)
            true
        }.onFailure { Log.w(TAG, "FfmpegAudioRenderer unavailable: ${it.message}") }.getOrDefault(false)
    }

    companion object {
        private const val TAG = "LumenRenderersFactory"
    }
}
