package com.lumen.player.ui.subtitles

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.FileOpen
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.C
import androidx.media3.common.Tracks
import com.lumen.player.core.util.Constants
import kotlinx.coroutines.launch
import java.io.File

private val subtitleLanguages = listOf(
    "English", "Spanish", "French", "German", "Italian",
    "Portuguese", "Russian", "Japanese", "Chinese", "Arabic", "Hindi"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubtitlePanelSheet(
    tracks: Tracks?,
    videoUri: android.net.Uri?,
    activeSubtitleUri: String?,
    subtitleDelayUs: Long = 0L,
    initialSearchQuery: String = "",
    onDismiss: () -> Unit,
    onPickEmbedded: (groupId: String, trackIndex: Int) -> Unit,
    onDisableEmbedded: () -> Unit,
    onPickLocal: (LocalSubtitle) -> Unit,
    onPickDownloaded: (filePath: String, mime: String) -> Unit,
    onSubtitleDelayChange: (Long) -> Unit = {}
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var query by remember { mutableStateOf(initialSearchQuery) }
    var selectedLang by remember { mutableStateOf(subtitleLanguages[0]) }
    var langExpanded by remember { mutableStateOf(false) }
    var searchResults by remember { mutableStateOf<List<SubtitleSearchResult>?>(null) }
    var isSearching by remember { mutableStateOf(false) }
    var isDownloading by remember { mutableStateOf(false) }
    var downloadError by remember { mutableStateOf<String?>(null) }
    var localSubtitles by remember { mutableStateOf<List<LocalSubtitle>>(emptyList()) }

    LaunchedEffect(videoUri) {
        localSubtitles = scanLocalFilesForVideo(context, videoUri)
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        try {
            context.contentResolver.takePersistableUriPermission(
                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (_: SecurityException) {}
        val name = SubtitleHash.getFileName(context, uri) ?: uri.lastPathSegment.orEmpty()
        val mime = detectMime(name.ifBlank { uri.toString() })
        onPickLocal(
            LocalSubtitle(
                uri = uri,
                filePath = uri.toString(),
                mimeType = mime,
                language = langFromName(name.substringBeforeLast('.', name)),
                fileName = name.ifBlank { "Imported subtitle" }
            )
        )
    }

    val osApi = remember {
        OpenSubtitlesApi(apiKey = Constants.OPENSUBTITLES_API_KEY)
    }

    fun doSearch() {
        if (query.isBlank()) return
        isSearching = true
        downloadError = null
        scope.launch {
            try {
                val movieHash = videoUri?.let { SubtitleHash.hashFromUri(context, it) }
                val result = osApi.search(query = query, movieHash = movieHash)
                searchResults = if (result.isSuccess) result.getOrDefault(emptyList())
                else { downloadError = result.exceptionOrNull()?.message ?: "Search failed"; emptyList() }
            } catch (e: Exception) {
                downloadError = "Search failed: ${e.message}"
                searchResults = emptyList()
            } finally {
                isSearching = false
            }
        }
    }

    fun downloadAndPick(sub: SubtitleSearchResult) {
        isDownloading = true
        downloadError = null
        scope.launch {
            try {
                val out = File(context.cacheDir, "subtitles")
                val path = osApi.download(sub.fileId, out)
                if (path != null) onPickDownloaded(path, detectMime(path))
                else downloadError = "Download failed"
            } catch (e: Exception) {
                downloadError = "Download error: ${e.message}"
            } finally {
                isDownloading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        if (initialSearchQuery.isNotBlank()) {
            doSearch()
        }
    }

    val embedded = remember(tracks) { buildEmbeddedTracks(tracks) }

    var delayMs by remember(subtitleDelayUs) { mutableStateOf(subtitleDelayUs / 1000L) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            item {
                androidx.compose.material3.BottomSheetDefaults.DragHandle(
                    modifier = Modifier.align(Alignment.CenterHorizontally).fillMaxWidth()
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Subtitles",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close")
                    }
                }
            }

            // Section 1: Subtitle Delay
            item {
                HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                Spacer(Modifier.height(12.dp))
                SectionTitle("Subtitle Delay")
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Delay",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        "${delayMs / 1000.0}s",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(
                        onClick = { delayMs = 0; onSubtitleDelayChange(0L) },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(Icons.Rounded.Refresh, contentDescription = "Reset delay",
                            modifier = Modifier.size(20.dp))
                    }
                }
                Slider(
                    value = delayMs.toFloat().coerceIn(-5000f, 5000f),
                    onValueChange = { delayMs = (it / 250f).toLong() * 250 },
                    onValueChangeFinished = { onSubtitleDelayChange(delayMs * 1000L) },
                    valueRange = -5000f..5000f,
                    steps = 39,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
            }

            // Section 2: Embedded Tracks
            item {
                HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                Spacer(Modifier.height(12.dp))
                SectionTitle("Embedded Tracks")
                if (embedded.isEmpty()) {
                    Text(
                        "No embedded subtitle tracks",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                } else {
                    SubtitleRow(
                        label = "None / Disable",
                        secondary = "Turn off subtitles",
                        isActive = activeSubtitleUri == null,
                        onClick = onDisableEmbedded
                    )
                    embedded.forEach { opt ->
                        SubtitleRow(
                            label = opt.title,
                            secondary = opt.subtitle,
                            isActive = opt.isActive,
                            onClick = { onPickEmbedded(opt.groupId, opt.trackIndex) }
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            // Section 3: Local Subtitle Files
            item {
                HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                Spacer(Modifier.height(12.dp))
                SectionTitle("Local Subtitle Files")
                if (localSubtitles.isEmpty()) {
                    Text(
                        "No matching files in the same folder",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                } else {
                    localSubtitles.forEach { sub ->
                        SubtitleRow(
                            label = sub.language,
                            secondary = "${extFor(sub.fileName).uppercase()} \u00B7 ${sub.fileName}",
                            isActive = activeSubtitleUri == sub.uri.toString(),
                            onClick = { onPickLocal(sub) }
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                FilledTonalButton(
                    onClick = {
                        val mimeTypes = arrayOf(
                            "application/x-subrip", "text/vtt", "text/x-ssa",
                            "text/srt", "text/plain", "*/*"
                        )
                        importLauncher.launch(mimeTypes)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Rounded.FileOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Import subtitle from device")
                }
                Spacer(Modifier.height(8.dp))
            }

            // Section 4: Download from OpenSubtitles
            item {
                HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                Spacer(Modifier.height(12.dp))
                SectionTitle("Download from OpenSubtitles")

                if (Constants.OPENSUBTITLES_API_KEY.isBlank()) {
                    Text(
                        "OpenSubtitles API key not configured. Add your key in Settings.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search by movie or filename") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { doSearch() }),
                        trailingIcon = {
                            IconButton(onClick = { doSearch() }) {
                                Icon(Icons.Rounded.Search, contentDescription = "Search")
                            }
                        }
                    )
                    Spacer(Modifier.height(8.dp))

                    ExposedDropdownMenuBox(
                        expanded = langExpanded,
                        onExpandedChange = { langExpanded = !langExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedLang,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = langExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            label = { Text("Language") }
                        )
                        ExposedDropdownMenu(
                            expanded = langExpanded,
                            onDismissRequest = { langExpanded = false }
                        ) {
                            subtitleLanguages.forEach { lang ->
                                DropdownMenuItem(
                                    text = { Text(lang) },
                                    onClick = {
                                        selectedLang = lang
                                        langExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    when {
                        isDownloading -> {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Downloading\u2026", style = MaterialTheme.typography.bodySmall)
                        }
                        isSearching -> {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Searching\u2026", style = MaterialTheme.typography.bodySmall)
                        }
                        downloadError != null -> {
                            Text(
                                downloadError ?: "",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        searchResults != null && searchResults!!.isEmpty() -> {
                            Text(
                                "No results",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        searchResults != null -> {
                            searchResults!!.forEach { sub ->
                                Row(
                                    Modifier.fillMaxWidth()
                                        .clickable { downloadAndPick(sub) }
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            sub.language.uppercase(),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            sub.fileName,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Icon(
                                        Icons.Rounded.Download,
                                        contentDescription = "Download",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                        else -> {
                            Text(
                                "Search by movie or filename above.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        title,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun SubtitleRow(
    label: String,
    secondary: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(28.dp), contentAlignment = Alignment.Center) {
            if (isActive) {
                Icon(
                    Icons.Rounded.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        Column(Modifier.weight(1f)) {
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                secondary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private data class EmbeddedEntry(
    val key: String,
    val groupId: String,
    val trackIndex: Int,
    val title: String,
    val subtitle: String,
    val isActive: Boolean = false
)

private fun buildEmbeddedTracks(tracks: Tracks?): List<EmbeddedEntry> {
    if (tracks == null) return emptyList()
    val out = mutableListOf<EmbeddedEntry>()
    tracks.groups.forEach { group ->
        val tg = group.mediaTrackGroup
        if (tg.type != C.TRACK_TYPE_TEXT) return@forEach
        for (i in 0 until tg.length) {
            val format = tg.getFormat(i)
            val label = format.label?.toString() ?: "Track ${i + 1}"
            val language = format.language?.uppercase() ?: "UND"
            val mime = format.sampleMimeType ?: format.codecs ?: "text"
            val shortMime = when {
                mime.contains("vtt", ignoreCase = true) -> "VTT"
                mime.contains("ttml", ignoreCase = true) -> "TTML"
                mime.contains("cea-608", ignoreCase = true) ||
                    mime.contains("cea-708", ignoreCase = true) -> "CEA"
                else -> "SUB"
            }
            out += EmbeddedEntry(
                key = "${tg.id}.$i",
                groupId = tg.id,
                trackIndex = i,
                title = "$language \u00B7 $shortMime",
                subtitle = label,
                isActive = group.isTrackSelected(i)
            )
        }
    }
    return out
}

private fun scanLocalFilesForVideo(
    context: android.content.Context,
    videoUri: android.net.Uri?
): List<LocalSubtitle> {
    if (videoUri == null) return emptyList()
    val path: String? = if (videoUri.scheme == "file") videoUri.path else resolveContentPath(context, videoUri)
    if (path == null) return emptyList()
    val videoFile = File(path)
    val parent = videoFile.parentFile ?: return emptyList()
    val base = stripExt(videoFile.name)
    val se = seasonEpisodeTokens(base)
    val files = parent.listFiles() ?: return emptyList()
    return files.asSequence()
        .filter { it.isFile }
        .filter { hasSubExt(it.name) }
        .filter { f ->
            val other = stripExt(f.name)
            if (other.equals(base, ignoreCase = true)) return@filter true
            se?.let { (s, n, ep) ->
                val ot = seasonEpisodeTokens(other)
                if (ot != null) {
                    val (os, on, oep) = ot
                    return@filter os.equals(s, ignoreCase = true) && on == n && oep == ep
                }
            }
            false
        }
        .map { f ->
            LocalSubtitle(
                uri = android.net.Uri.fromFile(f),
                filePath = f.absolutePath,
                mimeType = mimeForExt(f.extension),
                language = langFromName(f.nameWithoutExtension),
                fileName = f.name
            )
        }
        .sortedWith(compareBy({ it.language }, { it.fileName }))
        .toList()
}

private fun resolveContentPath(context: android.content.Context, uri: android.net.Uri): String? {
    return try {
        val proj = arrayOf(android.provider.MediaStore.Video.VideoColumns.DATA)
        context.contentResolver.query(uri, proj, null, null, null)?.use { c ->
            if (c.moveToFirst()) c.getString(0) else null
        }
    } catch (_: Exception) { null }
}

private fun stripExt(name: String): String {
    val dot = name.lastIndexOf('.')
    return if (dot <= 0) name else name.substring(0, dot)
}

private fun hasSubExt(name: String): Boolean {
    val ext = name.substringAfterLast('.', "").lowercase()
    return ext in setOf("srt", "ass", "ssa", "vtt", "sub")
}

private fun seasonEpisodeTokens(name: String): Triple<String, Int, Int>? {
    val m = Regex("""^(.*?)\.S(\d{1,2})E(\d{1,2})$""", RegexOption.IGNORE_CASE).find(name) ?: return null
    val (s, n, e) = m.destructured
    return Triple(s.trim(), n.toIntOrNull() ?: return null, e.toIntOrNull() ?: return null)
}

private fun langFromName(name: String): String {
    val parts = name.split('.')
    if (parts.size >= 2) {
        val c = parts[parts.size - 2]
        if (c.length in 2..8 && c.all { it.isLetter() || it == '_' || it == '-' }) {
            return c.uppercase()
        }
    }
    return "UND"
}

private fun mimeForExt(ext: String): String = when (ext.lowercase()) {
    "srt" -> "application/x-subrip"
    "vtt" -> "text/vtt"
    "ass", "ssa" -> "text/x-ssa"
    "sub" -> "application/x-subrip"
    else -> "application/x-subrip"
}

private fun extFor(name: String): String = name.substringAfterLast('.', "srt")

private fun detectMime(path: String): String = when {
    path.endsWith(".srt", ignoreCase = true) -> "application/x-subrip"
    path.endsWith(".vtt", ignoreCase = true) -> "text/vtt"
    path.endsWith(".ass", ignoreCase = true) || path.endsWith(".ssa", ignoreCase = true) -> "text/x-ssa"
    else -> "application/x-subrip"
}
