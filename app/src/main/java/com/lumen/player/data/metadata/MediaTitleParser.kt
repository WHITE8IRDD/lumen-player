package com.lumen.player.data.metadata

import com.lumen.player.domain.model.MediaMetadata
import com.lumen.player.domain.repository.MetadataResolver
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import java.io.File

fun friendlyPath(path: String?): String {
    if (path.isNullOrBlank()) return "Internal Storage"
    val normalized = path.trimEnd('/')
    val internalPrefixes = listOf(
        "/storage/emulated/0",
        "/sdcard",
        "/mnt/sdcard",
        "/data/media/0"
    )
    val sdCardPattern = Regex("^/storage/[^/]+\\d.*$")
    for (prefix in internalPrefixes) {
        if (normalized.startsWith(prefix)) {
            val suffix = normalized.removePrefix(prefix).trimStart('/')
            return if (suffix.isEmpty()) "Internal Storage" else suffix
        }
    }
    if (sdCardPattern.matches(normalized)) {
        val parts = normalized.split("/")
        if (parts.size >= 4) {
            val afterStorage = parts.drop(2).joinToString("/")
            return "SD Card/$afterStorage"
        }
        return "SD Card"
    }
    return "Internal Storage"
}

fun friendlyParentFolder(path: String?): String {
    if (path.isNullOrBlank()) return "Internal Storage"
    val file = File(path.trimEnd('/'))
    val parent = file.parentFile ?: return friendlyPath(path)
    val segments = parent.path.split(Regex("/+")).filter { it.isNotEmpty() }
    if (segments.size <= 2) return friendlyPath(parent.path)
    val lastTwo = segments.takeLast(2).joinToString("/")
    return if (segments.first() == "storage" && segments.size >= 3) {
        if (segments[1] == "emulated" && segments[2] == "0") {
            val suffix = segments.drop(3)
            if (suffix.size <= 2) suffix.joinToString("/")
            else suffix.takeLast(2).joinToString("/")
        } else {
            lastTwo
        }
    } else lastTwo
}

@Singleton
class MediaTitleParser @Inject constructor() : MetadataResolver {

    private val knownExtensions = setOf(
        "mp4", "mkv", "avi", "mov", "wmv", "flv", "webm", "m4v", "mpg", "mpeg",
        "3gp", "ts", "m2ts", "vob", "ogv",
        "mp3", "flac", "wav", "aac", "ogg", "wma", "m4a", "opus", "alac"
    )

    private val bracketPattern = Regex("\\[.*?\\]")
    private val tagParenPattern = Regex("\\([^)]*?(?:1080p|720p|2160p|480p|360p|4K|x264|x265|HEVC|AVC|XviD|DivX|BluRay|WEBRip|WEB-DL|HDRip|DVDRip|BrRip|HDTV|YTS|ELiTE|Panda|RARBG|ROVERS|JVT|AMCORD|IMAX|EVO|SPARKS|FGT|UNCUT|UNRATED|EXTENDED|REMASTERED|RESTORED|RECUT|REDUX|REPACK|PROPER|INTERNAL|LIMITED|COMPLETE|AAC5\\.1|DD5\\.1|DDP5\\.1|EAC3|AC3|DTS|DTS-HD|TrueHD|Atmos|FLAC|MP3|OGG|OPUS|WAV|AMZN|NF|DSNP|HMAX|ATVP|CAM|TS|TC|R5|SCREENER|10bit|8bit|HDR|HDR10|DolbyVision|DV).*?\\)", RegexOption.IGNORE_CASE)
    private val qualityPattern = Regex("(?i)\\b(?:1080p|720p|2160p|480p|360p|4K|8K|UHD|UHDR|2K|HD|SD|x264|x265|h264|h265|HEVC|AVC|XviD|DivX|BluRay|WEBRip|WEB-DL|WEB DL|WEBDL|HDRip|DVDRip|BrRip|HDTV|HDTVRip|UNRATED|UNCUT|EXTENDED|REMASTERED|RESTORED|RECUT|REDUX|REPACK|PROPER|INTERNAL|LIMITED|COMPLETE|FHD|HDR|HDR10|DolbyVision|Dolby Vision|DV|10bit|8bit|YTS|YIFY|RARBG|ELiTE|Panda|ROVERS|JVT|AMCORD|IMAX|EVO|SPARKS|FGT|GECKOS|EZTV|ETRG|AMZN|NF|DSNP|HMAX|ATVP|PMTP|CAM|TS|TC|R5|SCREENER|DSR|PDTV|DVD|BD|AAC5\\.1|AAC 5\\.1|DD5\\.1|DDP5\\.1|DD\\+5\\.1|EAC3|E-AC3|AC3|AC-3|DTS|DTS-HD|DTS-X|DTSHD|TrueHD|True HD|Atmos|Dolby Atmos|DolbyDigital|FLAC|MP3|OGG|OPUS|WAV|5\\.1|7\\.1|2\\.0|2\\.1|6CH|2CH|8CH)\\b")
    private val groupSuffixPattern = Regex("-[a-zA-Z0-9]+(?:\\.[a-zA-Z0-9]+)*$")
    private val separatorPattern = Regex("[\\._]")
    private val spacePattern = Regex("\\s+")
    private val yearPattern = Regex("\\b(19\\d{2}|20\\d{2})\\b")
    private val sePattern = Regex("\\bS(\\d{1,2})E(\\d{1,2})\\b", RegexOption.IGNORE_CASE)
    private val episodePattern = Regex("\\b(?:EP|EPISODE|Episode)\\s*(\\d{1,3})\\b", RegexOption.IGNORE_CASE)
    private val dashPattern = Regex("\\s*[-–—]+\\s*")
    private val trailingPunct = Regex("[,\\s]+$")
    private val leadingPunct = Regex("^[,\\s]+")
    private val trailingStopwordPattern = Regex("\\s+(?:and|&|the|or|for)\\s*$", RegexOption.IGNORE_CASE)

    private val stopwords = setOf(
        "of", "the", "a", "an", "and", "or", "but", "for",
        "in", "on", "at", "to", "by", "with", "vs", "from",
        "as", "into", "onto", "upon"
    )

    override suspend fun resolve(rawFilename: String): MediaMetadata {
        var s = rawFilename.trim()

        // Strip file extension — only if last segment is a known media extension
        val lastDotIndex = s.lastIndexOf('.')
        if (lastDotIndex > 0) {
            val possibleExt = s.substring(lastDotIndex + 1).lowercase(Locale.getDefault())
            if (possibleExt in knownExtensions) {
                s = s.substring(0, lastDotIndex)
            }
        }

        s = bracketPattern.replace(s, "")
        s = tagParenPattern.replace(s, "")
        s = qualityPattern.replace(s, "")
        s = groupSuffixPattern.replace(s, "")
        s = separatorPattern.replace(s, " ")
        s = spacePattern.replace(s, " ").trim()
        s = trailingStopwordPattern.replace(s, "")

        // Extract year
        val yearMatch = yearPattern.find(s)
        val year = yearMatch?.value?.toIntOrNull()
        if (year != null) {
            s = yearPattern.replaceFirst(s, "").trim()
        }

        // Extract season/episode (SxxExx format)
        val seMatch = sePattern.find(s)
        val seasonEpisode = seMatch?.let {
            "S${it.groupValues[1].padStart(2, '0')}E${it.groupValues[2].padStart(2, '0')}"
        }
        if (seasonEpisode != null) {
            s = sePattern.replaceFirst(s, "").trim()
        }

        // Extract episode-only (EP xx, Episode xx format)
        val epMatch = if (seasonEpisode == null) episodePattern.find(s) else null
        val episodeOnly = epMatch?.let {
            "E${it.groupValues[1].padStart(2, '0')}"
        }
        if (episodeOnly != null) {
            s = episodePattern.replaceFirst(s, "").trim()
        }

        s = dashPattern.replace(s, " ")
        s = trailingPunct.replace(s, "")
        s = leadingPunct.replace(s, "")
        s = spacePattern.replace(s, " ").trim()

        // Title case with stopword support
        val words = s.split(" ")
        val cleanTitle = words.mapIndexed { index, word ->
            val isFirst = index == 0
            val isLast = index == words.lastIndex
            val lower = word.lowercase(Locale.getDefault())

            when {
                // Acronym (all uppercase letters, length >= 2) — preserve
                word.length > 1 && word.any { it.isLetter() } &&
                    word.all { !it.isLetter() || it.isUpperCase() } -> word
                // First or last word — always capitalize first letter
                isFirst || isLast -> word.replaceFirstChar { it.uppercase(Locale.getDefault()) }
                // Stopword — lowercase
                lower in stopwords -> lower
                // Two-letter word — capitalize first letter (Dr, Mr, St)
                word.length <= 2 && word.all { it.isLetter() } ->
                    word.replaceFirstChar { it.uppercase(Locale.getDefault()) }
                // Default — capitalize first letter
                else -> word.replaceFirstChar { it.uppercase(Locale.getDefault()) }
            }
        }.joinToString(" ")

        // Subtitle colon substitution: replace mid-title "The" with ": "
        // when "The" starts a subtitle phrase (not first, not last word).
        // Capitalize the word immediately after the colon.
        val colonResult = cleanTitle.split(" ").let { tokens ->
            if (tokens.size >= 4) {
                val idx = tokens.indexOfFirst { it.equals("The", ignoreCase = true) }
                if (idx in 1 until tokens.lastIndex) {
                    val before = tokens.take(idx).joinToString(" ")
                    val afterCapped = tokens.drop(idx).joinToString(" ")
                        .replaceFirstChar { it.uppercase(Locale.getDefault()) }
                    "$before: $afterCapped"
                } else cleanTitle
            } else cleanTitle
        }

        val cleaned = colonResult
            .replace(Regex("[\\[\\](){}]"), "")
            .replace(Regex("\\s+"), " ")
            .trim()

        val expanded = cleaned
            .replace(Regex("\\bDC\\b"), "Director's Cut")
            .replace(Regex("\\bEC\\b"), "Extended Cut")
            .replace(Regex("\\bUC\\b"), "Ultimate Cut")
            .replace(Regex("\\bTC\\b"), "Theatrical Cut")

        return MediaMetadata(
            cleanTitle = expanded.ifEmpty { rawFilename },
            year = year,
            seasonEpisode = seasonEpisode ?: episodeOnly
        )
    }
}
