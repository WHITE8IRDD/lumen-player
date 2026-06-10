package com.lumen.player.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lumen.player.R
import com.lumen.player.ui.components.LumenConfirmationDialog

@Composable
fun SettingsScreen(
    onOpenEqualizer: () -> Unit,
    onOpenHiddenItems: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 8.dp)
    ) {
        Text(
            stringResource(R.string.settings),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp)
        )

        SectionTitle("Appearance")
        SwitchRow(
            title = stringResource(R.string.dynamic_color),
            summary = stringResource(R.string.dynamic_color_summary),
            checked = settings.dynamicColor,
            onToggle = viewModel::setDynamicColor
        )

        HorizontalDivider()
        SectionTitle("Playback")
        SwitchRow(
            title = stringResource(R.string.resume_playback),
            summary = stringResource(R.string.resume_playback_summary),
            checked = settings.resumePlayback,
            onToggle = viewModel::setResumePlayback
        )
        SwitchRow(
            title = stringResource(R.string.audio_focus),
            summary = stringResource(R.string.audio_focus_summary),
            checked = settings.respectAudioFocus,
            onToggle = viewModel::setRespectAudioFocus
        )
        SwitchRow(
            title = stringResource(R.string.gapless_playback),
            summary = null,
            checked = settings.gaplessPlayback,
            onToggle = viewModel::setGapless
        )
        SwitchRow(
            title = stringResource(R.string.skip_silence),
            summary = null,
            checked = settings.skipSilence,
            onToggle = viewModel::setSkipSilence
        )

        HorizontalDivider()
        SectionTitle("Library")
        SwitchRow(
            title = stringResource(R.string.show_videos),
            summary = null,
            checked = settings.showVideos,
            onToggle = viewModel::setShowVideos
        )

        SwitchRow(
            title = "Hardware acceleration",
            summary = "Prefer hardware decoders for video playback",
            checked = settings.hardwareAcceleration,
            onToggle = viewModel::setHardwareAcceleration
        )

        HorizontalDivider()
        SectionTitle("Online Metadata")
        var showClearCacheDialog by remember { mutableStateOf(false) }
        IconSwitchRow(
            icon = Icons.Rounded.Cloud,
            title = "Fetch online metadata",
            summary = "Download posters and descriptions from TMDB",
            checked = settings.fetchOnlineMetadata,
            onToggle = viewModel::setFetchOnlineMetadata
        )
        if (settings.fetchOnlineMetadata) {
            if (!viewModel.isTmdbApiAvailable) {
                Text(
                    "TMDB API key not set. Metadata will not be fetched.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(start = 56.dp, end = 16.dp, bottom = 8.dp)
                )
            }
            IconSwitchRow(
                icon = Icons.Rounded.Wifi,
                title = "Wi-Fi only",
                summary = "Only download metadata on Wi-Fi connections",
                checked = settings.metadataWifiOnly,
                onToggle = viewModel::setMetadataWifiOnly
            )
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable { showClearCacheDialog = true }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Rounded.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.padding(start = 12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Clear metadata cache", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "Remove cached poster URLs and re-fetch from TMDB",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        if (showClearCacheDialog) {
            LumenConfirmationDialog(
                title = "Clear metadata cache?",
                body = "This will remove all cached poster URLs. They will be re-fetched from TMDB on the next library scan.",
                confirmLabel = "Clear",
                onConfirm = {
                    viewModel.clearMetadataCache()
                    showClearCacheDialog = false
                },
                onDismiss = { showClearCacheDialog = false }
            )
        }

        HorizontalDivider()
        SectionTitle("Audio")
        Row(
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onOpenEqualizer)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Rounded.GraphicEq, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.padding(start = 12.dp))
            Column {
                Text(stringResource(R.string.equalizer), style = MaterialTheme.typography.bodyLarge)
                Text(
                    "EQ, Bass, Virtualizer, Loudness",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        HorizontalDivider()
        SectionTitle("Content")
        Row(
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onOpenHiddenItems)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Rounded.VisibilityOff, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.padding(start = 12.dp))
            Column {
                Text("Hidden from Home", style = MaterialTheme.typography.bodyLarge)
                Text(
                    "Manage items hidden from the Home screen",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
    )
}

@Composable
private fun SwitchRow(
    title: String,
    summary: String?,
    checked: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { onToggle(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            if (!summary.isNullOrEmpty()) {
                Text(
                    summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(checked = checked, onCheckedChange = onToggle)
    }
}

@Composable
private fun IconSwitchRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    summary: String?,
    checked: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { onToggle(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.padding(start = 12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            if (!summary.isNullOrEmpty()) {
                Text(
                    summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(checked = checked, onCheckedChange = onToggle)
    }
}
