package com.lumen.player.ui.player.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.media3.common.C
import androidx.media3.common.Tracks

/**
 * Plain composable (no [ModalBottomSheet] wrapper!) that renders track
 * options for a given [trackType] as a scrollable list.
 *
 * CRITICAL: The parent [TrackModeSheet] already provides the bottom-sheet
 * scaffold. If this composable ALSO wrapped its content in a
 * [ModalBottomSheet] the app would create NESTED modal sheets — a known
 * cause of UI freezes and crashes in Compose Material3.
 *
 * Pass [onDismiss] if you need the parent to close — it is forwarded from
 * TrackModeSheet.
 */
@Composable
fun TrackSelectorSheet(
    title: String,
    trackType: @C.TrackType Int,
    tracks: Tracks?,
    onDismiss: () -> Unit,
    onSelect: (groupId: String?, trackIndex: Int) -> Unit
) {
    val options = remember(tracks, trackType) { buildOptions(tracks, trackType) }
    var selectedKey by remember { mutableStateOf<String?>(null) }

    if (title.isNotEmpty()) {
        Text(title, style = MaterialTheme.typography.titleLarge)
    }

    if (options.isEmpty()) {
        Text(
            "No ${trackLabel(trackType)} tracks available",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 12.dp)
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth().heightIn(max = 240.dp)
    ) {
        items(options, key = { "${it.groupId ?: "_"}.${it.trackIndex}" }) { opt ->
            val key = "${opt.groupId ?: "_"}.${opt.trackIndex}"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        selectedKey = key
                        onSelect(opt.groupId, opt.trackIndex)
                        onDismiss()
                    }
                    .padding(vertical = 12.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.size(24.dp), contentAlignment = Alignment.Center) {
                    if (key == selectedKey) {
                        Icon(
                            Icons.Rounded.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Column {
                    Text(
                        opt.label,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (key == selectedKey) FontWeight.SemiBold
                        else FontWeight.Normal
                    )
                    if (!opt.language.isNullOrBlank()) {
                        Text(
                            opt.language,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

private fun buildOptions(tracks: Tracks?, type: Int): List<TrackOption> {
    if (tracks == null) return emptyList()
    val out = mutableListOf<TrackOption>()
    tracks.groups.forEach { group ->
        val tg = group.mediaTrackGroup
        if (tg.type != type) return@forEach
        for (i in 0 until tg.length) {
            val format = tg.getFormat(i)
            out += TrackOption(
                groupId = tg.id,
                trackIndex = i,
                label = format.label?.toString()
                    ?: format.language?.let { it }
                    ?: "Track ${i + 1}",
                language = format.language
            )
        }
    }
    return out
}

private fun trackLabel(type: Int): String = when (type) {
    C.TRACK_TYPE_AUDIO -> "audio"
    C.TRACK_TYPE_TEXT -> "subtitle"
    C.TRACK_TYPE_VIDEO -> "video"
    else -> "track"
}

private data class TrackOption(
    val groupId: String?,
    val trackIndex: Int,
    val label: String,
    val language: String? = null
)