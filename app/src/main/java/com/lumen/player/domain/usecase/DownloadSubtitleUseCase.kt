package com.lumen.player.domain.usecase

import com.lumen.player.domain.model.SubtitleResult
import com.lumen.player.domain.repository.SubtitleRepository
import java.io.File
import javax.inject.Inject

class DownloadSubtitleUseCase @Inject constructor(
    private val repository: SubtitleRepository
) {
    suspend operator fun invoke(
        subtitle: SubtitleResult,
        outputDir: File
    ): Result<String> {
        return repository.download(subtitle, outputDir)
    }
}
