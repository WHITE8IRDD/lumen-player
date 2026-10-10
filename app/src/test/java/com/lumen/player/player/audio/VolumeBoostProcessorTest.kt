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

    private fun boostAt(percent: Int): VolumeBoostController {
        val boost = VolumeBoostController()
        boost.applySettings(enabled = true, maxPercent = percent)
        boost.setGain(percent / 100f)
        return boost
    }

    // ── 16-bit PCM ──

    @Test
    fun gainOfOneCopiesSamplesUnchanged() {
        val out = run(boostAt(200).also { it.setGain(1f) }, C.ENCODING_PCM_16BIT,
            pcm16(1000, -1000, Short.MAX_VALUE, Short.MIN_VALUE))
        assertEquals(listOf<Short>(1000, -1000, Short.MAX_VALUE, Short.MIN_VALUE), shorts(out))
    }

    @Test
    fun gainOfTwoDoublesEachSample() {
        val out = run(boostAt(200), C.ENCODING_PCM_16BIT, pcm16(1000, -1000, 5000, -5000))
        assertEquals(listOf<Short>(2000, -2000, 10000, -10000), shorts(out))
    }

    @Test
    fun fractionalGainRoundsToNearestSample() {
        val out = run(boostAt(150), C.ENCODING_PCM_16BIT, pcm16(1000, -2))
        assertEquals(listOf<Short>(1500, -3), shorts(out))
    }

    @Test
    fun samplesAboveFullScaleAreClipped() {
        val out = run(boostAt(200), C.ENCODING_PCM_16BIT, pcm16(20_000, -20_000))
        assertEquals(listOf<Short>(Short.MAX_VALUE, Short.MIN_VALUE), shorts(out))
    }

    @Test
    fun outputHasSameLengthAsInput() {
        val input = pcm16(1, 2, 3, 4, 5, 6)
        val out = run(boostAt(200), C.ENCODING_PCM_16BIT, input)
        assertEquals(6, shorts(out).size)
    }

    // ── float PCM ──

    @Test
    fun floatGainScalesAndClipsToUnitRange() {
        val out = run(boostAt(200), C.ENCODING_PCM_FLOAT, pcmFloat(0.25f, -0.8f, 0.9f))
        val result = floats(out)
        assertEquals(3, result.size)
        assertEquals(0.5f, result[0], 1e-6f)
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
