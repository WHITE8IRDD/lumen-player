package com.lumen.player.domain.repository

import com.lumen.player.domain.model.SubtitleResult
import java.io.File

interface SubtitleRepository {

    suspend fun search(
        query: String,
        languages: String = "en"
    ): Result<List<SubtitleResult>>

    suspend fun searchByFile(
        file: File,
        title: String,
        languages: String = "en"
    ): Result<List<SubtitleResult>>

    suspend fun download(
        subtitle: SubtitleResult,
        outputDir: File
    ): Result<String>
}
