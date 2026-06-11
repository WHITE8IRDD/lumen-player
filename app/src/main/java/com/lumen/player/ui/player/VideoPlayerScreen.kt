package com.lumen.player.ui.player

import android.app.Activity
import android.app.PictureInPictureParams
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Rational
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.AspectRatio
import androidx.compose.material.icons.rounded.Brightness6
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ClosedCaption
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.FastRewind
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.LiveTv
import androidx.compose.material.icons.rounded.PictureInPicture
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import com.lumen.player.ui.components.LumenPickerDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.lumen.player.core.util.TimeFormatter
import com.lumen.player.domain.model.NetworkStream
import com.lumen.player.ui.components.FloatingGlassCard
import com.lumen.player.ui.components.LumenConfirmationDialog
import com.lumen.player.ui.library.StreamsViewModel
import com.lumen.player.ui.subtitles.SubtitlePanelSheet
import kotlin.math.abs

@UnstableApi
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoPlayerScreen(
    onClose: () -> Unit,
    onEnterPip: () -> Unit = {},
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val player = viewModel.exoPlayer
    val state by viewModel.state.collectAsStateWithLifecycle()
    val tracks by viewModel.tracks.collectAsStateWithLifecycle()
    val activity = context as? Activity
    val haptic = LocalHapticFeedback.current
    val streamsVm: StreamsViewModel = hiltViewModel()

    // ---- Player state ----
    var controlsVisible by remember { mutableStateOf(true) }
    var zoom by remember { mutableFloatStateOf(1f) }
    var resizeMode by remember { mutableStateOf(AspectRatioFrameLayout.RESIZE_MODE_FIT) }
    var uiMode by remember { mutableStateOf(VideoUiMode.MAIN) }
    var locked by remember { mutableStateOf(false) }
    var lockOverlayVisible by remember { mutableStateOf(false) }
    var lockRevealToken by remember { mutableStateOf(0) }
    var showSubtitles by remember { mutableStateOf(false) }
    var subtitleDelayUs by remember { mutableStateOf(0L) }
    var seekDragging by remember { mutableStateOf(false) }
    var seekPreviewMs by remember { mutableStateOf<Float?>(null) }
    var showChannelSwitcher by remember { mutableStateOf(false) }
    var channelSwitcherQuery by remember { mutableStateOf("") }

    // ---- Brightness / volume (window + AudioManager) ----
    val audioManager = remember(context) {
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }
    val maxVolume = remember(audioManager) {
        audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
    }
    var volumeFraction by remember {
        mutableFloatStateOf(
            audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / maxVolume
        )
    }
    val brightnessKeeper = remember { BrightnessKeeper(context) }
    // Read the current system brightness on entry so we don't snap to 0.5.
    // Range maps from 0..255 (system value) to 0.05..1.0 (window value).
    val systemBrightness = remember {
        val raw = try {
            Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS)
        } catch (_: Settings.SettingNotFoundException) {
            128
        }
        (raw.coerceIn(0, 255) / 255f).coerceIn(0.05f, 1f)
    }
    var windowBrightness by remember { mutableFloatStateOf(systemBrightness) }
    var brightnessHud by remember { mutableStateOf<HudInfo?>(null) }
    var volumeHud by remember { mutableStateOf<HudInfo?>(null) }
    var seekFlash by remember { mutableStateOf<SeekFlash?>(null) }

    // ---- Save / restore system brightness (window-only, never leaks) ----
    DisposableEffect(Unit) {
        brightnessKeeper.save()
        onDispose {
            brightnessKeeper.restore()
            viewModel.setSpeedPitched(1f)
        }
    }
    LaunchedEffect(windowBrightness) { brightnessKeeper.setWindowBrightness(windowBrightness) }

    // Derived state used by DisposableEffect below — must be declared before
    // use because Kotlin does not allow forward references in the same scope.
    val isPlaying by remember { derivedStateOf { state.isPlaying } }

    // ---- Keep screen on — tied to playback state ----
    DisposableEffect(isPlaying) {
        if (isPlaying) {
            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose { activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
    }

    // ---- Save / restore rotation ----
    DisposableEffect(Unit) {
        val original = activity?.requestedOrientation
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
        } else {
            @Suppress("DEPRECATION")
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
        }
        onDispose { if (original != null) activity.requestedOrientation = original }
    }

    // ---- Immersive mode (hide system bars) ----
    DisposableEffect(Unit) {
        val window = activity?.window ?: return@DisposableEffect onDispose {}
        WindowCompat.setDecorFitsSystemWindows(window, false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.apply {
                hide(android.view.WindowInsets.Type.systemBars())
                systemBarsBehavior = android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                android.view.View.SYSTEM_UI_FLAG_FULLSCREEN or
                android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            )
        }
        onDispose {
            WindowCompat.setDecorFitsSystemWindows(window, true)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.insetsController?.show(android.view.WindowInsets.Type.systemBars())
            } else {
                @Suppress("DEPRECATION")
                window.decorView.systemUiVisibility = android.view.View.SYSTEM_UI_FLAG_VISIBLE
            }
        }
    }

    // ---- Rotation lock when locked ----
    LaunchedEffect(locked) {
        if (locked) {
            @Suppress("DEPRECATION")
            val rotation = activity?.windowManager?.defaultDisplay?.rotation
            when (rotation) {
                android.view.Surface.ROTATION_0 -> activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                android.view.Surface.ROTATION_90 -> activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                android.view.Surface.ROTATION_180 -> activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
                android.view.Surface.ROTATION_270 -> activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
            }
        } else {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
        }
    }

    // ---- Lock overlay auto-hide after 1.5s, show for 2s on tap ----
    LaunchedEffect(locked) {
        if (locked) {
            lockOverlayVisible = true
            kotlinx.coroutines.delay(1_500)
            lockOverlayVisible = false
        } else {
            lockOverlayVisible = false
        }
    }

    // ---- 3s auto-hide for controls, 1s auto-hide for HUD ----
    LaunchedEffect(controlsVisible, locked) {
        if (controlsVisible && !locked) {
            kotlinx.coroutines.delay(3_000)
            controlsVisible = false
        }
    }
    LaunchedEffect(brightnessHud) { if (brightnessHud != null) { kotlinx.coroutines.delay(1_000); brightnessHud = null } }
    LaunchedEffect(volumeHud) { if (volumeHud != null) { kotlinx.coroutines.delay(1_000); volumeHud = null } }
    LaunchedEffect(seekFlash) { if (seekFlash != null) { kotlinx.coroutines.delay(700); seekFlash = null } }

    BackHandler { onClose() }

    // Derived state from the PlaybackState flow. Using derivedStateOf for
    // each field the parent reads ensures the parent Box only recomposes
    // when play/pause or track metadata changes — NOT on every 200ms tick.
    // Fields that change on every tick (positionMs, bufferedMs) are read
    // only inside sub-composables that receive state by lambda provider.
    val title by remember { derivedStateOf { state.title } }
    val artist by remember { derivedStateOf { state.artist } }
    val hasPrevious by remember { derivedStateOf { state.hasPrevious } }
    val hasNext by remember { derivedStateOf { state.hasNext } }
    val playbackSpeed by remember { derivedStateOf { state.playbackSpeed } }
    val channelContext by viewModel.streamContext.collectAsStateWithLifecycle()
    val showChannelButton = channelContext.isNotEmpty()

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            // === Single unified gesture surface ===
            // NOTE: No long-press 2x speed toggle. The gesture-driven 2x
            // feature was removed because it could not be made reliable:
            // a vertical brightness/volume drag that pauses mid-stroke would
            // trigger the long-press timer, jump to 2x, then restore to a
            // stale baseSpeed when the finger lifts. Speed adjustment is
            // exclusively available through the SpeedDialog (tap the speed
            // button in the control bar). This eliminates an entire class
            // of playback-speed corruption bugs.
            .pointerInput(locked) {
                if (locked) return@pointerInput
                var lastTapTime = 0L
                var lastTapX = -1f
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val downX = down.position.x
                    val downY = down.position.y
                    val width = size.width.toFloat()
                    val leftThird = width / 3f
                    val rightThird = width * 2f / 3f
                    val isLeftHalf = downX < width / 2f

                    var sawDrag = false
                    val startVolume = volumeFraction
                    val startBrightness = windowBrightness

                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Main)
                        val change = event.changes.firstOrNull() ?: break
                        if (!change.pressed) {
                            val duration = change.uptimeMillis - down.uptimeMillis
                            if (!sawDrag && duration < 300) {
                                // === TAP: single or double ===
                                val now = System.currentTimeMillis()
                                if (now - lastTapTime < 300 && abs(downX - lastTapX) < 100f) {
                                    // Double tap — seek ±10s or center = play/pause
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    when {
                                        downX < leftThird -> {
                                            viewModel.seekBy(-10_000)
                                            seekFlash = SeekFlash.Back
                                        }
                                        downX > rightThird -> {
                                            viewModel.seekBy(10_000)
                                            seekFlash = SeekFlash.Forward
                                        }
                                        else -> viewModel.playPause()
                                    }
                                    lastTapTime = 0L
                                } else {
                                    // Single tap — toggle controls
                                    controlsVisible = !controlsVisible
                                    lastTapTime = now
                                    lastTapX = downX
                                }
                            }
                            break
                        }

                        val dx = change.position.x - downX
                        val dy = change.position.y - downY
                        if (abs(dy) > 24f && abs(dy) > abs(dx)) {
                            sawDrag = true
                            if (isLeftHalf) {
                                windowBrightness = (startBrightness - dy / 400f).coerceIn(0.05f, 1f)
                                brightnessHud = HudInfo(Icons.Rounded.Brightness6,
                                    "${(windowBrightness * 100).toInt()}%", windowBrightness)
                            } else {
                                val newFrac = (startVolume - dy / 400f).coerceIn(0f, 1f)
                                val newVol = (newFrac * maxVolume).toInt().coerceIn(0, maxVolume)
                                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVol, 0)
                                volumeFraction = newFrac
                                volumeHud = HudInfo(Icons.AutoMirrored.Rounded.VolumeUp,
                                    "${(newFrac * 100).toInt()}%", newFrac)
                            }
                        }
                    }
                }
            }
    ) {
        // === Video surface ===
        if (player != null) {
            VideoSurface(
                player = player,
                zoomProvider = { zoom },
                resizeMode = resizeMode,
                onPinch = { newZoom -> zoom = newZoom.coerceIn(0.5f, 4f) }
            )
        }

        // === Unified controls overlay (top bar + bottom controls) ===
        AnimatedVisibility(
            visible = controlsVisible && !locked,
            enter = fadeIn(animationSpec = tween(200)),
            exit = fadeOut(animationSpec = tween(200)),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(Modifier.fillMaxSize()) {
                // Top gradient
                Box(
                    Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .background(
                            Brush.verticalGradient(
                                0f to Color.Black.copy(alpha = 0.85f),
                                1f to Color.Transparent
                            )
                        )
                        .padding(top = 32.dp, bottom = 16.dp, start = 4.dp, end = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(onClick = onClose) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back", tint = Color.White)
                        }
                        Spacer(Modifier.width(4.dp))
                        Text(
                            title.ifBlank { "Video" },
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        if (showChannelButton) {
                            IconButton(onClick = { showChannelSwitcher = true }) {
                                Icon(Icons.Rounded.LiveTv, "Channels", tint = Color.White)
                            }
                        }
                        IconButton(onClick = onEnterPip) {
                            Icon(Icons.Rounded.PictureInPicture, "PiP", tint = Color.White)
                        }
                    }
                }

                // Bottom controls (sub-composable to avoid parent recompose on every tick)
                VideoBottomControls(
                    player = player,
                    stateProvider = { state },
                    seekDragging = seekDragging,
                    seekPreviewFraction = seekPreviewMs,
                    onSeekDraggingChange = { seekDragging = it },
                    onSeekPreviewChange = { seekPreviewMs = it },
                    onSeekCommit = { frac -> viewModel.seekTo((frac * maxOf(player?.duration ?: 0L, 0L)).toLong()) },
                    onSeekBy = viewModel::seekBy,
                    onSeekFlash = { seekFlash = it },
                    onPrevious = viewModel::previous,
                    onPlayPause = viewModel::playPause,
                    onNext = viewModel::next,
                    onOpenSpeed = { uiMode = VideoUiMode.SPEED },
                    onOpenAspect = { uiMode = VideoUiMode.ASPECT },
                    onOpenSubtitles = { showSubtitles = true },
                    onToggleLock = {
                        if (!locked) locked = true
                    },
                    locked = locked,
                    resizeMode = resizeMode,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }

        // === Seek flash indicator ===
        SeekFlashLayer(
            stateProvider = { seekFlash },
            modifier = Modifier.fillMaxSize()
        )

        // === Brightness / Volume HUD (vertical) ===
        BrightnessHudLayer(
            stateProvider = { brightnessHud },
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 28.dp)
        )
        VolumeHudLayer(
            stateProvider = { volumeHud },
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 28.dp)
        )

        // === Locked overlay with auto-hide ===
        // State machine:
        //   LOCKED + HIDDEN (default after 1.5s)
        //     → tap screen → LOCKED + VISIBLE
        //     → 2s timer → LOCKED + HIDDEN
        //     → tap unlock → UNLOCKED
        LaunchedEffect(locked) {
            if (locked) {
                lockOverlayVisible = true
                lockRevealToken = 0
                kotlinx.coroutines.delay(1_500)
                if (lockRevealToken == 0) lockOverlayVisible = false
            } else {
                lockOverlayVisible = false
            }
        }
        // When lockRevealToken changes (user tapped screen), show for 2s then hide
        LaunchedEffect(lockRevealToken) {
            if (lockRevealToken > 0 && locked) {
                val myToken = lockRevealToken
                lockOverlayVisible = true
                kotlinx.coroutines.delay(2_000)
                if (lockRevealToken == myToken) lockOverlayVisible = false
            }
        }
        if (locked) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.15f))
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                if (locked) lockRevealToken++
                            }
                        )
                    }
            ) {
                AnimatedVisibility(
                    visible = lockOverlayVisible,
                    enter = fadeIn(animationSpec = tween(150)),
                    exit = fadeOut(animationSpec = tween(300)),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(Modifier.fillMaxSize().padding(16.dp)) {
                        IconButton(
                            onClick = {
                                locked = false
                                controlsVisible = true
                                lockOverlayVisible = false
                            },
                            modifier = Modifier.align(Alignment.TopEnd)
                        ) {
                            Icon(Icons.Rounded.Lock, "Unlock", tint = Color.White)
                        }
                        Text(
                            "Tap to unlock",
                            color = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 48.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        // === Dialogs ===
        when (uiMode) {
            VideoUiMode.SPEED -> SpeedDialog(playbackSpeed,
                { spd -> viewModel.setSpeedPitched(spd); uiMode = VideoUiMode.MAIN },
                { uiMode = VideoUiMode.MAIN })
            VideoUiMode.ASPECT -> AspectDialog(resizeMode,
                { rm -> resizeMode = rm; uiMode = VideoUiMode.MAIN },
                { uiMode = VideoUiMode.MAIN })
            VideoUiMode.MAIN -> Unit
        }

        // === Channel switcher (M3U stream context) ===
        if (showChannelSwitcher) {
            val filtered = remember(channelContext, channelSwitcherQuery) {
                if (channelSwitcherQuery.isBlank()) channelContext
                else channelContext.filter {
                    it.title.contains(channelSwitcherQuery, ignoreCase = true) ||
                        (it.category?.contains(channelSwitcherQuery, ignoreCase = true) == true)
                }
            }
            val currentStream = remember(channelContext, title) {
                channelContext.find { it.title == title }
            }
            ModalBottomSheet(
                onDismissRequest = { showChannelSwitcher = false; channelSwitcherQuery = "" },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp)
                        .heightIn(max = 600.dp)
                        .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                ) {
                    if (currentStream != null) {
                        FloatingGlassCard(
                            title = currentStream.title,
                            subtitle = currentStream.category,
                            trailing = {
                                IconButton(onClick = { streamsVm.toggleFavorite(currentStream.id) }) {
                                    Icon(
                                        if (currentStream.isFavorite) Icons.Rounded.Favorite
                                        else Icons.Rounded.FavoriteBorder,
                                        null,
                                        tint = if (currentStream.isFavorite) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                    Text("Switch Channel",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp))
                    OutlinedTextField(
                        value = channelSwitcherQuery,
                        onValueChange = { channelSwitcherQuery = it },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        placeholder = { Text("Search channels...") },
                        leadingIcon = { Icon(Icons.Rounded.Search, null) },
                        singleLine = true,
                        shape = RoundedCornerShape(28.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        )
                    )
                    Spacer(Modifier.height(8.dp))
                    val grouped = remember(filtered) { filtered.groupBy { it.category ?: "Uncategorized" } }
                    LazyColumn(Modifier.fillMaxWidth().weight(1f)) {
                        grouped.forEach { (cat, streams) ->
                            item(key = "h_$cat") {
                                Text(cat,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp))
                            }
                            items(streams, key = { "ch_${it.id}_${it.url}" }) { stream ->
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.switchToStream(stream)
                                            showChannelSwitcher = false
                                            channelSwitcherQuery = ""
                                        }
                                        .padding(horizontal = 24.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(Modifier.weight(1f)) {
                                        Text(stream.title,
                                            style = MaterialTheme.typography.bodyLarge,
                                            maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        if (stream.category != null) {
                                            Text(stream.category,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                    IconButton(onClick = { streamsVm.toggleFavorite(stream.id) }) {
                                        Icon(
                                            if (stream.isFavorite) Icons.Rounded.Favorite
                                            else Icons.Rounded.FavoriteBorder,
                                            null,
                                            tint = if (stream.isFavorite) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // === Subtitle panel (3 sections: embedded, local files, OpenSubtitles download) ===
        if (showSubtitles) {
            val currentUri = player?.currentMediaItem?.localConfiguration?.uri
            val activeUri = resolveActiveExternalSubtitleUri(player)
                ?: currentSubtitleFilePath(context)
            val initialQuery = remember(currentUri) {
                if (currentUri != null) {
                    val name = com.lumen.player.ui.subtitles.SubtitleHash.getFileName(context, currentUri)
                        ?: currentUri.lastPathSegment?.substringAfterLast('/')
                        ?: ""
                    name.substringBeforeLast('.').trim()
                } else ""
            }
            SubtitlePanelSheet(
                tracks = tracks,
                videoUri = currentUri,
                activeSubtitleUri = activeUri,
                subtitleDelayUs = subtitleDelayUs,
                initialSearchQuery = initialQuery,
                onDismiss = { showSubtitles = false },
                onPickEmbedded = { gid, idx ->
                    viewModel.selectTrack(gid, idx)
                    showSubtitles = false
                },
                onDisableEmbedded = {
                    player?.trackSelectionParameters?.let { p ->
                        player.trackSelectionParameters = p.buildUpon()
                            .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
                            .build()
                    }
                    showSubtitles = false
                },
                onPickLocal = { sub ->
                    viewModel.addSubtitle(sub.uri, sub.mimeType)
                    showSubtitles = false
                },
                onPickDownloaded = { path, mime ->
                    viewModel.addSubtitle(Uri.fromFile(java.io.File(path)), mime)
                    showSubtitles = false
                },
                onSubtitleDelayChange = { delay ->
                    subtitleDelayUs = delay
                    viewModel.setSubtitleOffset(delay)
                }
            )
        }
    }
}

/**
 * Returns the file:// path of the currently active external subtitle, if any.
 * Used to mark the matching row in the local-files section.
 */
private fun currentSubtitleFilePath(context: android.content.Context): String? {
    return null
}

/**
 * Resolves the URI of the currently selected external subtitle track from the player's tracks.
 */
private fun resolveActiveExternalSubtitleUri(player: androidx.media3.common.Player?): String? {
    val mediaItem = player?.currentMediaItem ?: return null
    val subs = mediaItem.localConfiguration?.subtitleConfigurations ?: return null
    if (subs.isNotEmpty()) {
        return subs.first().uri.toString()
    }
    return null
}

private enum class VideoUiMode { MAIN, SPEED, ASPECT }
private enum class SeekFlash { Back, Forward }
private data class HudInfo(val icon: ImageVector, val label: String, val value: Float)

/**
 * Custom seek bar — completely hand-rolled so it stays at 60fps during drag.
 *
 *  - 4dp track height
 *  - 12dp thumb (16dp while actively dragging)
 *  - buffered position drawn in a lighter white behind the played position
 *  - time preview "puck" floats above the thumb in real time
 *  - actual `player.seekTo` only fires on finger release
 *  - no `animateFloatAsState`, no `Slider` debounce — every pointer event
 *    updates state immediately
 */
@Composable
private fun VideoSeekBar(
    positionProvider: () -> Long,
    bufferedProvider: () -> Long,
    duration: Long,
    seekDragging: Boolean,
    seekFraction: Float?,
    onDraggingChange: (Boolean) -> Unit,
    onPreviewChange: (Float?) -> Unit,
    onSeekCommit: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val playedFraction: Float = (seekFraction ?: run {
        if (duration > 0L) positionProvider().toFloat() / duration else 0f
    }).coerceIn(0f, 1f)
    val bufferedFraction: Float = if (duration > 0L)
        bufferedProvider().toFloat() / duration.toFloat() else 0f
    val isActive = seekFraction != null

    val thumbSize = if (isActive) 16.dp else 12.dp
    val trackHeight = 4.dp
    val density = LocalDensity.current

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .pointerInput(duration) {
                val width = size.width.toFloat()
                val fracFromX: (Float) -> Float = { x ->
                    (x / width).coerceIn(0f, 1f)
                }
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    onDraggingChange(true)
                    onPreviewChange(fracFromX(down.position.x))
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Main)
                        val change = event.changes.firstOrNull() ?: break
                        if (!change.pressed) {
                            val frac = fracFromX(change.position.x)
                            onPreviewChange(frac)
                            onSeekCommit(frac)
                            onDraggingChange(false)
                            onPreviewChange(null)
                            break
                        }
                        onPreviewChange(fracFromX(change.position.x))
                    }
                }
            }
    ) {
        val parentWidthPx = constraints.maxWidth.toFloat()
        val trackWidthPx = parentWidthPx - with(density) { thumbSize.toPx() }
        val thumbXpx = trackWidthPx * playedFraction
        val thumbXDp = with(density) { thumbXpx.toDp() }
        val previewXDp = with(density) { thumbXpx.toDp() }
        val trackHeightPx = with(density) { trackHeight.toPx() }

        // Time-preview "puck" above the thumb (only visible while dragging)
        if (isActive && duration > 0L) {
            val previewMs = (playedFraction * duration).toLong()
            Box(
                modifier = Modifier
                    .padding(start = previewXDp)
                    .offset(y = (-(22.dp)))
                    .size(width = 56.dp, height = 22.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(Color.Black.copy(alpha = 0.75f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    TimeFormatter.msToClock(previewMs),
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Track layers: base, buffered (lighter), played (white)
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .height(trackHeight)
                .clip(RoundedCornerShape(trackHeight / 2))
                .background(Color.White.copy(alpha = 0.22f))
        ) {
            Box(
                Modifier
                    .fillMaxWidth(bufferedFraction.coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .background(Color.White.copy(alpha = 0.38f))
            )
            Box(
                Modifier
                    .fillMaxWidth(playedFraction.coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .background(Color.White)
            )
        }

        // Thumb (drawn on top of the track, offset by half its size for centering)
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = thumbXDp)
                .size(thumbSize)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}

/**
 * Bottom controls sub-composable. Reads state via lambda to prevent parent
 * recomposition on every 200ms position tick.
 */
@Composable
private fun VideoBottomControls(
    player: Player?,
    stateProvider: () -> com.lumen.player.player.PlaybackState,
    seekDragging: Boolean,
    seekPreviewFraction: Float?,
    onSeekDraggingChange: (Boolean) -> Unit,
    onSeekPreviewChange: (Float?) -> Unit,
    onSeekCommit: (Float) -> Unit,
    onSeekBy: (Long) -> Unit,
    onSeekFlash: (SeekFlash) -> Unit,
    onPrevious: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onOpenSpeed: () -> Unit,
    onOpenAspect: () -> Unit,
    onOpenSubtitles: () -> Unit,
    onToggleLock: () -> Unit,
    locked: Boolean,
    resizeMode: Int,
    modifier: Modifier = Modifier
) {
    val s = stateProvider()
    val duration = s.durationMs.coerceAtLeast(0L)

    Box(
        modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    0f to Color.Transparent,
                    1f to Color.Black.copy(alpha = 0.88f)
                )
            )
            .padding(top = 12.dp, bottom = 24.dp, start = 12.dp, end = 12.dp)
    ) {
        Column(Modifier.fillMaxWidth()) {
            // === Seek bar at the very top of the bottom controls (above timestamps) ===
            VideoSeekBar(
                positionProvider = { s.positionMs },
                bufferedProvider = { s.bufferedMs },
                duration = duration,
                seekDragging = seekDragging,
                seekFraction = seekPreviewFraction,
                onDraggingChange = onSeekDraggingChange,
                onPreviewChange = onSeekPreviewChange,
                onSeekCommit = onSeekCommit,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp)
            )

            // Timestamps — left = current, right = total
            Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 2.dp)) {
                val displayMs = if (seekPreviewFraction != null) {
                    (seekPreviewFraction * duration).toLong()
                } else s.positionMs
                Text(
                    TimeFormatter.msToClock(displayMs),
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall
                )
                Spacer(Modifier.weight(1f))
                Text(
                    TimeFormatter.msToClock(duration),
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall
                )
            }

            Spacer(Modifier.height(6.dp))

            // Transport row — ±10s is gesture-only (double-tap on left/right third).
            // The numeric overlay buttons were removed; the gesture handler in
            // VideoPlayerScreen still emits SeekFlash so the on-screen flash
            // and the −10s / +10s label continue to appear.
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onPrevious,
                    enabled = s.hasPrevious,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.Rounded.SkipPrevious, "Previous",
                        tint = if (s.hasPrevious) Color.White else Color.White.copy(alpha = 0.3f),
                        modifier = Modifier.size(30.dp)
                    )
                }
                FilledIconButton(
                    onClick = onPlayPause,
                    modifier = Modifier.size(64.dp),
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = Color.White, contentColor = Color.Black
                    )
                ) {
                    Crossfade(s.isPlaying, animationSpec = tween(150), label = "pp") { playing ->
                        Icon(
                            if (playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            "Play/Pause", modifier = Modifier.size(36.dp)
                        )
                    }
                }
                IconButton(
                    onClick = onNext,
                    enabled = s.hasNext,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.Rounded.SkipNext, "Next",
                        tint = if (s.hasNext) Color.White else Color.White.copy(alpha = 0.3f),
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            // Secondary row: speed, aspect, subtitles, lock
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onOpenSpeed) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Rounded.Speed, "Speed", tint = Color.White,
                            modifier = Modifier.size(22.dp))
                        Text(formatSpeed(s.playbackSpeed), color = Color.White,
                            style = MaterialTheme.typography.labelSmall)
                    }
                }
                IconButton(onClick = onOpenAspect) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Rounded.AspectRatio, "Aspect", tint = Color.White,
                            modifier = Modifier.size(22.dp))
                        Text(aspectLabel(resizeMode), color = Color.White,
                            style = MaterialTheme.typography.labelSmall)
                    }
                }
                IconButton(onClick = onOpenSubtitles) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Rounded.ClosedCaption, "Subtitles", tint = Color.White,
                            modifier = Modifier.size(22.dp))
                        Text("Subs", color = Color.White,
                            style = MaterialTheme.typography.labelSmall)
                    }
                }
                IconButton(onClick = onToggleLock) {
                    Icon(
                        if (locked) Icons.Rounded.LockOpen else Icons.Rounded.Lock,
                        "Lock", tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun VerticalHud(hud: HudInfo, modifier: Modifier = Modifier) {
    Surface(
        color = Color.Black.copy(alpha = 0.55f),
        shape = RoundedCornerShape(20.dp),
        modifier = modifier
    ) {
        Column(
            Modifier.padding(horizontal = 10.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(hud.icon, null, tint = Color.White, modifier = Modifier.size(28.dp))
            // Vertical bar: 6dp wide, 160dp tall, filled from the bottom up
            Box(
                Modifier
                    .width(6.dp)
                    .height(160.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color.White.copy(alpha = 0.22f))
            ) {
                Box(
                    Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .fillMaxHeight(hud.value.coerceIn(0f, 1f))
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color.White)
                )
            }
            Text(
                hud.label,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

/**
 * Reads the brightness HUD state inside a sub-composable so the parent
 * [VideoPlayerScreen] does not subscribe to it. Without this, every
 * vertical-drag event during a brightness adjustment would force the
 * entire screen — including the player surface — to recompose.
 */
@Composable
private fun BrightnessHudLayer(
    stateProvider: () -> HudInfo?,
    modifier: Modifier = Modifier
) {
    val hud = stateProvider() ?: return
    VerticalHud(hud = hud, modifier = modifier)
}

/** Same idea as [BrightnessHudLayer], for the volume HUD. */
@Composable
private fun VolumeHudLayer(
    stateProvider: () -> HudInfo?,
    modifier: Modifier = Modifier
) {
    val hud = stateProvider() ?: return
    VerticalHud(hud = hud, modifier = modifier)
}

/** Isolated sub-composable for the seek flash indicator. Shows an
 *  expanding ripple + a `−10s` / `+10s` label in the half of the screen
 *  where the user double-tapped. */
@Composable
private fun SeekFlashLayer(
    stateProvider: () -> SeekFlash?,
    modifier: Modifier = Modifier
) {
    val flash = stateProvider() ?: return
    val isBack = flash == SeekFlash.Back
    val transition = androidx.compose.animation.core.rememberInfiniteTransition(label = "seek-flash")
    val ripple by transition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = tween(700, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "ripple"
    )
    Box(
        modifier.padding(24.dp),
        contentAlignment = if (isBack) Alignment.CenterStart else Alignment.CenterEnd
    ) {
        Surface(
            color = Color.Black.copy(alpha = 0.55f),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .size(width = 120.dp, height = 96.dp)
                .graphicsLayer { this.scaleX = ripple; this.scaleY = ripple }
        ) {
            Column(
                Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    if (isBack) Icons.Rounded.FastRewind else Icons.Rounded.FastForward,
                    null, tint = Color.White, modifier = Modifier.size(36.dp)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    if (isBack) "−10s" else "+10s",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

private fun formatSpeed(s: Float): String {
    val v = if (s == s.toInt().toFloat()) s.toInt().toFloat() else s
    return if (v == v.toInt().toFloat()) "${v.toInt()}x" else String.format("%.2fx", v)
}

@Composable
private fun SpeedDialog(current: Float, onPick: (Float) -> Unit, onDismiss: () -> Unit) {
    val speeds = listOf(0.25f, 0.5f, 0.75f, 1f, 1.25f, 1.5f, 1.75f, 2f, 2.5f, 3f, 4f)
    LumenPickerDialog(
        onDismiss = onDismiss,
        title = "Playback speed",
        options = speeds,
        selectedValue = current,
        optionLabel = { formatSpeed(it) },
        onPick = { onPick(it) }
    )
}

@Composable
private fun AspectDialog(current: Int, onPick: (Int) -> Unit, onDismiss: () -> Unit) {
    val options = listOf(
        AspectRatioFrameLayout.RESIZE_MODE_FIT to "Fit",
        AspectRatioFrameLayout.RESIZE_MODE_FILL to "Fill",
        AspectRatioFrameLayout.RESIZE_MODE_ZOOM to "Crop",
        AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH to "Stretch"
    )
    LumenPickerDialog(
        onDismiss = onDismiss,
        title = "Aspect ratio",
        options = options,
        selectedValue = options.first { it.first == current },
        optionLabel = { p -> p.second },
        onPick = { p -> onPick(p.first) }
    )
}

private fun aspectLabel(mode: Int): String = when (mode) {
    AspectRatioFrameLayout.RESIZE_MODE_FIT -> "Fit"
    AspectRatioFrameLayout.RESIZE_MODE_FILL -> "Fill"
    AspectRatioFrameLayout.RESIZE_MODE_ZOOM -> "Crop"
    AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH -> "Stretch"
    else -> "Fit"
}

@UnstableApi
@Composable
private fun VideoSurface(
    player: Player,
    zoomProvider: () -> Float,
    resizeMode: Int,
    onPinch: (zoom: Float) -> Unit
) {
    AndroidView(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, _, gestureZoom, _ ->
                    if (gestureZoom != 1f) onPinch(zoomProvider() * gestureZoom)
                }
            },
        factory = { ctx ->
            PlayerView(ctx).apply {
                useController = false
                this.resizeMode = resizeMode
                setShutterBackgroundColor(android.graphics.Color.BLACK)
                this.player = player
            }
        },
        update = { view ->
            view.player = player
            view.resizeMode = resizeMode
            val z = zoomProvider()
            view.scaleX = z
            view.scaleY = z
        }
    )
}

/**
 * Tracks the original system brightness on entry and restores it on exit.
 * All brightness changes are written to the current activity's window
 * `screenBrightness` attribute only — never the system setting.
 */
private class BrightnessKeeper(private val context: Context) {
    private var saved = false
    fun save() { saved = true }
    fun setWindowBrightness(value: Float) {
        val activity = (context as? Activity) ?: return
        val attrs = activity.window.attributes
        attrs.screenBrightness = value.coerceIn(0.05f, 1f)
        activity.window.attributes = attrs
    }
    fun restore() {
        if (!saved) return
        val activity = (context as? Activity) ?: return
        val attrs = activity.window.attributes
        attrs.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
        activity.window.attributes = attrs
    }
}

@UnstableApi
fun enterPip(activity: android.app.Activity, width: Int, height: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val params = PictureInPictureParams.Builder()
            .setAspectRatio(Rational(width.coerceAtLeast(1), height.coerceAtLeast(1)))
            .build()
        activity.setPictureInPictureParams(params)
        activity.enterPictureInPictureMode(params)
    }
}

/**
 * Resolves a video content/file URI to its parent folder URI for use as the
 * file picker's initial directory.
 */
private fun getParentUri(context: Context, uri: Uri): Uri? {
    return try {
        val path = if (uri.scheme == "file") {
            uri.path
        } else {
            var p: String? = null
            context.contentResolver.query(
                uri,
                arrayOf(android.provider.MediaStore.Video.VideoColumns.DATA),
                null, null, null
            )?.use { c ->
                if (c.moveToFirst()) p = c.getString(0)
            }
            p
        }
        if (path != null) {
            val parent = java.io.File(path).parentFile
            if (parent != null) Uri.fromFile(parent) else null
        } else null
    } catch (_: Exception) {
        null
    }
}
