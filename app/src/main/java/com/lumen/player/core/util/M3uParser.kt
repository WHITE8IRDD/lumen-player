package com.lumen.player.core.util

data class M3uEntry(
    val name: String,
    val url: String,
    val logo: String? = null,
    val groupTitle: String? = null
)

object M3uParser {
    fun parse(content: String, defaultGroup: String = "Imported"): List<M3uEntry> {
        val entries = mutableListOf<M3uEntry>()
        val lines = content.lines()
        var currentName: String? = null
        var currentLogo: String? = null
        var currentGroup: String? = null

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty() || trimmed.startsWith("#EXTM3U")) continue

            if (trimmed.startsWith("#EXTINF:")) {
                val afterComma = trimmed.indexOfLast { it == ',' }
                currentName = if (afterComma >= 0) trimmed.substring(afterComma + 1).trim() else null
                val logoMatch = Regex("""tvg-logo="([^"]*)"""").find(trimmed)
                currentLogo = logoMatch?.groupValues?.getOrNull(1)?.takeIf { it.isNotBlank() }
                val groupMatch = Regex("""group-title="([^"]*)"""").find(trimmed)
                currentGroup = groupMatch?.groupValues?.getOrNull(1)?.takeIf { it.isNotBlank() }
                continue
            }

            if (trimmed.startsWith("#")) continue

            val name = currentName
            if (name != null && trimmed.isNotBlank()) {
                entries.add(M3uEntry(
                    name = name,
                    url = trimmed,
                    logo = currentLogo,
                    groupTitle = currentGroup
                ))
            }
            currentName = null
            currentLogo = null
            currentGroup = null
        }
        return entries
    }
}
