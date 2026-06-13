package com.lumen.player.data.metadata

import com.lumen.player.domain.model.MediaMetadata
import com.lumen.player.domain.repository.MetadataResolver
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaTitleParser @Inject constructor() : MetadataResolver {

    private val bracketPattern = Regex("\\[.*?\\]")
    private val tagParenPattern = Regex("\\([^)]*?(?:1080p|720p|2160p|480p|x264|x265|HEVC|BluRay|WEBRip|WEB-DL|HDRip|DVDRip|BrRip|YTS|ELiTE|Panda|RARBG|ROVERS|JVT|AMCORD|IMAX|HDTV|UNRATED|EXTENDED|REPACK|PROPER|INTERNAL).*?\\)", RegexOption.IGNORE_CASE)
    private val qualityPattern = Regex("(?i)\\b(?:1080p|720p|2160p|480p|360p|4K|8K|UHD|UHDR|2K|HD|SD|x264|x265|h264|h265|HEVC|BluRay|WEBRip|WEB-DL|WEB DL|HDRip|DVDRip|BrRip|HDTV|HDTVRip|UNRATED|EXTENDED|REPACK|PROPER|INTERNAL|FHD|HDR|YTS|YIFY|RARBG|ELiTE|Panda|ROVERS|JVT|AMCORD|IMAX)\\b")
    private val groupSuffixPattern = Regex("-[a-zA-Z0-9]+(?:\\.[a-zA-Z0-9]+)*$")
    private val separatorPattern = Regex("[\\._]")
    private val spacePattern = Regex("\\s+")
    private val yearPattern = Regex("\\b(19\\d{2}|20\\d{2})\\b")
    private val sePattern = Regex("\\bS(\\d{1,2})E(\\d{1,2})\\b", RegexOption.IGNORE_CASE)
    private val episodePattern = Regex("\\b(?:EP|EPISODE|Episode)\\s*(\\d{1,3})\\b", RegexOption.IGNORE_CASE)
    private val dashPattern = Regex("\\s*[-–—]+\\s*")
    private val trailingPunct = Regex("[,\\s]+$")
    private val leadingPunct = Regex("^[,\\s]+")

    private val stopwords = setOf(
        "of", "the", "a", "an", "and", "or", "but", "for",
        "in", "on", "at", "to", "by", "with", "vs", "from",
        "as", "into", "onto", "upon"
    )

    override suspend fun resolve(rawFilename: String): MediaMetadata {
        var s = rawFilename.trim()

        // Strip file extension — only if the part after the last dot
        // contains no spaces (avoids treating "Witanime.com" as an extension)
        val dot = s.lastIndexOf('.')
        if (dot > s.lastIndexOf('/') && dot > s.lastIndexOf('\\') && dot > 0
            && dot + 1 < s.length && !s.substring(dot).contains(' ')) {
            s = s.substring(0, dot)
        }

        s = bracketPattern.replace(s, "")
        s = tagParenPattern.replace(s, "")
        s = qualityPattern.replace(s, "")
        s = groupSuffixPattern.replace(s, "")
        s = separatorPattern.replace(s, " ")
        s = spacePattern.replace(s, " ").trim()

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

        return MediaMetadata(
            cleanTitle = cleaned.ifEmpty { rawFilename },
            year = year,
            seasonEpisode = seasonEpisode ?: episodeOnly
        )
    }
}
