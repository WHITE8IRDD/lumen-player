package com.lumen.player.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.Icons
import com.lumen.player.ui.components.LumenSquircle
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.ui.draw.rotate
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lumen.player.BuildConfig
import com.lumen.player.R
import com.lumen.player.core.theme.LumenAmber
import com.lumen.player.core.theme.LumenBorderSubtle

import com.lumen.player.ui.components.LumenConfirmationDialog
import com.lumen.player.ui.components.LumenInputDialog
import com.lumen.player.player.audio.VolumeBoostController
import com.lumen.player.ui.components.LumenPickerDialog
import com.lumen.player.ui.theme.LumenFontChoice
import com.lumen.player.ui.theme.LumenTextCase
import com.lumen.player.ui.theme.LumenText
import com.lumen.player.ui.theme.LocalLumenTextCase
import com.lumen.player.ui.theme.applyLumenCase
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Slider
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import kotlin.math.roundToInt
import androidx.compose.material3.SliderDefaults


@Composable
fun SettingsScreen(
    onOpenEqualizer: () -> Unit,
    onOpenHiddenItems: () -> Unit = {},
    onDebugBlob: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val s by viewModel.settings.collectAsStateWithLifecycle()

    var showFontSizePicker by remember { mutableStateOf(false) }
    var showAccentColorPicker by remember { mutableStateOf(false) }
    var showAspectRatioPicker by remember { mutableStateOf(false) }
    var showBrightnessPicker by remember { mutableStateOf(false) }
    var showSubtitleColorPicker by remember { mutableStateOf(false) }
    var showLanguagePicker by remember { mutableStateOf(false) }
    var showSubtitleDelayDialog by remember { mutableStateOf(false) }
    var showSubtitleTextSizeDialog by remember { mutableStateOf(false) }
    var showClearCacheDialog by remember { mutableStateOf(false) }
    var showResetSettingsDialog by remember { mutableStateOf(false) }
    var showResetOnboardingDialog by remember { mutableStateOf(false) }
    var showVolumeBoostPicker by remember { mutableStateOf(false) }
    val boostStatus by viewModel.boostEngineStatus.collectAsStateWithLifecycle()

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 40.dp, bottom = 8.dp)
    ) {
        Text(
            "Settings",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 32.dp)
        )

        // ── PLAYBACK GROUP ──
        SettingsSectionHeader(
            title =             "Playback",
            subtitle = "Resume, focus, gapless",
            expanded = s.sectionPlayback,
            onToggle = { viewModel.setSectionExpanded("playback", !s.sectionPlayback) }
        )
        AnimatedVisibility(visible = s.sectionPlayback) {
            Column {
                SettingsDivider()
                SettingsSwitchRow(title = stringResource(R.string.resume_playback), summary = stringResource(R.string.resume_playback_summary), checked = s.resumePlayback, onToggle = viewModel::setResumePlayback)
                SettingsDivider()
                SettingsSwitchRow(title = stringResource(R.string.audio_focus), summary = stringResource(R.string.audio_focus_summary), checked = s.respectAudioFocus, onToggle = viewModel::setRespectAudioFocus)
                SettingsDivider()
                SettingsSwitchRow(title = stringResource(R.string.gapless_playback), summary = null, checked = s.gaplessPlayback, onToggle = viewModel::setGapless)
                SettingsDivider()
                SettingsSwitchRow(title = stringResource(R.string.skip_silence), summary = null, checked = s.skipSilence, onToggle = viewModel::setSkipSilence)
            }
        }
        SettingsDivider()

        // ── VOLUME GROUP ──
        SettingsSectionHeader(
            title = "Volume",
            subtitle = "Boost above 100%",
            expanded = s.sectionVolume,
            onToggle = { viewModel.setSectionExpanded("volume", !s.sectionVolume) }
        )
        AnimatedVisibility(visible = s.sectionVolume) {
            Column {
                SettingsDivider()
                SettingsSwitchRow(
                    title = "Activate boost",
                    summary = "Raise the volume above 100% in the music and video players. Very high levels can distort.",
                    checked = s.volumeBoostEnabled,
                    onToggle = viewModel::setVolumeBoostEnabled
                )
                AnimatedVisibility(visible = s.volumeBoostEnabled) {
                    Column {
                        SettingsDivider()
                        SettingsClickableRow(
                            title = "Maximum boost",
                            subtitle = "${s.volumeBoostMaxPercent}%",
                            onClick = { showVolumeBoostPicker = true }
                        )
                        SettingsDivider()
                        SettingsStatusRow(title = "Boost status", subtitle = boostStatus)
                    }
                }
            }
        }
        SettingsDivider()

        SettingsSectionHeader(
            title =             "Video",
            subtitle = "Hardware acceleration, aspect ratio",
            expanded = s.sectionVideo,
            onToggle = { viewModel.setSectionExpanded("video", !s.sectionVideo) }
        )
        AnimatedVisibility(visible = s.sectionVideo) {
            Column {
                SettingsDivider()
                SettingsSwitchRow(title = "Hardware acceleration", summary = "Prefer hardware decoders for video playback", checked = s.hardwareAcceleration, onToggle = viewModel::setHardwareAcceleration)
                SettingsDivider()
                SettingsClickableRow(title = "Default aspect ratio", subtitle = s.aspectRatio.replace("_", ":").replace("R", ""), onClick = { showAspectRatioPicker = true })
                SettingsDivider()
                SettingsClickableRow(title = "Brightness mode", subtitle = s.brightnessMode.lowercase().replaceFirstChar { it.uppercase() }, onClick = { showBrightnessPicker = true })
            }
        }
        SettingsDivider()

        SettingsSectionHeader(
            title =             "Audio",
            subtitle = "Equalizer, replay gain",
            expanded = s.sectionAudio,
            onToggle = { viewModel.setSectionExpanded("audio", !s.sectionAudio) }
        )
        AnimatedVisibility(visible = s.sectionAudio) {
            Column {
                SettingsDivider()
                SettingsClickableRow(title = stringResource(R.string.equalizer), subtitle = "EQ, bass, virtualizer, loudness", onClick = onOpenEqualizer)
                SettingsDivider()
                SettingsSwitchRow(title = "Replay gain", summary = "Normalize volume across tracks", checked = s.replayGain, onToggle = viewModel::setReplayGain)
            }
        }
        SettingsDivider()

        SettingsSectionHeader(
            title =             "Subtitles",
            subtitle = "Text size, color, language, delay",
            expanded = s.sectionSubtitles,
            onToggle = { viewModel.setSectionExpanded("subtitles", !s.sectionSubtitles) }
        )
        AnimatedVisibility(visible = s.sectionSubtitles) {
            Column {
                SettingsDivider()
                SettingsClickableRow(title = "Default text size", subtitle = "${s.subtitleTextSize.toInt()}sp", onClick = { showSubtitleTextSizeDialog = true })
                SettingsDivider()
                SettingsClickableRow(title = "Default text color", subtitle = s.subtitleColor.lowercase().replaceFirstChar { it.uppercase() }, onClick = { showSubtitleColorPicker = true })
                SettingsDivider()
                SettingsClickableRow(title = "Default delay", subtitle = "${s.subtitleDelayUs / 1000}ms", onClick = { showSubtitleDelayDialog = true })
                SettingsDivider()
                SettingsClickableRow(title = "Default language", subtitle = s.subtitleLanguage, onClick = { showLanguagePicker = true })
            }
        }
        SettingsDivider()

        // ── LIBRARY GROUP ──
        SettingsSectionHeader(
            title =             "Library",
            subtitle = "Excluded folders, auto-scan",
            expanded = s.sectionLibrary,
            onToggle = { viewModel.setSectionExpanded("library", !s.sectionLibrary) }
        )
        AnimatedVisibility(visible = s.sectionLibrary) {
            Column {
                SettingsDivider()
                SettingsClickableRow(title = "Excluded folders", subtitle = "Manage hidden folders", onClick = onOpenHiddenItems)
                SettingsDivider()
                SettingsSwitchRow(title = "Auto-scan on startup", summary = null, checked = s.autoScanOnStartup, onToggle = viewModel::setAutoScanOnStartup)
                SettingsDivider()
                SettingsSwitchRow(title = stringResource(R.string.show_videos), summary = null, checked = s.showVideos, onToggle = viewModel::setShowVideos)
                SettingsDivider()
                SettingsSwitchRow(title = "Show audio", summary = null, checked = s.showAudio, onToggle = viewModel::setShowAudio)
            }
        }
        SettingsDivider()

        SettingsSectionHeader(
            title =             "Network",
            subtitle = "Streams, TMDB metadata",
            expanded = s.sectionNetwork,
            onToggle = { viewModel.setSectionExpanded("network", !s.sectionNetwork) }
        )
        AnimatedVisibility(visible = s.sectionNetwork) {
            Column {
                SettingsDivider()
                SettingsClickableRow(title = "Open network stream", subtitle = "Enter a URL to stream", onClick = { })
                SettingsDivider()
                SettingsSwitchRow(title = "TMDB metadata", summary = "Download posters and descriptions from TMDB", checked = s.fetchOnlineMetadata, onToggle = viewModel::setFetchOnlineMetadata)
                if (s.fetchOnlineMetadata) {
                    if (!viewModel.isTmdbApiAvailable) {
                        Text(
                            "TMDB API key not set. Metadata will not be fetched.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
                        )
                    }
                    SettingsDivider()
                    SettingsSwitchRow(title = "Wi-Fi only", summary = "Only download metadata on Wi-Fi", checked = s.metadataWifiOnly, onToggle = viewModel::setMetadataWifiOnly)
                }
            }
        }
        SettingsDivider()

        // ── APPEARANCE GROUP ──
        Spacer(Modifier.height(32.dp))

        SettingsSectionHeader(
            title =             "Appearance",
            subtitle = "Theme, colors, typography",
            expanded = s.sectionAppearance,
            onToggle = { viewModel.setSectionExpanded("appearance", !s.sectionAppearance) }
        )
        AnimatedVisibility(visible = s.sectionAppearance) {
            Column {
                SettingsDivider()
                SettingsClickableRow(title = "Font size", subtitle = s.fontSize.lowercase().replaceFirstChar { it.uppercase() }, onClick = { showFontSizePicker = true })
                SettingsDivider()
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { showAccentColorPicker = true }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Accent Color", style = MaterialTheme.typography.bodyLarge)
                        Text(s.accentColorHex, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    val parsedColor = remember(s.accentColorHex) {
                        try { Color(android.graphics.Color.parseColor(s.accentColorHex)) }
                        catch (_: Exception) { Color(0xFF00E5C8) }
                    }
                    Box(
                        Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(parsedColor)
                            .border(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), CircleShape)
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        Icons.Rounded.ChevronRight,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
                SettingsDivider()
                TypographySection(
                    currentFontChoice = try { LumenFontChoice.valueOf(s.fontFamilyChoice) } catch (_: Exception) { LumenFontChoice.SERIF_DISPLAY },
                    currentWeightOffset = s.weightOffset,
                    onFontChoice = { viewModel.setFontFamilyChoice(it.name) },
                    onWeightOffset = { viewModel.setWeightOffset(it) }
                )
            }
        }
        SettingsDivider()

        // ── SYSTEM GROUP ──
        Spacer(Modifier.height(32.dp))

        SettingsSectionHeader(
            title =             "Advanced",
            subtitle = "Cache, reset all settings",
            expanded = s.sectionAdvanced,
            onToggle = { viewModel.setSectionExpanded("advanced", !s.sectionAdvanced) }
        )
        AnimatedVisibility(visible = s.sectionAdvanced) {
            Column {
                SettingsDivider()
                SettingsClickableRow(title = "Clear metadata cache", subtitle = "Remove cached poster URLs", onClick = { showClearCacheDialog = true })
                SettingsDivider()
                SettingsClickableRow(title = "Reset all settings", subtitle = "Restore defaults", onClick = { showResetSettingsDialog = true })
                SettingsDivider()
                SettingsClickableRow(title = "Reset onboarding", subtitle = "Show welcome screen on next launch", onClick = { showResetOnboardingDialog = true })
            }
        }
        SettingsDivider()

        SettingsSectionHeader(
            title =             "About",
            subtitle = "Version, social links",
            expanded = s.sectionAbout,
            onToggle = { viewModel.setSectionExpanded("about", !s.sectionAbout) }
        )
        AnimatedVisibility(visible = s.sectionAbout) {
            Column {
                SettingsDivider()
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
                        "Your media. Your rules.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
                SettingsDivider()
                AboutRow(title = "Version", value = "${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})")
                SettingsDivider()
                val ctx = LocalContext.current
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/acee.visuals/")))
                        },
                        modifier = Modifier
                            .size(56.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_instagram),
                            contentDescription = "Instagram",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(Modifier.width(24.dp))
                    IconButton(
                        onClick = {
                            ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/WHITE8IRDD/lumen-player.git")))
                        },
                        modifier = Modifier
                            .size(56.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_github),
                            contentDescription = "GitHub",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                Text(
                    text = "Powered by 𝘈𝘝® engine",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    fontStyle = FontStyle.Italic,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp, bottom = 16.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    if (showFontSizePicker) {
        LumenPickerDialog(
            onDismiss = { showFontSizePicker = false },
            title = "Font size",
            options = listOf("SMALL", "DEFAULT", "LARGE"),
            selectedValue = s.fontSize,
            optionLabel = { it.lowercase() },
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
            title = "Default aspect ratio",
            options = listOf("AUTO", "R16_9", "R4_3", "STRETCH"),
            selectedValue = s.aspectRatio,
            optionLabel = { it.replace("_", ":").replace("R", "").lowercase() },
            onPick = { viewModel.setAspectRatio(it); showAspectRatioPicker = false }
        )
    }
    if (showBrightnessPicker) {
        LumenPickerDialog(
            onDismiss = { showBrightnessPicker = false },
            title = "Brightness mode",
            options = listOf("SYSTEM", "MANUAL", "AUTO"),
            selectedValue = s.brightnessMode,
            optionLabel = { it.lowercase() },
            onPick = { viewModel.setBrightnessMode(it); showBrightnessPicker = false }
        )
    }
    if (showSubtitleColorPicker) {
        LumenPickerDialog(
            onDismiss = { showSubtitleColorPicker = false },
            title = "Subtitle color",
            options = listOf("WHITE", "YELLOW", "CYAN", "PINK"),
            selectedValue = s.subtitleColor,
            optionLabel = { it.lowercase() },
            onPick = { viewModel.setSubtitleColor(it); showSubtitleColorPicker = false }
        )
    }
    if (showLanguagePicker) {
        val languages = listOf("English", "Spanish", "French", "German", "Italian", "Portuguese", "Russian", "Japanese", "Korean", "Chinese", "Arabic", "Hindi", "Turkish")
        LumenPickerDialog(
            onDismiss = { showLanguagePicker = false },
            title = "Subtitle language",
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
            title = "Reset all settings?",
            body = "All preferences will be restored to their default values. Media data will not be affected.",
            confirmLabel = "Reset",
            onConfirm = { viewModel.resetAllSettings(); showResetSettingsDialog = false },
            onDismiss = { showResetSettingsDialog = false }
        )
    }
    if (showResetOnboardingDialog) {
        LumenConfirmationDialog(
            title = "Reset onboarding?",
            body = "The welcome screen will appear on the next launch.",
            confirmLabel = "Reset",
            onConfirm = { viewModel.resetOnboarding(); showResetOnboardingDialog = false },
            onDismiss = { showResetOnboardingDialog = false }
        )

        if (showVolumeBoostPicker) {
            LumenPickerDialog(
                onDismiss = { showVolumeBoostPicker = false },
                title = "Maximum boost",
                options = VolumeBoostController.MAX_PERCENT_OPTIONS,
                selectedValue = s.volumeBoostMaxPercent,
                optionLabel = { "$it%" },
                onPick = {
                    viewModel.setVolumeBoostMaxPercent(it)
                    showVolumeBoostPicker = false
                }
            )
        }
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
        title = "Subtitle text size",
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
        title = "Subtitle delay",
        options = listOf(-5000L, -3000L, -1000L, 0L, 1000L, 3000L, 5000L),
        selectedValue = delayMs,
        optionLabel = { "${it}ms" },
        onPick = { onConfirm(it * 1000) }
    )
}

@Composable
private fun SettingsSectionHeader(
    title: String,
    subtitle: String,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "chevron"
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onToggle
            )
            .defaultMinSize(minHeight = 56.dp)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.labelLarge.copy(
                    letterSpacing = 0.5.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(2.dp))
            Text(
                subtitle,
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 0.sp,
                    fontWeight = FontWeight.Normal
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            Icons.Rounded.KeyboardArrowDown,
            contentDescription = if (expanded) "Collapse" else "Expand",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp).rotate(chevronRotation)
        )
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        thickness = 0.5.dp,
        color = LumenBorderSubtle,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}

/** Read-only row: a title with a status line underneath. */
@Composable
private fun SettingsStatusRow(
    title: String,
    subtitle: String
) {
    Column(
        Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Text(
            title,
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(2.dp))
        Text(
            subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    summary: String?,
    checked: Boolean,
    onToggle: (Boolean) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { onToggle(!checked) }
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f).padding(end = 16.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp)
            )
            if (!summary.isNullOrEmpty()) {
                Spacer(Modifier.height(2.dp))
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
private fun SettingsClickableRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    chevronIcon: ImageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos
) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(2.dp))
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
        Icon(
            Icons.Rounded.ChevronRight,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun AccentColorRow(
    hex: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "accent color",
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
            modifier = Modifier.weight(1f)
        )
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(Color(android.graphics.Color.parseColor(hex)))
                .border(1.dp, LumenBorderSubtle, CircleShape)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            hex,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(8.dp))
        Icon(
            Icons.AutoMirrored.Rounded.ArrowForwardIos,
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
            .defaultMinSize(minHeight = 48.dp)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
            modifier = Modifier.weight(1f)
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private val ACCENT_COLORS = listOf(
    "#F5F5F5" to "light white",
    "#FFAB40" to "amber",
    "#FF4D6D" to "rose",
    "#7C5CFF" to "purple",
    "#FFB347" to "warm",
    "#4CAF50" to "green",
    "#2196F3" to "blue",
    "#FF6B9D" to "pink",
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
            shape = LumenSquircle(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            shadowElevation = 8.dp
        ) {
            Column(Modifier.padding(24.dp)) {
                Text("Accent color", style = MaterialTheme.typography.titleLarge)
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
                            label = { Text("#rrggbb") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = LumenSquircle(12.dp)
                        )
                        FilledTonalButton(
                            onClick = { onPick(customHex) },
                            shape = LumenSquircle(12.dp)
                        ) {
                            Text("Apply")
                        }
                    }
                }
            }
        }
    }
}

private val weightNames = listOf("Light", "Normal", "Medium", "SemiBold", "Bold")

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TypographySection(
    currentFontChoice: LumenFontChoice,
    currentWeightOffset: Int,
    onFontChoice: (LumenFontChoice) -> Unit,
    onWeightOffset: (Int) -> Unit
) {
    SettingsDivider()
    Spacer(Modifier.height(8.dp))

    LumenText(
        value = "font",
        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
        color = MaterialTheme.colorScheme.secondary,
        modifier = Modifier.padding(start = 16.dp, bottom = 10.dp)
    )

    LazyRow(
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(LumenFontChoice.entries, key = { it.name }) { choice ->
            val isSelected = choice == currentFontChoice
            Surface(
                onClick = { onFontChoice(choice) },
                shape = LumenSquircle(16.dp),
                border = BorderStroke(
                    if (isSelected) 2.dp else 1.dp,
                    if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                ),
                color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                else Color.Transparent,
                modifier = Modifier.width(96.dp).height(80.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(6.dp)
                ) {
                    Text(
                        "Aa",
                        fontFamily = choice.family,
                        fontSize = 28.sp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        choice.displayName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }

    Spacer(Modifier.height(20.dp))

    LumenText(
        value = "${weightNames[currentWeightOffset.coerceIn(0, 4)]}",
        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
        color = MaterialTheme.colorScheme.secondary,
        modifier = Modifier.padding(start = 16.dp, bottom = 4.dp)
    )

    Slider(
        value = currentWeightOffset.toFloat(),
        onValueChange = { onWeightOffset(it.roundToInt()) },
        valueRange = 0f..4f,
        steps = 3,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = SliderDefaults.colors(
            thumbColor = MaterialTheme.colorScheme.primary,
            activeTrackColor = MaterialTheme.colorScheme.primary,
            inactiveTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        weightNames.forEach { name ->
            Text(
                name,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }

    Spacer(Modifier.height(20.dp))

    Surface(
        shape = LumenSquircle(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        LumenText(
            value = "The quick brown fox jumps over the lazy dog",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(16.dp)
        )
    }

    Spacer(Modifier.height(8.dp))
}
