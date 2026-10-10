package com.lumen.player.ui.subtitles

import com.lumen.player.ui.theme.LocalAccentColor
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.FileOpen
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.HourglassBottom
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties

data class EmbeddedTrack(
    val id: String,
    val groupId: String,
    val trackIndex: Int,
    val language: String?,
    val label: String,
    val index: Int,
    val isActive: Boolean = false
)

@Composable
fun SubtitleMenu(
    visible: Boolean,
    embeddedTracks: List<EmbeddedTrack>,
    activeTrackId: String?,
    onDismiss: () -> Unit,
    onDisable: () -> Unit,
    onSelectTrack: (EmbeddedTrack) -> Unit,
    onShowDelayDialog: () -> Unit,
    onPickFile: () -> Unit,
    onShowLocalSubs: () -> Unit,
    onShowDownload: () -> Unit
) {
    if (!visible) return

    DropdownMenu(
        expanded = visible,
        onDismissRequest = onDismiss,
        modifier = Modifier
            .background(Color(0xFF1C1C1C), RoundedCornerShape(14.dp))
            .border(BorderStroke(1.dp, LocalAccentColor.current.copy(alpha = 0.35f)), RoundedCornerShape(14.dp))
            .width(280.dp),
        properties = PopupProperties(focusable = true)
    ) {
        Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text("Subtitles", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White.copy(0.6f), letterSpacing = 0.5.sp)
        }
        HorizontalDivider(color = Color.White.copy(0.08f), thickness = 0.5.dp)

        if (embeddedTracks.isNotEmpty()) {
            TrackMenuItem(icon = if (activeTrackId == null) Icons.Rounded.Check else null, label = "No track", isActive = activeTrackId == null, onClick = { onDisable(); onDismiss() })
            embeddedTracks.forEach { track ->
                TrackMenuItem(
                    icon = if (track.id == activeTrackId) Icons.Rounded.Check else null,
                    label = "Track ${track.index + 1} \u00B7 ${(track.language ?: "UND").uppercase()}",
                    subtitle = track.label,
                    isActive = track.id == activeTrackId,
                    onClick = { onSelectTrack(track); onDismiss() }
                )
            }
            HorizontalDivider(color = Color.White.copy(0.08f), thickness = 0.5.dp)
        }

        MenuRow(icon = Icons.Rounded.Restore, label = "Subtitle delay", onClick = { onShowDelayDialog(); onDismiss() })
        MenuRow(icon = Icons.Rounded.FileOpen, label = "Select subtitle file", onClick = { onPickFile(); onDismiss() })
        MenuRow(icon = Icons.Rounded.FolderOpen, label = "Local subtitles", onClick = { onShowLocalSubs(); onDismiss() })
        MenuRow(icon = Icons.Rounded.Download, label = "Download subtitles", onClick = { onShowDownload(); onDismiss() })
    }
}

@Composable
private fun MenuRow(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().height(44.dp).clickable(onClick = onClick).padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = Color.White.copy(0.7f), modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(14.dp))
        Text(label, fontSize = 14.sp, color = Color.White)
    }
}

@Composable
private fun TrackMenuItem(icon: ImageVector?, label: String, subtitle: String? = null, isActive: Boolean = false, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().height(44.dp).clickable(onClick = onClick).padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(icon, null, tint = if (isActive) LocalAccentColor.current else Color.White.copy(0.7f), modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(14.dp))
        } else { Spacer(Modifier.width(32.dp)) }
        Column(Modifier.weight(1f)) {
            Text(label, fontSize = 14.sp, color = if (isActive) LocalAccentColor.current else Color.White)
            if (subtitle != null) Text(subtitle, fontSize = 11.sp, color = Color.White.copy(0.5f), maxLines = 1)
        }
    }
}
