package com.lumen.player.player.audio

import androidx.media3.common.C
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.audio.BaseAudioProcessor
import androidx.media3.common.util.UnstableApi
import java.nio.ByteBuffer
import kotlin.math.roundToInt

/**
 * Multiplies decoded PCM samples by the current [VolumeBoostController] gain.
 *
 * It sits in the ExoPlayer audio pipeline, so it affects every renderer the
 * same way. At gain 1.0 the buffer is copied through unchanged, so there is
 * no audible difference below 100 %.
 *
 * Supports 16-bit integer PCM and float PCM. For any other encoding the
 * processor is inactive and reports that to the controller.
 */
@UnstableApi
class VolumeBoostProcessor(
    private val boost: VolumeBoostController
) : BaseAudioProcessor() {

    override fun onConfigure(inputAudioFormat: AudioProcessor.AudioFormat): AudioProcessor.AudioFormat {
        val encoding = inputAudioFormat.encoding
        val supported = encoding == C.ENCODING_PCM_16BIT || encoding == C.ENCODING_PCM_FLOAT
        boost.reportFormat(describe(encoding), supported)
        return if (supported) inputAudioFormat else AudioProcessor.AudioFormat.NOT_SET
    }

    override fun queueInput(inputBuffer: ByteBuffer) {
        val remaining = inputBuffer.remaining()
        if (remaining == 0) return

        val gain = boost.gain
        val out = replaceOutputBuffer(remaining)
        out.order(inputBuffer.order())

        if (gain <= VolumeBoostController.MIN_LEVEL) {
            out.put(inputBuffer)
        } else {
            when (inputAudioFormat.encoding) {
                C.ENCODING_PCM_16BIT -> boost16Bit(inputBuffer, out, gain)
                C.ENCODING_PCM_FLOAT -> boostFloat(inputBuffer, out, gain)
                else -> out.put(inputBuffer)
            }
        }
        // Copy any trailing bytes that do not form a whole sample.
        if (inputBuffer.hasRemaining()) out.put(inputBuffer)
        out.flip()
    }

    private fun boost16Bit(input: ByteBuffer, out: ByteBuffer, gain: Float) {
        while (input.remaining() >= 2) {
            out.putShort(applyGain16(input.short, gain))
        }
    }

    private fun boostFloat(input: ByteBuffer, out: ByteBuffer, gain: Float) {
        while (input.remaining() >= 4) {
            out.putFloat(applyGainFloat(input.float, gain))
        }
    }

    private fun describe(encoding: Int): String = when (encoding) {
        C.ENCODING_PCM_16BIT -> "16-bit PCM"
        C.ENCODING_PCM_FLOAT -> "float PCM"
        else -> "encoding $encoding"
    }

    companion object {
        /**
         * Scales one 16-bit sample and clips it to the signed 16-bit range.
         * Internal so the unit tests can check the exact math.
         */
        internal fun applyGain16(sample: Short, gain: Float): Short {
            val scaled = (sample.toFloat() * gain).roundToInt()
            return scaled.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }

        /** Scales one float sample and clips it to the -1..1 range. */
        internal fun applyGainFloat(sample: Float, gain: Float): Float =
            (sample * gain).coerceIn(-1f, 1f)
    }
}
