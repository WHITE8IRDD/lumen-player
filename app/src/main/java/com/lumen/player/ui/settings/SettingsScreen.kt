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
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lumen.player.R

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

        SwitchRow(
            title = "Fetch online metadata",
            summary = "Download posters and descriptions from TMDB",
            checked = settings.fetchOnlineMetadata,
            onToggle = viewModel::setFetchOnlineMetadata
        )
        if (settings.fetchOnlineMetadata) {
            SwitchRow(
                title = "Wi-Fi only",
                summary = "Only download metadata on Wi-Fi connections",
                checked = settings.metadataWifiOnly,
                onToggle = viewModel::setMetadataWifiOnly
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
