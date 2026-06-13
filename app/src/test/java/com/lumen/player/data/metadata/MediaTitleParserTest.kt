package com.lumen.player.data.metadata

import kotlinx.coroutines.runBlocking
import org.junit.Test

class MediaTitleParserTest {
    private val parser = MediaTitleParser()

    @Test
    fun resolutionExtraction() = runBlocking {
        val cases = mapOf(
            "Movie.2024.1080p.x264.mkv" to "1080P",
            "Show.S01E01.2160p.x265.mkv" to "4K",
            "Video.2024.4K.BluRay.x264.mkv" to "4K",
            "Film.2024.UHD.x264.mkv" to "4K",
            "No.Reso.File.mkv" to null,
            "Double.1080p.2160p.mkv" to "1080P"
        )
        for ((filename, expected) in cases) {
            val r = parser.resolve(filename)
            val actual = r.resolution
            assert(actual == expected) { "Expected resolution=$expected for '$filename' but got $actual" }
        }
    }

    @Test
    fun parseFiveExamples() = runBlocking {
        val cases = listOf(
            "[Witanime.com] YPNKKGINKS EP 01 FHD",
            "Blue.Mountain.State.2010.S01E01.1080p.x265-Panda.mkv",
            "Talladega.Nights.The.Ballad.Of.Ricky.Bobby.2006.UNRATED.1080p.BluRay.x264-[YTS.AG].mp4",
            "Avengers.Endgame.2019.BluRay.1080p.x264.mkv",
            "Black.Doves.S01E03.1080p.x265-ELiTE.mkv"
        )
        for (filename in cases) {
            val r = parser.resolve(filename)
            println("$filename")
            println("  cleanTitle=${r.cleanTitle}")
            println("  year=${r.year}")
            println("  seasonEpisode=${r.seasonEpisode}")
            println()
        }
    }
}
