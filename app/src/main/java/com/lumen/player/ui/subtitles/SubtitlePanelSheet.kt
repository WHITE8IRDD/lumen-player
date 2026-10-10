package com.lumen.player.ui.subtitles

import com.lumen.player.ui.theme.LocalAccentColor
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lumen.player.domain.model.SubtitleResult
import java.io.File

private val quickLangs = listOf("EN" to "English", "AR" to "Arabic", "FR" to "French", "ES" to "Spanish")

@Composable
fun SubtitlePanelContent(
    localSubs: List<File>,
    onlineSubs: List<SubtitleResult>,
    appliedSubtitleName: String?,
    currentLanguage: String,
    onLanguageChange: (String) -> Unit,
    isLoading: Boolean,
    onLocalApply: (File) -> Unit,
    onOnlineDownload: (SubtitleResult) -> Unit,
    onSearchTap: () -> Unit,
    onDismiss: () -> Unit
) {
    fun detectLocalSubLanguage(name: String): String? {
        val lower = name.lowercase()
        return when {
            lower.contains(".eng.") || lower.contains(".en.") || lower.contains("english") || lower.contains("_eng") || lower.startsWith("eng") -> "en"
            lower.contains(".ara.") || lower.contains(".ar.") || lower.contains("arabic") || lower.contains("_ara") -> "ar"
            lower.contains(".spa.") || lower.contains(".es.") || lower.contains("spanish") || lower.contains("_spa") || lower.contains("latin american") -> "es"
            lower.contains(".fre.") || lower.contains(".fra.") || lower.contains(".fr.") || lower.contains("french") -> "fr"
            lower.contains(".ger.") || lower.contains(".deu.") || lower.contains(".de.") || lower.contains("german") -> "de"
            lower.contains(".ita.") || lower.contains(".it.") || lower.contains("italian") -> "it"
            lower.contains(".kor.") || lower.contains(".ko.") || lower.contains("korean") -> "ko"
            lower.contains(".chi.") || lower.contains(".zho.") || lower.contains("chinese") || lower.contains("simplified") || lower.contains("traditional") -> "zh"
            lower.contains(".jpn.") || lower.contains(".ja.") || lower.contains("japanese") -> "ja"
            else -> null
        }
    }

    val filteredLocalSubs = remember(localSubs, currentLanguage) {
        localSubs.filter { file ->
            val detected = detectLocalSubLanguage(file.name)
            val matches = detected == null || detected == currentLanguage
            android.util.Log.d("SubFix", "Local filter: ${file.name} -> $detected (match: $matches)")
            matches
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0E0E12))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(48.dp).padding(start = 4.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Rounded.ArrowBack, "Back", tint = Color.White, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(4.dp))
            quickLangs.forEach { (code, _) ->
                val isSelected = currentLanguage == code
                Box(
                    modifier = Modifier.height(34.dp)
                        .background(
                            if (isSelected) LocalAccentColor.current
                            else Color.White.copy(0.08f),
                            RoundedCornerShape(17.dp)
                        )
                        .clickable { onLanguageChange(code) }
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(code, fontSize = 13.sp, fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.Black else Color.White.copy(0.7f))
                }
                Spacer(Modifier.width(6.dp))
            }
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onSearchTap, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Rounded.Search, "More languages", tint = Color.White.copy(0.5f), modifier = Modifier.size(18.dp))
            }
        }
        HorizontalDivider(color = Color.White.copy(0.06f))

        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = LocalAccentColor.current, modifier = Modifier.size(28.dp), strokeWidth = 2.5.dp)
            }
        } else if (onlineSubs.isEmpty() && filteredLocalSubs.isEmpty()) {
            Column(Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Icon(Icons.Rounded.SearchOff, null, tint = Color.White.copy(0.3f), modifier = Modifier.size(40.dp))
                Spacer(Modifier.height(12.dp))
                Text("No subtitles found", fontSize = 14.sp, color = Color.White.copy(0.6f))
                Spacer(Modifier.height(12.dp))
                TextButton(onClick = onSearchTap) { Text("Edit search", color = LocalAccentColor.current, fontSize = 12.sp) }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                if (filteredLocalSubs.isNotEmpty()) {
                    item { SectionLabel("ON DEVICE", LocalAccentColor.current) }
                    items(filteredLocalSubs) { file ->
                        val detectedLang = detectLocalSubLanguage(file.name)
                        CompactSubRow(
                            label = detectedLang?.uppercase() ?: "SUB", name = file.name, trailingText = "${file.length() / 1024}KB",
                            downloadCount = null,
                            isApplied = appliedSubtitleName == file.name, isDownloading = false,
                            accentColor = if (detectedLang != null) LocalAccentColor.current else Color.White.copy(0.5f),
                            onClick = { onLocalApply(file) }
                        )
                    }
                }
                item { SectionLabel("ONLINE", LocalAccentColor.current) }
                if (onlineSubs.isEmpty()) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                            Text("No online results. Try a different language.", fontSize = 12.sp, color = Color.White.copy(0.4f))
                        }
                    }
                } else {
                    items(onlineSubs) { result ->
                        CompactSubRow(
                            label = result.language, name = result.releaseName ?: "Unknown", trailingText = null,
                            downloadCount = result.downloadCount?.let { if (it >= 1000) "${it / 1000}k" else it.toString() },
                            isApplied = appliedSubtitleName == result.releaseName, isDownloading = false, accentColor = LocalAccentColor.current,
                            onClick = { onOnlineDownload(result) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String, color: Color) {
    Text(text, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp,
        color = color.copy(0.7f), modifier = Modifier.padding(start = 14.dp, top = 14.dp, bottom = 6.dp))
}

@Composable
private fun CompactSubRow(
    label: String?, name: String, trailingText: String?,
    downloadCount: String?,
    isApplied: Boolean, isDownloading: Boolean = false, accentColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().height(52.dp)
            .background(if (isApplied) accentColor.copy(0.10f) else Color.Transparent)
            .clickable(enabled = !isDownloading, onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (label != null) {
            Box(
                modifier = Modifier.size(width = 32.dp, height = 22.dp)
                    .background(accentColor.copy(0.18f), RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = accentColor) }
            Spacer(Modifier.width(12.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(name, fontSize = 14.sp,
                color = if (isApplied) accentColor else Color.White,
                fontWeight = if (isApplied) FontWeight.SemiBold else FontWeight.Normal,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (downloadCount != null || trailingText != null) {
                Spacer(Modifier.height(2.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (downloadCount != null) {
                        Icon(Icons.Rounded.Download, null, tint = Color.White.copy(0.4f), modifier = Modifier.size(10.dp))
                        Text(downloadCount, fontSize = 10.sp, color = Color.White.copy(0.5f))
                    }
                    if (trailingText != null) Text(trailingText, fontSize = 10.sp, color = Color.White.copy(0.4f))
                }
            }
        }
        Spacer(Modifier.width(8.dp))
        Box(Modifier.size(28.dp), contentAlignment = Alignment.Center) {
            when {
                isDownloading -> CircularProgressIndicator(color = accentColor, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                isApplied -> Icon(Icons.Rounded.CheckCircle, null, tint = accentColor, modifier = Modifier.size(20.dp))
                label != null -> Icon(Icons.Rounded.Download, null, tint = accentColor.copy(0.7f), modifier = Modifier.size(18.dp))
                else -> Icon(Icons.Rounded.PlayArrow, null, tint = accentColor.copy(0.7f), modifier = Modifier.size(18.dp))
            }
        }
    }
    HorizontalDivider(thickness = 0.5.dp, color = Color.White.copy(0.05f))
}
