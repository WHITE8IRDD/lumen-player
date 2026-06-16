package com.lumen.player.ui.subtitles

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import java.io.File
import androidx.lifecycle.viewModelScope
import com.lumen.player.data.remote.SubtitleApiService
import com.lumen.player.domain.model.SubtitleResult
import com.lumen.player.domain.usecase.DownloadSubtitleUseCase
import com.lumen.player.domain.usecase.SearchSubtitlesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

fun cleanTitleForSearch(raw: String): String {
    var name = raw.substringBeforeLast(".")
    val stopWords = setOf(
        "1080p", "720p", "4k", "2160p", "480p",
        "bluray", "brrip", "webrip", "web-dl", "hdtv",
        "x264", "x265", "hevc", "avc", "aac", "ac3",
        "yify", "yts", "rarbg", "ettv", "eztv",
        "ultimate", "cut", "extended", "theatrical",
        "remastered", "directors"
    )
    val parts = name.split(Regex("[._ ]"))
    val cleaned = mutableListOf<String>()
    for (part in parts) {
        if (part.matches(Regex("(19|20)\\d{2}"))) break
        if (part.lowercase() in stopWords) break
        if (part.matches(Regex("(?i)S\\d{2}E\\d{2}"))) break
        cleaned.add(part)
    }
    return cleaned.joinToString(" ").trim()
}

fun detectLocalSubtitles(videoPath: String?): List<File> {
    if (videoPath == null) return emptyList()
    val videoFile = File(videoPath)
    val folder = videoFile.parentFile ?: return emptyList()
    val subExtensions = setOf("srt", "vtt", "ass", "ssa", "sub")
    return folder.listFiles()
        ?.filter { it.isFile && it.extension.lowercase() in subExtensions }
        ?.sortedBy { it.name }
        ?: emptyList()
}

data class SubtitleDownloadState(
    val isLoading: Boolean = false,
    val results: List<SubtitleResult> = emptyList(),
    val error: String? = null,
    val isDownloading: Boolean = false,
    val downloadingId: String? = null,
    val downloadedIds: Set<String> = emptySet()
)

@HiltViewModel
class SubtitleViewModel @Inject constructor(
    private val searchSubtitlesUseCase: SearchSubtitlesUseCase,
    private val downloadSubtitleUseCase: DownloadSubtitleUseCase,
    private val apiService: SubtitleApiService
) : ViewModel() {

    private val _downloadState = MutableStateFlow(SubtitleDownloadState())
    val downloadState: StateFlow<SubtitleDownloadState> = _downloadState.asStateFlow()

    var onApplySubtitle: ((File) -> Unit)? = null

    fun searchByFile(file: File?, query: String, languages: String = "en") {
        viewModelScope.launch {
            Log.d("SubFix", "═══ searchByFile() ═══")
            Log.d("SubFix", "Query: $query, Languages: $languages")

            _downloadState.update { SubtitleDownloadState(isLoading = true) }
            val result = if (file != null && file.exists()) {
                searchSubtitlesUseCase.byFile(file, query, languages)
            } else {
                searchSubtitlesUseCase(query, languages)
            }
            result.fold(
                onSuccess = { results ->
                    Log.d("SubFix", "API returned ${results.size} raw results")
                    results.take(3).forEach { Log.d("SubFix", "Sample: ${it.releaseName} lang=${it.language}") }
                    val filtered = filterAndSortResults(results)
                    Log.d("SubFix", "After filter+sort: ${filtered.size} results")
                    _downloadState.update {
                        it.copy(
                            isLoading = false,
                            results = filtered,
                            error = if (filtered.isEmpty()) "No subtitles found. Try a different language or title." else null
                        )
                    }
                },
                onFailure = { error ->
                    _downloadState.update {
                        it.copy(
                            isLoading = false,
                            results = emptyList(),
                            error = "Search failed. ${error.message}"
                        )
                    }
                }
            )
        }
    }

    fun searchManual(name: String, season: String?, episode: String?, language: String, hearingImpaired: Boolean) {
        val query = buildString {
            append(name.trim())
            if (!season.isNullOrBlank()) append(" S${season.padStart(2, '0')}")
            if (!episode.isNullOrBlank()) append("E${episode.padStart(2, '0')}")
        }
        searchByFile(file = null, query = query, languages = language)
    }

    fun downloadAndApply(context: Context, result: SubtitleResult) {
        viewModelScope.launch {
            Log.d("SubFix", "═══ downloadAndApply START ═══")
            Log.d("SubFix", "URL: ${result.downloadUrl}")
            Log.d("SubFix", "ViewModel: Language=${result.language} Name=${result.releaseName}")
            Log.d("SubFix", "ViewModel: Callback set=${onApplySubtitle != null}")

            _downloadState.update { it.copy(downloadingId = result.downloadUrl) }

            val downloadResult = apiService.downloadSubtitle(context, result)

            downloadResult.fold(
                onSuccess = { file ->
                    Log.d("SubFix", "Download success: ${file.absolutePath}")
                    Log.d("SubFix", "About to invoke callback: ${file.name}")
                    Log.d("SubFix", "File exists: ${file.exists()}, size: ${file.length()}")

                    withContext(Dispatchers.Main) {
                        val callback = onApplySubtitle
                        if (callback == null) {
                            Log.e("SubFix", "FAIL: onApplySubtitle callback is NULL")
                            Toast.makeText(context, "Apply callback not set", Toast.LENGTH_SHORT).show()
                        } else {
                            Log.d("SubFix", "Invoking apply callback for: ${file.name}")
                            callback.invoke(file)
                        }

                        _downloadState.update {
                            it.copy(
                                downloadingId = null,
                                downloadedIds = it.downloadedIds + result.downloadUrl
                            )
                        }

                        Toast.makeText(context, "Subtitle applied", Toast.LENGTH_SHORT).show()
                    }
                },
                onFailure = { error ->
                    Log.e("SubFix", "Download failed", error)
                    withContext(Dispatchers.Main) {
                        _downloadState.update { it.copy(downloadingId = null) }
                        Toast.makeText(context, "Failed: ${error.message}", Toast.LENGTH_LONG).show()
                    }
                }
            )

            Log.d("SubFix", "═══ downloadAndApply END ═══")
        }
    }

    fun download(subtitle: SubtitleResult, outputDir: File) {
        viewModelScope.launch {
            _downloadState.update { it.copy(isDownloading = true, downloadingId = subtitle.downloadUrl, error = null) }
            val result = downloadSubtitleUseCase(subtitle, outputDir)
            result.fold(
                onSuccess = { path ->
                    _downloadState.update {
                        it.copy(
                            isDownloading = false,
                            downloadingId = null,
                            downloadedIds = it.downloadedIds + subtitle.downloadUrl
                        )
                    }
                },
                onFailure = { error ->
                    _downloadState.update {
                        it.copy(
                            isDownloading = false,
                            downloadingId = null,
                            error = error.message ?: "Download failed"
                        )
                    }
                }
            )
        }
    }

    fun resetDownload() {
        _downloadState.update { it.copy(isDownloading = false, downloadingId = null) }
    }

    private fun filterAndSortResults(rawResults: List<SubtitleResult>): List<SubtitleResult> {
        Log.d("SubFix", "Filtering ${rawResults.size} raw results")
        val filtered = rawResults.filter { result ->
            val name = result.releaseName?.trim() ?: ""
            if (name.isBlank()) { Log.d("SubFix", "Skipping: blank name"); return@filter false }
            if (name.all { it.isDigit() }) { Log.d("SubFix", "Skipping: pure digits"); return@filter false }
            true
        }
        Log.d("SubFix", "After garbage filter: ${filtered.size}")
        val deduped = filtered.distinctBy { (it.releaseName?.lowercase()?.trim() ?: "") + (it.downloadUrl) }
        Log.d("SubFix", "After dedupe: ${deduped.size}")
        val sorted = deduped.sortedByDescending { result ->
            val name = result.releaseName ?: ""; var score = name.length
            val nl = name.lowercase()
            if (nl.contains("bluray") || nl.contains("blu-ray")) score += 20
            if (nl.contains("1080p")) score += 15; if (nl.contains("720p")) score += 10
            if (nl.contains("web-dl") || nl.contains("webdl")) score += 10
            if (nl.contains("x264") || nl.contains("x265")) score += 5
            score
        }
        val finalList = sorted.take(50)
        Log.d("SubFix", "Final results: ${finalList.size}")
        return finalList
    }
}
