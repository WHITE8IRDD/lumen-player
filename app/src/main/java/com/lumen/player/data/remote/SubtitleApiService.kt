package com.lumen.player.data.remote

import android.content.Context
import android.util.Log
import com.lumen.player.BuildConfig
import com.lumen.player.domain.model.SubtitleResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Dns
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.File
import java.io.StringReader
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.URLEncoder
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubtitleApiService @Inject constructor() {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .dns(object : Dns {
            override fun lookup(hostname: String): List<InetAddress> {
                return try {
                    Dns.SYSTEM.lookup(hostname)
                } catch (e: Exception) {
                    Log.w("SubFix", "System DNS failed for $hostname, trying Google DNS fallback...")
                    try {
                        val addr = resolveDnsRaw(hostname)
                        Log.d("SubFix", "Google DNS resolved $hostname -> ${addr.hostAddress}")
                        listOf(addr)
                    } catch (e2: Exception) {
                        Log.e("SubFix", "Google DNS also failed for $hostname", e2)
                        throw e
                    }
                }
            }
        })
        .build()

    private var cachedToken: String? = null

    private fun resolveDnsRaw(hostname: String): InetAddress {
        val id = (System.currentTimeMillis() % 65535).toInt()
        val buf = java.io.ByteArrayOutputStream()
        buf.write((id shr 8) and 0xFF); buf.write(id and 0xFF)
        buf.write(0x01); buf.write(0x00)  // flags: recursion desired
        buf.write(0x00); buf.write(0x01)  // 1 question
        buf.write(0x00); buf.write(0x00); buf.write(0x00); buf.write(0x00); buf.write(0x00); buf.write(0x00)  // answer/ns/additional = 0
        hostname.split(".").forEach { part -> buf.write(part.length); buf.write(part.toByteArray()) }
        buf.write(0x00)  // end of hostname
        buf.write(0x00); buf.write(0x01)  // type A
        buf.write(0x00); buf.write(0x01)  // class IN
        val requestBytes = buf.toByteArray()
        val socket = DatagramSocket()
        socket.soTimeout = 5000
        socket.send(DatagramPacket(requestBytes, requestBytes.size, InetAddress.getByName("8.8.8.8"), 53))
        val responseBuf = ByteArray(512)
        val response = DatagramPacket(responseBuf, responseBuf.size)
        socket.receive(response)
        socket.close()
        val data = response.data
        if (data.size < 12) throw Exception("DNS response too short")
        val ansCount = ((data[6].toInt() and 0xFF) shl 8) or (data[7].toInt() and 0xFF)
        if (ansCount == 0) throw Exception("DNS no answer")
        var pos = 12
        while (pos < data.size) {
            val len = data[pos].toInt() and 0xFF
            if (len == 0) { pos++; break }
            pos += len + 1
        }
        pos += 4  // skip type + class
        if (pos + 10 > data.size) throw Exception("DNS response truncated")
        val ttl = ((data[pos].toInt() and 0xFF) shl 24) or ((data[pos+1].toInt() and 0xFF) shl 16) or
                  ((data[pos+2].toInt() and 0xFF) shl 8) or (data[pos+3].toInt() and 0xFF)
        pos += 6  // skip TTL (4 bytes) + rdlength (2 bytes)
        val ip = ((data[pos].toInt() and 0xFF)).toString() + "." +
                 ((data[pos+1].toInt() and 0xFF)).toString() + "." +
                 ((data[pos+2].toInt() and 0xFF)).toString() + "." +
                 ((data[pos+3].toInt() and 0xFF)).toString()
        Log.d("SubFix", "DNS raw TTL=$ttl IP=$ip")
        return InetAddress.getByName(ip)
    }

    init {
        Log.d("SubFix", "═══ SubtitleApiService init ═══")
        Log.d("SubFix", "BuildConfig.SUBDL_API_KEY length: ${BuildConfig.SUBDL_API_KEY.length}")
        Log.d("SubFix", "Key starts with: ${BuildConfig.SUBDL_API_KEY.take(15)}")
        if (BuildConfig.SUBDL_API_KEY.isBlank()) { Log.e("SubFix", "CRITICAL: SUBDL_API_KEY is BLANK") }
    }

    suspend fun search(
        title: String,
        languageCode: String
    ): Result<List<SubtitleResult>> {
        Log.d("SubFix", "═══ search() called ═══")
        Log.d("SubFix", "Title: '$title', Lang: '$languageCode'")
        val subdl = searchSubDL(title, languageCode)
        if (subdl.isSuccess) {
            val count = subdl.getOrNull()?.size ?: 0
            Log.d("SubFix", "SubDL returned $count results")
            if (count > 0) return subdl
        } else {
            val err = subdl.exceptionOrNull()
            Log.w("SubFix", "SubDL failed: ${err?.message}")
            // Return the network error immediately instead of silent fallback
            if (err != null && err.message?.contains("Unable to resolve host", ignoreCase = true) == true) {
                return Result.failure(Exception("Network unavailable. Check your internet connection."))
            }
        }
        Log.d("SubFix", "SubDL empty, trying OpenSubtitles")
        val os = searchOpenSubtitles(title, languageCode)
        Log.d("SubFix", "OpenSubtitles returned ${os.getOrNull()?.size ?: 0} results")
        return os
    }

    suspend fun searchByHash(
        fileHash: String,
        fileSize: Long,
        languageCode: String
    ): Result<List<SubtitleResult>> = withContext(Dispatchers.IO) {
        try {
            val token = cachedToken ?: loginAnonymously().getOrElse { return@withContext Result.failure(it) }
            cachedToken = token

            val langCode = mapLang(languageCode)
            val xmlRequest = "<?xml version=\"1.0\"?><methodCall><methodName>SearchSubtitles</methodName><params><param><value><string>$token</string></value></param><param><value><array><data><value><struct><member><name>sublanguageid</name><value><string>$langCode</string></value></member><member><name>moviehash</name><value><string>$fileHash</string></value></member><member><name>moviebytesize</name><value><double>$fileSize</double></value></member></struct></value></data></array></value></param></params></methodCall>"

            val request = Request.Builder()
                .url("https://api.opensubtitles.org/xml-rpc")
                .header("Content-Type", "text/xml")
                .header("User-Agent", "VLSub 0.10.2")
                .post(xmlRequest.toRequestBody("text/xml".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext Result.failure(Exception("OpenSubtitles HTTP ${response.code}"))
                val body = response.body?.string() ?: ""
                val results = parseSearchResponse(body)
                Result.success(results.map { it.copy(isHashMatch = true) })
            }
        } catch (e: Exception) {
            Log.e("SubtitleApi", "Hash search error", e)
            cachedToken = null
            Result.failure(e)
        }
    }

    private suspend fun searchSubDL(
        title: String,
        languageCode: String,
        season: Int? = null,
        episode: Int? = null
    ): Result<List<SubtitleResult>> = withContext(Dispatchers.IO) {
        Log.d("SubFix", "═══════════════════════════════════════")
        Log.d("SubFix", "═══ searchSubDL CALLED ═══")
        Log.d("SubFix", "Raw title input: '$title'")
        Log.d("SubFix", "Language input: '$languageCode'")
        Log.d("SubFix", "API key length: ${BuildConfig.SUBDL_API_KEY.length}")
        Log.d("SubFix", "API key first 10: ${BuildConfig.SUBDL_API_KEY.take(10)}")
        try {
            val langCode = when (languageCode.uppercase()) {
                "AR" -> "AR"
                "FR" -> "FR"
                "ES" -> "ES"
                else -> "EN"
            }
            val cleanedTitle = title
                .substringBeforeLast(".")
                .replace(Regex("[._]"), " ")
                .replace(Regex("\\[.*?]"), "")
                .replace(Regex("(?i)\\b(1080p|720p|480p|2160p|BluRay|BrRip|WEBRip|WEB-DL|HDRip|DVDRip|x264|x265|HEVC|AAC|YIFY|RARBG|UNRATED|UNCUT|EXTENDED|REPACK|PROPER)\\b"), "")
                .replace(Regex("\\b(19|20)\\d{2}\\b"), "")
                .replace(Regex("\\s+"), " ")
                .trim()
            val titleToSearch = if (cleanedTitle.length >= 3) cleanedTitle else title.split(".").take(2).joinToString(" ")
            val url = "https://api.subdl.com/api/v1/subtitles" +
                "?api_key=subdl_aZWowDbCBrAROUyXeyYvagjqNa6LHqcNCpBDaYTq7wg" +
                "&film_name=${URLEncoder.encode(titleToSearch, "UTF-8")}" +
                "&languages=$langCode" +
                "&subs_per_page=30"
            Log.d("SubFix", "FULL URL: $url")
            val request = Request.Builder().url(url).addHeader("User-Agent", "LumenPlayer/1.0").addHeader("Accept", "application/json").get().build()
            Log.d("SubFix", "Sending HTTP request...")
            val response = client.newCall(request).execute()
            val bodyStr = response.body?.string() ?: ""
            Log.d("SubFix", "HTTP code: ${response.code}")
            Log.d("SubFix", "Response length: ${bodyStr.length} bytes")
            Log.d("SubFix", "Response first 500 chars:")
            Log.d("SubFix", bodyStr.take(500))
            val json = try { JSONObject(bodyStr) } catch (e: Exception) { Log.e("SubFix", "FAIL: JSON parse error", e); Log.e("SubFix", "Body: $bodyStr"); return@withContext Result.failure(e) }
            val status = json.optBoolean("status", false)
            Log.d("SubFix", "JSON status field: $status")
            if (!status) { Log.w("SubFix", "API returned status=false: ${json.optString("error", "unknown")}"); return@withContext Result.success(emptyList()) }
            val subsArray = json.optJSONArray("subtitles")
            val resultsArray = json.optJSONArray("results")
            Log.d("SubFix", "subtitles array: ${subsArray?.length() ?: "NULL"}")
            Log.d("SubFix", "results array: ${resultsArray?.length() ?: "NULL"}")
            if (subsArray == null || subsArray.length() == 0) {
                Log.w("SubFix", "No subtitles in response")
                Log.w("SubFix", "FULL BODY: $bodyStr")
                return@withContext Result.success(emptyList())
            }
            Log.d("SubFix", "Parsing ${subsArray.length()} subtitle entries...")
            val results = mutableListOf<SubtitleResult>()
            for (i in 0 until subsArray.length()) {
                val sub = subsArray.getJSONObject(i)
                val releaseName = sub.optString("release_name", "")
                val langFull = sub.optString("lang", "")
                val langCode = sub.optString("language", "")
                val urlPath = sub.optString("url", "")
                val author = sub.optString("author", "")
                val hi = sub.optBoolean("hi", false)
                val name = sub.optString("name", "")
                Log.d("SubFix", "Entry $i: release='$releaseName', url='$urlPath'")
                if (urlPath.isBlank()) { Log.w("SubFix", "Entry $i: BLANK URL, skipping"); continue }
                val downloadUrl = if (urlPath.startsWith("http")) urlPath else "https://dl.subdl.com$urlPath"
                results.add(SubtitleResult(
                    releaseName = releaseName.ifBlank { name.ifBlank { "Unknown" } },
                    language = langCode.ifBlank { langFull }.uppercase(),
                    rating = null, downloadCount = null, uploadDate = null, format = "srt",
                    downloadUrl = downloadUrl, isZip = true, source = "SubDL", author = author.ifBlank { null }, isHearingImpaired = hi
                ))
            }
            Log.d("SubFix", "FINAL: parsed ${results.size} valid results")
            Log.d("SubFix", "═══ searchSubDL DONE ═══")
            Result.success(results)
        } catch (e: Exception) {
            Log.e("SubFix", "EXCEPTION in searchSubDL", e)
            Result.failure(e)
        }
    }

    private suspend fun searchOpenSubtitles(
        title: String,
        languageCode: String
    ): Result<List<SubtitleResult>> = withContext(Dispatchers.IO) {
        try {
            val token = cachedToken ?: loginAnonymously().getOrElse { return@withContext Result.failure(it) }
            cachedToken = token

            val cleaned = cleanTitle(title)
            val lang = mapLang(languageCode)
            val xmlRequest = "<?xml version=\"1.0\"?><methodCall><methodName>SearchSubtitles</methodName><params><param><value><string>$token</string></value></param><param><value><struct><member><name>sublanguageid</name><value><string>$lang</string></value></member><member><name>query</name><value><string>${escapeXml(cleaned)}</string></value></member></struct></value></param></params></methodCall>"

            val request = Request.Builder()
                .url("https://api.opensubtitles.org/xml-rpc")
                .header("Content-Type", "text/xml")
                .header("User-Agent", "VLSub 0.10.2")
                .post(xmlRequest.toRequestBody("text/xml".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext Result.failure(Exception("OpenSubtitles HTTP ${response.code}"))
                val body = response.body?.string() ?: ""
                Result.success(parseSearchResponse(body))
            }
        } catch (e: Exception) {
            Log.e("SubtitleApi", "OpenSubtitles error", e)
            cachedToken = null
            Result.failure(e)
        }
    }

    private suspend fun loginAnonymously(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val xml = "<?xml version=\"1.0\"?><methodCall><methodName>LogIn</methodName><params><param><value><string></string></value></param><param><value><string></string></value></param><param><value><string>en</string></value></param><param><value><string>VLSub 0.10.2</string></value></param></params></methodCall>"
            val request = Request.Builder()
                .url("https://api.opensubtitles.org/xml-rpc")
                .header("Content-Type", "text/xml")
                .header("User-Agent", "VLSub 0.10.2")
                .post(xml.toRequestBody("text/xml".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                val token = parseToken(body)
                if (token.isBlank()) Result.failure(Exception("Login failed"))
                else Result.success(token)
            }
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun downloadSubtitle(
        downloadUrl: String,
        destDir: java.io.File,
        fileName: String
    ): String? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(downloadUrl).header("User-Agent", "LumenPlayer/1.0").build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                val bytes = response.body?.bytes() ?: return@withContext null
                destDir.mkdirs()
                val outFile = java.io.File(destDir, fileName)
                when {
                    bytes.size >= 2 && bytes[0] == 0x1F.toByte() && bytes[1] == 0x8B.toByte() -> {
                        java.util.zip.GZIPInputStream(bytes.inputStream()).use { gz ->
                            outFile.outputStream().use { gz.copyTo(it) }
                        }
                    }
                    bytes.size >= 4 && bytes[0] == 0x50.toByte() && bytes[1] == 0x4b.toByte() -> {
                        extractZip(bytes, outFile)
                    }
                    else -> {
                        outFile.writeBytes(bytes)
                    }
                }
                outFile.absolutePath
            }
        } catch (e: Exception) { Log.e("SubtitleApi", "Download error", e); null }
    }

    suspend fun downloadSubtitle(
        context: Context,
        result: SubtitleResult
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            Log.d("SubFix", "Downloading from: ${result.downloadUrl}")

            val request = Request.Builder()
                .url(result.downloadUrl)
                .addHeader("User-Agent", "LumenPlayer/1.0")
                .build()

            val response = client.newCall(request).execute()
            Log.d("SubFix", "Download HTTP: ${response.code}")

            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("HTTP ${response.code}"))
            }

            val bytes = response.body?.bytes()
                ?: return@withContext Result.failure(Exception("Empty body"))

            Log.d("SubFix", "Downloaded ${bytes.size} bytes")

            val subsDir = File(context.getExternalFilesDir(null), "subtitles")
            subsDir.mkdirs()

            val safeName = (result.releaseName ?: "subtitle")
                .replace(Regex("[^a-zA-Z0-9._\\- ]"), "_").take(100)

            if (bytes.size >= 4 && bytes[0] == 0x50.toByte() && bytes[1] == 0x4b.toByte()) {
                val outFile = File(subsDir, "$safeName.srt")
                extractZip(bytes, outFile)
                if (outFile.exists() && outFile.length() > 0) {
                    Log.d("SubFix", "Extracted: ${outFile.absolutePath}")
                    return@withContext Result.success(outFile)
                }
            }
            if (bytes.size >= 2 && bytes[0] == 0x1F.toByte() && bytes[1] == 0x8B.toByte()) {
                val outFile = File(subsDir, "$safeName.srt")
                java.util.zip.GZIPInputStream(bytes.inputStream()).use { gz ->
                    outFile.outputStream().use { gz.copyTo(it) }
                }
                if (outFile.exists() && outFile.length() > 0) {
                    Log.d("SubFix", "Extracted gzip: ${outFile.absolutePath}")
                    return@withContext Result.success(outFile)
                }
            }

            val rawFile = File(subsDir, "$safeName.srt")
            rawFile.writeBytes(bytes)
            Log.d("SubFix", "Saved raw: ${rawFile.absolutePath}")
            Result.success(rawFile)
        } catch (e: Exception) {
            Log.e("SubFix", "Download exception", e)
            Result.failure(e)
        }
    }

    private fun extractZip(input: ByteArray, output: java.io.File) {
        java.util.zip.ZipInputStream(input.inputStream()).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                if (!entry.isDirectory && entry.name.lowercase().let { it.endsWith(".srt") || it.endsWith(".vtt") }) {
                    output.outputStream().use { zip.copyTo(it) }
                    zip.closeEntry(); return
                }
                zip.closeEntry(); entry = zip.nextEntry
            }
        }
    }

    private fun parseToken(xml: String): String {
        try {
            val parser = XmlPullParserFactory.newInstance().newPullParser()
            parser.setInput(StringReader(xml))
            var event = parser.eventType
            var inToken = false
            while (event != XmlPullParser.END_DOCUMENT) {
                if (event == XmlPullParser.START_TAG && parser.name == "name") { parser.next(); if (parser.text == "token") inToken = true }
                if (inToken && event == XmlPullParser.START_TAG && parser.name == "string") { parser.next(); return parser.text ?: "" }
                event = parser.next()
            }
        } catch (e: Exception) { }
        return ""
    }

    private fun parseSearchResponse(xml: String): List<SubtitleResult> {
        val results = mutableListOf<SubtitleResult>()
        try {
            val parser = XmlPullParserFactory.newInstance().newPullParser()
            parser.setInput(StringReader(xml))
            var struct = mutableMapOf<String, String>()
            var name: String? = null
            var inStruct = false
            var event = parser.eventType
            while (event != XmlPullParser.END_DOCUMENT) {
                when (event) {
                    XmlPullParser.START_TAG -> {
                        when (parser.name) {
                            "struct" -> { inStruct = true; struct = mutableMapOf() }
                            "name" -> { parser.next(); name = parser.text }
                            "string", "int", "double" -> { if (inStruct && name != null) { parser.next(); struct[name!!] = parser.text ?: "" } }
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (parser.name == "struct" && inStruct && struct.containsKey("SubFileName")) {
                            results.add(SubtitleResult(
                                releaseName = struct["SubFileName"],
                                language = struct["SubLanguageID"]?.uppercase(),
                                rating = struct["SubRating"],
                                downloadCount = struct["SubDownloadsCnt"]?.toIntOrNull(),
                                uploadDate = struct["SubAddDate"],
                                format = struct["SubFormat"] ?: "srt",
                                downloadUrl = struct["SubDownloadLink"] ?: "",
                                isZip = false,
                                source = "OpenSubtitles"
                            ))
                            inStruct = false
                        }
                    }
                }
                event = parser.next()
            }
        } catch (e: Exception) { }
        return results
    }

    private fun mapLang(code2: String): String = when (code2.lowercase()) {
        "en" -> "eng"; "ar" -> "ara"; "fr" -> "fre"; "es" -> "spa"
        "pt" -> "por"; "de" -> "ger"; "it" -> "ita"; "tr" -> "tur"
        "ja" -> "jpn"; "ko" -> "kor"; "zh" -> "chi"; else -> "eng"
    }

    private fun escapeXml(s: String) = s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")
    private fun cleanTitle(raw: String): String {
        if (raw.isBlank()) return ""
        Log.d("SubFix", "cleanTitle INPUT: '$raw'")
        var result = raw
        result = result.substringBeforeLast(".")
        Log.d("SubFix", "After ext removal: '$result'")
        result = result.replace(Regex("\\[.*?]"), "").replace(Regex("\\(.*?\\)"), "")
        Log.d("SubFix", "After brackets removal: '$result'")
        result = result.replace(Regex("[._]"), " ")
        Log.d("SubFix", "After separator replacement: '$result'")
        val tags = listOf("1080p", "720p", "480p", "2160p", "4K", "UHD", "BluRay", "BrRip", "BDRip", "WEBRip", "WEB-DL", "HDRip", "DVDRip", "HDTV", "PDTV", "x264", "x265", "H264", "H265", "HEVC", "AVC", "AAC", "AC3", "DTS", "MP3", "AAC5", "5", "1", "YIFY", "RARBG", "FGT", "EVO", "ETRG", "YTS", "MX", "REMUX", "PROPER", "REPACK", "EXTENDED", "UNRATED", "UNCUT", "AND", "DC", "Directors", "Cut")
        tags.forEach { tag -> result = result.replace(Regex("(?i)\\b$tag\\b"), "") }
        Log.d("SubFix", "After tags removal: '$result'")
        result = result.replace(Regex("\\b(19|20)\\d{2}\\b"), "")
        Log.d("SubFix", "After year removal: '$result'")
        result = result.replace(Regex("[-\\s]+$"), "")
        result = result.replace(Regex("\\s+"), " ").trim()
        Log.d("SubFix", "cleanTitle OUTPUT: '$result'")
        if (result.isBlank() || result.length < 3) {
            val fallback = raw.substringBeforeLast(".").replace(Regex("[._]"), " ").split(" ").take(3).joinToString(" ")
            Log.w("SubFix", "Cleaning too aggressive, using fallback: '$fallback'")
            return fallback
        }
        return result
    }
}
