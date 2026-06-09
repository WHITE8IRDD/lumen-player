package com.lumen.player.ui.subtitles

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.RandomAccessFile
import java.nio.ByteOrder

class SubtitleHashTest {

    @get:Rule val tempFolder = TemporaryFolder()

    @Test
    fun hashFile_emptyFile() {
        val f = tempFolder.newFile("empty.bin")
        val hash = SubtitleHash.hashFile(f.absolutePath)
        assertEquals(16, hash.length)
        assertEquals("0000000000000000", hash)
    }

    @Test
    fun hashFile_singleByte() {
        val f = tempFolder.newFile("single.bin")
        f.writeBytes(byteArrayOf(0x41))
        val hash = SubtitleHash.hashFile(f.absolutePath)
        assertEquals(16, hash.length)
    }

    @Test
    fun hashFile_knownValue() {
        // Create a file with known content and verify the hash is consistent
        val f = tempFolder.newFile("known.bin")
        val data = ByteArray(65536)
        for (i in data.indices) data[i] = (i and 0xFF).toByte()
        f.writeBytes(data)
        val hash1 = SubtitleHash.hashFile(f.absolutePath)
        val hash2 = SubtitleHash.hashFile(f.absolutePath)
        assertEquals(hash1, hash2)
    }

    @Test
    fun hashFile_largeFile() {
        val f = tempFolder.newFile("large.bin")
        val data = ByteArray(200_000)
        for (i in data.indices) data[i] = (i * 31 and 0xFF).toByte()
        f.writeBytes(data)
        val hash = SubtitleHash.hashFile(f.absolutePath)
        assertEquals(16, hash.length)
        assertNotNull(hash)
    }
}
