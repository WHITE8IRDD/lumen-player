package com.lumen.player.ui.subtitles

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OpenSubtitlesApiTest {

    @Test
    fun `empty API key returns failure`() {
        val api = OpenSubtitlesApi(apiKey = "")
        val result = runBlocking { api.search(query = "test") }
        assertTrue(result.isFailure)
        assertEquals("OpenSubtitles API key not configured", result.exceptionOrNull()?.message)
    }
}
