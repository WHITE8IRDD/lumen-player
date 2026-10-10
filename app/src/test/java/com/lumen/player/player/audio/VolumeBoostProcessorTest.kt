package com.lumen.player.player.audio

import androidx.media3.common.C
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.util.UnstableApi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.ByteBuffer
import java.nio.ByteOrder

@UnstableApi
class VolumeBoostProcessorTest {

    private fun pcm16(vararg samples: Short): ByteBuffer {
        val buf = ByteBuffer.allocate(samples.size * 2).order(ByteOrder.LITTLE_ENDIAN)
        samples.forEach { buf.putShort(it) }
        buf.flip()
        return buf
    }

    private fun pcmFloat(vararg samples: Float): ByteBuffer {
        val buf = ByteBuffer.allocate(samples.size * 4).order(ByteOrder.LITTLE_ENDIAN)
        samples.forEach { buf.putFloat(it) }
        buf.flip()
        return buf
    }

    /** Runs one buffer through a configured processor and returns its output. */
    private fun run(
        boost: VolumeBoostController,
        encoding: Int,
        input: ByteBuffer
    ): ByteBuffer {
        val processor = VolumeBoostProcessor(boost)
        processor.configure(AudioProcessor.AudioFormat(44_100, 2, encoding))
        processor.flush()
        processor.queueInput(input)
        return processor.getOutput().order(ByteOrder.LITTLE_ENDIAN)
    }

    private fun shorts(buf: ByteBuffer): List<Short> {
        val out = mutableListOf<Short>()
        while (buf.remaining() >= 2) out.add(buf.short)
        return out
    }

    private fun floats(buf: ByteBuffer): List<Float> {
        val out = mutableListOf<Float>()
        while (buf.remaining() >= 4) out.add(buf.float)
        return out
    }

    /** A controller with the boost on and its level set; the gain is level cubed. */
    private fun boostAt(level: Float): VolumeBoostController {
        val boost = VolumeBoostController()
        boost.applySettings(enabled = true, maxPercent = 200)
        boost.setGain(level)
        return boost
    }

    // ── 16-bit PCM ──

    @Test
    fun levelOfOneCopiesSamplesUnchanged() {
        val out = run(boostAt(1f), C.ENCODING_PCM_16BIT,
            pcm16(1000, -1000, Short.MAX_VALUE, Short.MIN_VALUE))
        assertEquals(listOf<Short>(1000, -1000, Short.MAX_VALUE, Short.MIN_VALUE), shorts(out))
    }

    @Test
    fun levelOf150AppliesTheCubedGain() {
        // 1.5 cubed = 3.375 (exact in binary), as in VLC above 100 %.
        val out = run(boostAt(1.5f), C.ENCODING_PCM_16BIT, pcm16(1000, -1000, 5000, -5000))
        assertEquals(listOf<Short>(3375, -3375, 16875, -16875), shorts(out))
    }

    @Test
    fun fractionalGainRoundsToNearestSample() {
        // 1.2 cubed = 1.728: 1000 -> 1728, -2 -> -3.456 -> -3.
        val out = run(boostAt(1.2f), C.ENCODING_PCM_16BIT, pcm16(1000, -2))
        assertEquals(listOf<Short>(1728, -3), shorts(out))
    }

    @Test
    fun samplesAboveFullScaleAreClipped() {
        val out = run(boostAt(2f), C.ENCODING_PCM_16BIT, pcm16(20_000, -20_000))
        assertEquals(listOf<Short>(Short.MAX_VALUE, Short.MIN_VALUE), shorts(out))
    }

    @Test
    fun outputHasSameLengthAsInput() {
        val input = pcm16(1, 2, 3, 4, 5, 6)
        val out = run(boostAt(1.5f), C.ENCODING_PCM_16BIT, input)
        assertEquals(6, shorts(out).size)
    }

    // ── float PCM ──

    @Test
    fun floatGainScalesAndClipsToUnitRange() {
        // 1.2 cubed = 1.728: 0.25 -> 0.432; -0.8 and 0.9 clip to -1 and 1.
        val out = run(boostAt(1.2f), C.ENCODING_PCM_FLOAT, pcmFloat(0.25f, -0.8f, 0.9f))
        val result = floats(out)
        assertEquals(3, result.size)
        assertEquals(0.432f, result[0], 1e-4f)
        assertEquals(-1f, result[1], 1e-6f)
        assertEquals(1f, result[2], 1e-6f)
    }

    // ── engine state ──

    @Test
    fun supportedEncodingsAreActive() {
        val processor = VolumeBoostProcessor(VolumeBoostController())
        processor.configure(AudioProcessor.AudioFormat(48_000, 2, C.ENCODING_PCM_16BIT))
        assertTrue(processor.isActive)
        processor.configure(AudioProcessor.AudioFormat(48_000, 2, C.ENCODING_PCM_FLOAT))
        assertTrue(processor.isActive)
    }

    @Test
    fun unsupportedEncodingIsInactiveAndReported() {
        val boost = VolumeBoostController()
        val processor = VolumeBoostProcessor(boost)
        processor.configure(AudioProcessor.AudioFormat(48_000, 2, C.ENCODING_PCM_24BIT))
        assertFalse(processor.isActive)
        assertEquals(VolumeBoostController.EngineState.UNSUPPORTED, boost.engineState.value)
    }

    @Test
    fun supportedFormatIsReportedAsActive() {
        val boost = VolumeBoostController()
        val processor = VolumeBoostProcessor(boost)
        processor.configure(AudioProcessor.AudioFormat(44_100, 2, C.ENCODING_PCM_16BIT))
        assertEquals(VolumeBoostController.EngineState.ACTIVE, boost.engineState.value)
        assertEquals("16-bit PCM", boost.formatName.value)
    }
}
