package com.lumen.player.player.audio

import androidx.media3.common.C
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.audio.BaseAudioProcessor
import androidx.media3.common.util.UnstableApi
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.roundToInt

/**
 * Multiplies decoded PCM samples by the current [VolumeBoostController] gain.
 *
 * It sits in the ExoPlayer audio pipeline, so it affects every renderer the
 * same way. At gain 1.0 the buffer is copied through unchanged, so there is
 * no audible difference below 100 %.
 *
 * Supports 16-bit integer PCM and float PCM. Other encodings are left alone
 * (the processor reports itself as inactive for them).
 */
@UnstableApi
class VolumeBoostProcessor(
    private val boost: VolumeBoostController
) : BaseAudioProcessor() {

    override fun onConfigure(inputAudioFormat: AudioProcessor.AudioFormat): AudioProcessor.AudioFormat =
        when (inputAudioFormat.encoding) {
            C.ENCODING_PCM_16BIT, C.ENCODING_PCM_FLOAT -> inputAudioFormat
            else -> AudioProcessor.AudioFormat.NOT_SET
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
            val sample = input.short.toFloat() * gain
            val clipped = sample.roundToInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
            out.putShort(clipped.toShort())
        }
    }

    private fun boostFloat(input: ByteBuffer, out: ByteBuffer, gain: Float) {
        while (input.remaining() >= 4) {
            val sample = input.float * gain
            out.putFloat(sample.coerceIn(-1f, 1f))
        }
    }
}
