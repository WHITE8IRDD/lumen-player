package com.lumen.player.domain.usecase

import com.lumen.player.domain.model.SubtitleResult
import com.lumen.player.domain.repository.SubtitleRepository
import javax.inject.Inject

import java.io.File

class SearchSubtitlesUseCase @Inject constructor(
    private val repository: SubtitleRepository
) {
    suspend operator fun invoke(
        query: String,
        languages: String = "en"
    ): Result<List<SubtitleResult>> {
        if (query.isBlank()) return Result.failure(Exception("Search query is empty"))
        return repository.search(query, languages)
    }

    suspend fun byFile(
        file: File,
        title: String,
        languages: String = "en"
    ): Result<List<SubtitleResult>> {
        return repository.searchByFile(file, title, languages)
    }
}
