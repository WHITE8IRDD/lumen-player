package com.lumen.player.data.repository

import com.lumen.player.data.remote.SubtitleApiService
import com.lumen.player.domain.model.SubtitleResult
import com.lumen.player.domain.repository.SubtitleRepository
import com.lumen.player.ui.subtitles.SubtitleHash
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubtitleRepositoryImpl @Inject constructor(
    private val apiService: SubtitleApiService
) : SubtitleRepository {

    override suspend fun search(
        query: String,
        languages: String
    ): Result<List<SubtitleResult>> = apiService.search(query, languages)

    override suspend fun searchByFile(
        file: File,
        title: String,
        languages: String
    ): Result<List<SubtitleResult>> {
        // For file-based search, we need Context for SubtitleHash.
        // The hash is optional - we try it but fall back to title search.
        return try {
            val hash = SubtitleHash.hashFile(file.absolutePath)
            val hashResult = apiService.searchByHash(hash, file.length(), languages)
            if (hashResult.isSuccess && hashResult.getOrNull()?.isNotEmpty() == true) {
                return hashResult
            }
            apiService.search(title, languages)
        } catch (e: Exception) {
            apiService.search(title, languages)
        }
    }

    override suspend fun download(
        subtitle: SubtitleResult,
        outputDir: File
    ): Result<String> {
        val fileName = subtitle.fileName ?: "${System.currentTimeMillis()}_${subtitle.releaseName ?: "subtitle"}.${subtitle.format}"
        val path = apiService.downloadSubtitle(subtitle.downloadUrl, outputDir, fileName)
        return if (path != null) Result.success(path)
        else Result.failure(Exception("Download failed"))
    }
}
