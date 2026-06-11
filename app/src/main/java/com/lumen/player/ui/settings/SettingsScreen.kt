package com.lumen.player.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.ArrowForwardIos
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Subtitles
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lumen.player.BuildConfig
import com.lumen.player.R
import com.lumen.player.domain.repository.ThemeMode
import com.lumen.player.ui.components.LumenConfirmationDialog
import com.lumen.player.ui.components.LumenInputDialog
import com.lumen.player.ui.components.LumenPickerDialog
import androidx.compose.foundation.BorderStroke
import com.lumen.player.core.theme.LumenBlue
import com.lumen.player.core.theme.LumenBlueDeep
import com.lumen.player.core.theme.LumenGreen
import com.lumen.player.core.theme.LumenGreenDeep
import com.lumen.player.core.theme.LumenYellow
import com.lumen.player.core.theme.LumenYellowDeep
import com.lumen.player.core.theme.LumenOrange
import com.lumen.player.core.theme.LumenOrangeDeep
import com.lumen.player.core.theme.LumenPink
import com.lumen.player.core.theme.LumenPinkDeep
import com.lumen.player.core.theme.LumenCyan
import com.lumen.player.core.theme.LumenCyanDeep
import com.lumen.player.core.theme.LumenRed
import com.lumen.player.core.theme.LumenRedDeep
import com.lumen.player.core.theme.LumenPurple
import com.lumen.player.core.theme.LumenPurpleDeep
import com.lumen.player.core.theme.LumenDarkOnSurfaceVariant
import com.lumen.player.ui.components.ShapeIcon

private fun themeLabel(mode: ThemeMode): String = when (mode) {
    ThemeMode.SYSTEM -> "System"
    ThemeMode.LIGHT -> "Light"
    ThemeMode.DARK -> "Dark"
}

@Composable
fun SettingsScreen(
    onOpenEqualizer: () -> Unit,
    onOpenHiddenItems: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val s by viewModel.settings.collectAsStateWithLifecycle()

    var showThemePicker by remember { mutableStateOf(false) }
    var showFontSizePicker by remember { mutableStateOf(false) }
    var showAccentColorPicker by remember { mutableStateOf(false) }
    var showAspectRatioPicker by remember { mutableStateOf(false) }
    var showBrightnessPicker by remember { mutableStateOf(false) }
    var showSubtitleColorPicker by remember { mutableStateOf(false) }
    var showLanguagePicker by remember { mutableStateOf(false) }
    var showSubtitleDelayDialog by remember { mutableStateOf(false) }
    var showSubtitleTextSizeDialog by remember { mutableStateOf(false) }
    var showOpenSubsKeyDialog by remember { mutableStateOf(false) }
    var showClearCacheDialog by remember { mutableStateOf(false) }
    var showResetSettingsDialog by remember { mutableStateOf(false) }
    var showResetOnboardingDialog by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 40.dp, bottom = 8.dp)
    ) {
        Text(
            stringResource(R.string.settings),
            style = MaterialTheme.typography.displayMedium,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp)
        )

        SettingsSection(
            title = "Appearance",
            subtitle = "Theme, colors, typography",
            icon = Icons.Rounded.Palette,
            color = LumenPurple,
            expanded = s.sectionAppearance,
            onToggle = { viewModel.setSectionExpanded("appearance", !s.sectionAppearance) }
        ) {
            ClickableRow(
                title = "Theme",
                subtitle = themeLabel(s.themeMode),
                onClick = { showThemePicker = true }
            )
            SwitchRow(
                title = stringResource(R.string.dynamic_color),
                summary = stringResource(R.string.dynamic_color_summary),
                checked = s.dynamicColor,
                onToggle = viewModel::setDynamicColor
            )
            ClickableRow(
                title = "Font Size",
                subtitle = s.fontSize.lowercase().replaceFirstChar { it.uppercase() },
                onClick = { showFontSizePicker = true }
            )
            AccentColorRow(
                hex = s.accentColorHex,
                onClick = { showAccentColorPicker = true }
            )
        }

        SettingsSection(
            title = "Playback",
            subtitle = "Resume, focus, gapless",
            icon = Icons.Rounded.PlayCircle,
            color = LumenGreen,
            expanded = s.sectionPlayback,
            onToggle = { viewModel.setSectionExpanded("playback", !s.sectionPlayback) }
        ) {
            SwitchRow(title = stringResource(R.string.resume_playback), summary = stringResource(R.string.resume_playback_summary), checked = s.resumePlayback, onToggle = viewModel::setResumePlayback)
            SwitchRow(title = stringResource(R.string.audio_focus), summary = stringResource(R.string.audio_focus_summary), checked = s.respectAudioFocus, onToggle = viewModel::setRespectAudioFocus)
            SwitchRow(title = stringResource(R.string.gapless_playback), summary = null, checked = s.gaplessPlayback, onToggle = viewModel::setGapless)
            SwitchRow(title = stringResource(R.string.skip_silence), summary = null, checked = s.skipSilence, onToggle = viewModel::setSkipSilence)
        }

        SettingsSection(
            title = "Video",
            subtitle = "Hardware acceleration, aspect ratio",
            icon = Icons.Rounded.Movie,
            color = LumenBlue,
            expanded = s.sectionVideo,
            onToggle = { viewModel.setSectionExpanded("video", !s.sectionVideo) }
        ) {
            SwitchRow(title = "Hardware Acceleration", summary = "Prefer hardware decoders for video playback", checked = s.hardwareAcceleration, onToggle = viewModel::setHardwareAcceleration)
            ClickableRow(title = "Default Aspect Ratio", subtitle = s.aspectRatio.replace("_", ":").replace("R", ""), onClick = { showAspectRatioPicker = true })
            ClickableRow(title = "Brightness Mode", subtitle = s.brightnessMode.lowercase().replaceFirstChar { it.uppercase() }, onClick = { showBrightnessPicker = true })
        }

        SettingsSection(
            title = "Subtitles",
            subtitle = "Text size, color, language, delay",
            icon = Icons.Rounded.Subtitles,
            color = LumenYellow,
            expanded = s.sectionSubtitles,
            onToggle = { viewModel.setSectionExpanded("subtitles", !s.sectionSubtitles) }
        ) {
            ClickableRow(title = "Default Text Size", subtitle = "${s.subtitleTextSize.toInt()}sp", onClick = { showSubtitleTextSizeDialog = true })
            ClickableRow(title = "Default Text Color", subtitle = s.subtitleColor.lowercase().replaceFirstChar { it.uppercase() }, onClick = { showSubtitleColorPicker = true })
            ClickableRow(title = "Default Delay", subtitle = "${s.subtitleDelayUs / 1000}ms", onClick = { showSubtitleDelayDialog = true })
            ClickableRow(title = "Default Language", subtitle = s.subtitleLanguage, onClick = { showLanguagePicker = true })
            ClickableRow(title = "OpenSubtitles API Key", subtitle = if (s.opensubtitlesApiKey.isNotEmpty()) "********" else "Not set", onClick = { showOpenSubsKeyDialog = true })
        }

        SettingsSection(
            title = "Library",
            subtitle = "Excluded folders, auto-scan",
            icon = Icons.Rounded.LibraryMusic,
            color = LumenOrange,
            expanded = s.sectionLibrary,
            onToggle = { viewModel.setSectionExpanded("library", !s.sectionLibrary) }
        ) {
            ClickableRow(title = "Excluded Folders", subtitle = "Manage hidden folders", onClick = onOpenHiddenItems)
            SwitchRow(title = "Auto-Scan on Startup", summary = null, checked = s.autoScanOnStartup, onToggle = viewModel::setAutoScanOnStartup)
            SwitchRow(title = stringResource(R.string.show_videos), summary = null, checked = s.showVideos, onToggle = viewModel::setShowVideos)
            SwitchRow(title = "Show Audio", summary = null, checked = s.showAudio, onToggle = viewModel::setShowAudio)
        }

        SettingsSection(
            title = "Audio",
            subtitle = "Equalizer, replay gain",
            icon = Icons.Rounded.MusicNote,
            color = LumenPink,
            expanded = s.sectionAudio,
            onToggle = { viewModel.setSectionExpanded("audio", !s.sectionAudio) }
        ) {
            ClickableRow(title = stringResource(R.string.equalizer), subtitle = "EQ, Bass, Virtualizer, Loudness", onClick = onOpenEqualizer)
            SwitchRow(title = "Replay Gain", summary = "Normalize volume across tracks", checked = s.replayGain, onToggle = viewModel::setReplayGain)
        }

        SettingsSection(
            title = "Network",
            subtitle = "Streams, TMDB metadata",
            icon = Icons.Rounded.Cloud,
            color = LumenCyan,
            expanded = s.sectionNetwork,
            onToggle = { viewModel.setSectionExpanded("network", !s.sectionNetwork) }
        ) {
            ClickableRow(
                title = "Open Network Stream",
                subtitle = "Enter a URL to stream",
                onClick = { },
                icon = Icons.Rounded.Wifi
            )
            SwitchRow(
                title = "TMDB Metadata",
                summary = "Download posters and descriptions from TMDB",
                checked = s.fetchOnlineMetadata,
                onToggle = viewModel::setFetchOnlineMetadata
            )
            if (s.fetchOnlineMetadata) {
                if (!viewModel.isTmdbApiAvailable) {
                    Text(
                        "TMDB API key not set. Metadata will not be fetched.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(start = 56.dp, end = 16.dp, bottom = 8.dp)
                    )
                }
                SwitchRow(title = "Wi-Fi only", summary = "Only download metadata on Wi-Fi", checked = s.metadataWifiOnly, onToggle = viewModel::setMetadataWifiOnly)
            }
        }

        SettingsSection(
            title = "Advanced",
            subtitle = "Cache, reset all settings",
            icon = Icons.Rounded.Settings,
            color = LumenRed,
            expanded = s.sectionAdvanced,
            onToggle = { viewModel.setSectionExpanded("advanced", !s.sectionAdvanced) }
        ) {
            ClickableRow(
                title = "Clear metadata cache",
                subtitle = "Remove cached poster URLs",
                onClick = { showClearCacheDialog = true },
                icon = Icons.Rounded.Delete
            )
            ClickableRow(
                title = "Reset All Settings",
                subtitle = "Restore defaults",
                onClick = { showResetSettingsDialog = true },
                icon = Icons.Rounded.RestartAlt,
                iconTint = MaterialTheme.colorScheme.error,
                titleColor = MaterialTheme.colorScheme.error
            )
            ClickableRow(
                title = "Reset Onboarding",
                subtitle = "Show welcome screen on next launch",
                onClick = { showResetOnboardingDialog = true },
                icon = Icons.Rounded.Refresh
            )
        }

        SettingsSection(
            title = "About",
            subtitle = "Version, licenses, source code",
            icon = Icons.Rounded.Info,
            color = MaterialTheme.colorScheme.onSurface,
            expanded = s.sectionAbout,
            onToggle = { viewModel.setSectionExpanded("about", !s.sectionAbout) }
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.lumen_logo_rounded),
                    contentDescription = null,
                    modifier = Modifier.size(80.dp)
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "Lumen",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "v1.0.0",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "A premium local-first media player",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
            AboutRow(title = "Version", value = "${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})")
            val ctx = LocalContext.current
            ClickableRow(title = "Open Source Licenses", subtitle = "View third-party licenses", onClick = {
                try {
                    val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", ctx.packageName, null)
                    }
                    ctx.startActivity(intent)
                } catch (_: Exception) { }
            })
            ClickableRow(title = "GitHub", subtitle = "View source code", onClick = {
                try {
                    ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/anomalco/lumen")))
                } catch (_: Exception) { }
            }, chevronIcon = Icons.AutoMirrored.Rounded.OpenInNew)
        }
    }

    if (showThemePicker) {
        LumenPickerDialog(
            onDismiss = { showThemePicker = false },
            title = "Theme",
            options = listOf(ThemeMode.SYSTEM, ThemeMode.LIGHT, ThemeMode.DARK),
            selectedValue = s.themeMode,
            optionLabel = { it.name.lowercase().replaceFirstChar { c -> c.uppercase() } },
            onPick = { viewModel.setThemeMode(it); showThemePicker = false }
        )
    }
    if (showFontSizePicker) {
        LumenPickerDialog(
            onDismiss = { showFontSizePicker = false },
            title = "Font Size",
            options = listOf("SMALL", "DEFAULT", "LARGE"),
            selectedValue = s.fontSize,
            optionLabel = { it.lowercase().replaceFirstChar { c -> c.uppercase() } },
            onPick = { viewModel.setFontSize(it); showFontSizePicker = false }
        )
    }
    if (showAccentColorPicker) {
        AccentColorPickerDialog(
            currentHex = s.accentColorHex,
            onDismiss = { showAccentColorPicker = false },
            onPick = { hex ->
                viewModel.setAccentColorHex(hex)
                showAccentColorPicker = false
            }
        )
    }
    if (showAspectRatioPicker) {
        LumenPickerDialog(
            onDismiss = { showAspectRatioPicker = false },
            title = "Default Aspect Ratio",
            options = listOf("AUTO", "R16_9", "R4_3", "STRETCH"),
            selectedValue = s.aspectRatio,
            optionLabel = { it.replace("_", ":").replace("R", "") },
            onPick = { viewModel.setAspectRatio(it); showAspectRatioPicker = false }
        )
    }
    if (showBrightnessPicker) {
        LumenPickerDialog(
            onDismiss = { showBrightnessPicker = false },
            title = "Brightness Mode",
            options = listOf("SYSTEM", "MANUAL", "AUTO"),
            selectedValue = s.brightnessMode,
            optionLabel = { it.lowercase().replaceFirstChar { c -> c.uppercase() } },
            onPick = { viewModel.setBrightnessMode(it); showBrightnessPicker = false }
        )
    }
    if (showSubtitleColorPicker) {
        LumenPickerDialog(
            onDismiss = { showSubtitleColorPicker = false },
            title = "Subtitle Color",
            options = listOf("WHITE", "YELLOW", "CYAN", "PINK"),
            selectedValue = s.subtitleColor,
            optionLabel = { it.lowercase().replaceFirstChar { c -> c.uppercase() } },
            onPick = { viewModel.setSubtitleColor(it); showSubtitleColorPicker = false }
        )
    }
    if (showLanguagePicker) {
        val languages = listOf("English", "Spanish", "French", "German", "Italian", "Portuguese", "Russian", "Japanese", "Korean", "Chinese", "Arabic", "Hindi", "Turkish")
        LumenPickerDialog(
            onDismiss = { showLanguagePicker = false },
            title = "Subtitle Language",
            options = languages,
            selectedValue = s.subtitleLanguage,
            optionLabel = { it },
            onPick = { viewModel.setSubtitleLanguage(it); showLanguagePicker = false }
        )
    }
    if (showSubtitleTextSizeDialog) {
        SubtitleTextSizeSlider(
            currentSize = s.subtitleTextSize,
            onDismiss = { showSubtitleTextSizeDialog = false },
            onConfirm = { viewModel.setSubtitleTextSize(it); showSubtitleTextSizeDialog = false }
        )
    }
    if (showSubtitleDelayDialog) {
        SubtitleDelaySlider(
            currentDelayUs = s.subtitleDelayUs,
            onDismiss = { showSubtitleDelayDialog = false },
            onConfirm = { viewModel.setSubtitleDelayUs(it); showSubtitleDelayDialog = false }
        )
    }
    if (showOpenSubsKeyDialog) {
        LumenInputDialog(
            onDismiss = { showOpenSubsKeyDialog = false },
            onConfirm = { viewModel.setOpensubtitlesApiKey(it); showOpenSubsKeyDialog = false },
            title = "OpenSubtitles API Key",
            initialValue = s.opensubtitlesApiKey,
            label = "API Key"
        )
    }
    if (showClearCacheDialog) {
        LumenConfirmationDialog(
            title = "Clear metadata cache?",
            body = "This will remove all cached poster URLs. They will be re-fetched from TMDB on the next library scan.",
            confirmLabel = "Clear",
            onConfirm = { viewModel.clearMetadataCache(); showClearCacheDialog = false },
            onDismiss = { showClearCacheDialog = false }
        )
    }
    if (showResetSettingsDialog) {
        LumenConfirmationDialog(
            title = "Reset All Settings?",
            body = "All preferences will be restored to their default values. Media data will not be affected.",
            confirmLabel = "Reset",
            onConfirm = { viewModel.resetAllSettings(); showResetSettingsDialog = false },
            onDismiss = { showResetSettingsDialog = false }
        )
    }
    if (showResetOnboardingDialog) {
        LumenConfirmationDialog(
            title = "Reset Onboarding?",
            body = "The welcome screen will appear on the next launch.",
            confirmLabel = "Reset",
            onConfirm = { viewModel.resetOnboarding(); showResetOnboardingDialog = false },
            onDismiss = { showResetOnboardingDialog = false }
        )
    }
}

@Composable
private fun SubtitleTextSizeSlider(
    currentSize: Float,
    onDismiss: () -> Unit,
    onConfirm: (Float) -> Unit
) {
    var size by remember { mutableStateOf(currentSize) }
    LumenPickerDialog(
        onDismiss = onDismiss,
        title = "Subtitle Text Size",
        options = listOf(12f, 14f, 16f, 18f, 20f, 24f, 28f, 32f, 36f),
        selectedValue = size,
        optionLabel = { "${it.toInt()}sp" },
        onPick = { onConfirm(it) }
    )
}

@Composable
private fun SubtitleDelaySlider(
    currentDelayUs: Long,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    var delayMs by remember { mutableStateOf(currentDelayUs / 1000) }
    LumenPickerDialog(
        onDismiss = onDismiss,
        title = "Subtitle Delay",
        options = listOf(-5000L, -3000L, -1000L, 0L, 1000L, 3000L, 5000L),
        selectedValue = delayMs,
        optionLabel = { "${it}ms" },
        onPick = { onConfirm(it * 1000) }
    )
}

@Composable
private fun SettingsSection(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    expanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    val iconEnd = when (icon) {
        Icons.Rounded.Palette -> LumenPurpleDeep
        Icons.Rounded.PlayCircle -> LumenGreenDeep
        Icons.Rounded.Movie -> LumenBlueDeep
        Icons.Rounded.Subtitles -> LumenYellowDeep
        Icons.Rounded.LibraryMusic -> LumenOrangeDeep
        Icons.Rounded.MusicNote -> LumenPinkDeep
        Icons.Rounded.Cloud -> LumenCyanDeep
        Icons.Rounded.Settings -> LumenRedDeep
        Icons.Rounded.Info -> LumenDarkOnSurfaceVariant
        else -> LumenPurpleDeep
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle)
                    .height(72.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ShapeIcon(
                    icon = icon,
                    gradientStart = color,
                    gradientEnd = iconEnd
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleMedium,
                        color = color
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    if (expanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            AnimatedVisibility(visible = expanded) {
                Column {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(Modifier.height(4.dp))
                    content()
                    Spacer(Modifier.height(4.dp))
                }
            }
        }
    }
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
            .defaultMinSize(minHeight = 56.dp)
            .clickable { onToggle(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f).padding(end = 16.dp)) {
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
private fun ClickableRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    icon: ImageVector? = null,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    titleColor: Color? = null,
    chevronIcon: ImageVector = Icons.Rounded.ArrowForwardIos
) {
    Row(
        Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 56.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = iconTint)
            Spacer(Modifier.width(12.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge,
                color = titleColor ?: MaterialTheme.colorScheme.onSurface
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.width(8.dp))
        Icon(
            chevronIcon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun AccentColorRow(
    hex: String,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 56.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Accent Color", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(Color(android.graphics.Color.parseColor(hex)))
                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            hex,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(8.dp))
        Icon(
            Icons.Rounded.ArrowForwardIos,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun AboutRow(
    title: String,
    value: String
) {
    Row(
        Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 56.dp)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private val ACCENT_COLORS = listOf(
    "#00E5C8" to "Teal",
    "#FF4D6D" to "Rose",
    "#7C5CFF" to "Purple",
    "#FFB347" to "Amber",
    "#4CAF50" to "Green",
    "#2196F3" to "Blue",
    "#FF6B9D" to "Pink",
    "#FFFFFF" to "White"
)

@Composable
private fun AccentColorPickerDialog(
    currentHex: String,
    onDismiss: () -> Unit,
    onPick: (String) -> Unit
) {
    var customHex by remember { mutableStateOf(currentHex) }
    var showCustom by remember { mutableStateOf(currentHex !in ACCENT_COLORS.map { it.first }) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.85f),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            shadowElevation = 8.dp
        ) {
            Column(Modifier.padding(24.dp)) {
                Text("Accent Color", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(16.dp))
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(ACCENT_COLORS, key = { it.first }) { (hex, name) ->
                        val isSelected = hex == currentHex
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { onPick(hex) }
                        ) {
                            Box(
                                Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color(android.graphics.Color.parseColor(hex)))
                                    .then(
                                        if (isSelected)
                                            Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                        else Modifier
                                    )
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                name, style = MaterialTheme.typography.labelSmall,
                                textAlign = TextAlign.Center,
                                color = if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                TextButton(onClick = { showCustom = !showCustom }) {
                    Text(if (showCustom) "Hide custom" else "Custom hex color")
                }
                if (showCustom) {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = customHex,
                            onValueChange = { customHex = it },
                            label = { Text("#RRGGBB") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        FilledTonalButton(
                            onClick = { onPick(customHex) },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Apply")
                        }
                    }
                }
            }
        }
    }
}
