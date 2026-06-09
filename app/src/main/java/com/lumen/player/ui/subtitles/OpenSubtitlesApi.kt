package com.lumen.player.ui.subtitles

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit
import java.util.zip.ZipInputStream

data class SubtitleSearchResult(
    val fileId: Int,
    val fileName: String,
    val language: String,
    val downloads: Int,
    val rating: Float,
    val releaseName: String?
)

data class SubtitleInfo(
    val fileId: Int,
    val fileName: String,
    val language: String,
    val subHd: String? = null
)

class OpenSubtitlesApi(
    private val apiKey: String = "",
    private val userAgent: String = "LumenPlayer v1.0"
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private var cachedToken: String? = null
    private val tokenMutex = kotlinx.coroutines.sync.Mutex()

    companion object {
        private const val BASE_URL = "https://api.opensubtitles.com/api/v1"
    }

    private suspend fun ensureToken(): Result<String> {
        if (apiKey.isBlank()) return Result.failure(IllegalStateException("OpenSubtitles API key not configured"))
        tokenMutex.lock()
        try {
            if (cachedToken != null) return Result.success(cachedToken!!)
            return withContext(Dispatchers.IO) {
                try {
                    val body = "{}".toRequestBody("application/json".toMediaType())
                    val builder = Request.Builder()
                        .url("$BASE_URL/login")
                        .post(body)
                        .header("User-Agent", userAgent)
                        .header("Accept", "application/json")
                    builder.header("Api-Key", apiKey)
                    val response = client.newCall(builder.build()).execute()
                    val bodyStr = response.body?.string().orEmpty()
                    val json = JSONObject(bodyStr)
                    val token = json.optString("token").takeIf { it.isNotBlank() }
                    if (token != null) {
                        cachedToken = token
                        Result.success(token)
                    } else {
                        android.util.Log.w("OpenSubtitlesApi", "guestLogin failed (${response.code}): $bodyStr")
                        Result.failure(Exception("Login failed: ${response.code}"))
                    }
                } catch (e: Exception) {
                    android.util.Log.e("OpenSubtitlesApi", "guestLogin error", e)
                    Result.failure(e)
                }
            }
        } finally {
            tokenMutex.unlock()
        }
    }

    fun invalidateToken() {
        cachedToken = null
    }

    /**
     * Search for subtitles by movie hash and/or query.
     */
    suspend fun search(
        query: String? = null,
        movieHash: String? = null,
        languages: String = "en"
    ): Result<List<SubtitleSearchResult>> = withContext(Dispatchers.IO) {
        val tokenResult = ensureToken()
        if (tokenResult.isFailure) return@withContext Result.failure(tokenResult.exceptionOrNull()!!)
        val token = tokenResult.getOrThrow()
        try {
            val url = buildString {
                append("$BASE_URL/subtitles?languages=$languages")
                if (!query.isNullOrBlank()) append("&query=${java.net.URLEncoder.encode(query, "UTF-8")}")
                if (!movieHash.isNullOrBlank()) append("&moviehash=$movieHash")
                append("&order_by=downloads&order_direction=desc&limit=20")
            }
            val reqBuilder = Request.Builder()
                .url(url)
                .header("User-Agent", userAgent)
                .header("Accept", "application/json")
                .header("Api-Key", apiKey)
                .header("Authorization", "Bearer $token")

            val response = client.newCall(reqBuilder.build()).execute()
            val body = response.body?.string().orEmpty()

            if (response.code == 401) { invalidateToken(); return@withContext Result.failure(Exception("Unauthorized")) }
            if (!response.isSuccessful) {
                android.util.Log.w("OpenSubtitlesApi",
                    "search returned ${response.code}: $body")
                return@withContext Result.failure(Exception("Search failed: ${response.code}"))
            }
            val json = JSONObject(body)
            val data = json.optJSONArray("data") ?: JSONArray()
            val results = (0 until data.length()).map { i ->
                val item = data.getJSONObject(i)
                val attributes = item.optJSONObject("attributes") ?: JSONObject()
                val files = attributes.optJSONArray("files") ?: JSONArray()
                val firstFile = if (files.length() > 0) files.getJSONObject(0) else JSONObject()
                SubtitleSearchResult(
                    fileId = firstFile.optInt("file_id", 0),
                    fileName = attributes.optString("release", attributes.optString("file_name", "Unknown")),
                    language = attributes.optString("language", "en"),
                    downloads = attributes.optInt("download_count", 0),
                    rating = attributes.optDouble("ratings", 0.0).toFloat(),
                    releaseName = attributes.optString("release").takeIf { it.isNotEmpty() }
                )
            }
            Result.success(results)
        } catch (e: Exception) {
            android.util.Log.e("OpenSubtitlesApi", "search error", e)
            Result.failure(e)
        }
    }

    /**
     * Download subtitle file by file_id.
     * Returns the downloaded .srt/.ass/.vtt file path, or null on failure.
     */
    suspend fun download(
        fileId: Int,
        outputDir: File
    ): String? = withContext(Dispatchers.IO) {
        val tokenResult = ensureToken()
        if (tokenResult.isFailure) return@withContext null
        val token = tokenResult.getOrThrow()
        try {
            val body = """{"file_id":$fileId}""".toRequestBody("application/json".toMediaType())
            val reqBuilder = Request.Builder()
                .url("$BASE_URL/download")
                .post(body)
                .header("User-Agent", userAgent)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .header("Api-Key", apiKey)
                .header("Authorization", "Bearer $token")

            val downloadResponse = client.newCall(reqBuilder.build()).execute()
            val downloadBody = downloadResponse.body?.string().orEmpty()
            if (downloadResponse.code == 401) { invalidateToken(); return@withContext null }
            if (!downloadResponse.isSuccessful) {
                android.util.Log.w("OpenSubtitlesApi",
                    "download returned ${downloadResponse.code}: $downloadBody")
                return@withContext null
            }
            val downloadJson = JSONObject(downloadBody)
            val link = downloadJson.optString("link") ?: return@withContext null
            if (link.isBlank()) return@withContext null

            // Fetch the actual subtitle file
            val fileReq = Request.Builder()
                .url(link)
                .header("User-Agent", userAgent)
                .build()
            val fileResponse = client.newCall(fileReq).execute()
            val bytes = fileResponse.body?.bytes() ?: return@withContext null

            outputDir.mkdirs()
            val fileName = downloadJson.optString("file_name", "subtitle_$fileId.srt")
            val ct = fileResponse.header("Content-Type", "")
            val isZip = fileName.endsWith(".zip") || ct?.contains("zip") == true

            if (isZip) {
                extractFromZip(bytes, outputDir, fileId)
            } else {
                val ext = guessExtension(bytes)
                val outFile = File(outputDir, "subtitle_${fileId}_downloaded.$ext")
                FileOutputStream(outFile).use { it.write(bytes) }
                outFile.absolutePath
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun extractFromZip(zipBytes: ByteArray, outputDir: File, fileId: Int): String? {
        try {
            val zis = ZipInputStream(zipBytes.inputStream())
            var entry = zis.nextEntry
            while (entry != null) {
                if (!entry.isDirectory && entry.name.let { n ->
                        n.endsWith(".srt") || n.endsWith(".ass") || n.endsWith(".ssa") || n.endsWith(".vtt")
                    }) {
                    val outFile = File(outputDir, "subtitle_${fileId}_${entry.name}")
                    FileOutputStream(outFile).use { fos ->
                        zis.copyTo(fos)
                    }
                    zis.closeEntry()
                    zis.close()
                    return outFile.absolutePath
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
            zis.close()
        } catch (_: Exception) { }
        return null
    }

    private fun guessExtension(data: ByteArray): String {
        val header = data.take(20).toByteArray()
        val text = header.toString(Charsets.UTF_8)
        return when {
            text.startsWith("1\n") || text.startsWith("1\r\n") || Regex("""^\d+\s*\n""").containsMatchIn(text) -> "srt"
            text.startsWith("[Script Info]") || text.contains("[Events]") -> "ass"
            text.startsWith("{\\rtf") -> "ass"
            text.contains("WEBVTT") -> "vtt"
            else -> "srt"
        }
    }
}
