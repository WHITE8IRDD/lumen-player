package com.lumen.player.ui.player

import android.app.Activity
import android.app.PictureInPictureParams
import android.content.res.Configuration
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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloat
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
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import com.lumen.player.ui.theme.LocalAccentColor
import com.lumen.player.ui.components.LumenSquircle
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
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Equalizer
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Audiotrack

import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Switch
import com.lumen.player.ui.components.LumenPickerDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.PopupProperties
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.media3.common.TrackSelectionOverride
import com.lumen.player.core.util.TimeFormatter
import com.lumen.player.domain.model.NetworkStream
import com.lumen.player.ui.components.FloatingGlassCard
import com.lumen.player.ui.components.LumenConfirmationDialog
import com.lumen.player.ui.library.StreamsViewModel
import com.lumen.player.ui.equalizer.EqualizerViewModel
import com.lumen.player.ui.subtitles.SubtitleMenu
import com.lumen.player.ui.subtitles.SubtitleDelayDialog
import com.lumen.player.ui.subtitles.SubtitlePanelContent
import com.lumen.player.ui.subtitles.SubtitleSearchForm
import com.lumen.player.ui.subtitles.EmbeddedTrack
import com.lumen.player.ui.subtitles.SubtitleHash
import kotlin.math.abs
import java.io.File
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.annotation.SuppressLint
import java.nio.ByteBuffer
import java.nio.charset.CodingErrorAction
import android.content.ContentValues
import android.graphics.Bitmap
import android.view.TextureView
import android.view.View
import android.media.MediaScannerConnection
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import android.provider.MediaStore
import android.view.PixelCopy
import android.view.SurfaceView
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.DpOffset

@UnstableApi
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun VideoPlayerScreen(
    onClose: () -> Unit,
    onEnterPip: () -> Unit = {},
    viewModel: PlayerViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) { com.lumen.player.core.util.PerfTimer.mark("2. VideoPlayerScreen entered") }
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
    var isUnlocking by remember { mutableStateOf(false) }
    var isLockIconVisible by remember { mutableStateOf(true) }
    var lockHintShown by remember { mutableStateOf(false) }
    var showSubMenu by remember { mutableStateOf(false) }
    var showDelayDialog by remember { mutableStateOf(false) }
    var showDownloadScreen by remember { mutableStateOf(false) }
    var showSearchForm by remember { mutableStateOf(false) }
    var subtitleDelayMs by remember { mutableStateOf(0) }
    var subtitleDelayUs by remember { mutableStateOf(0L) }
    var menuExpanded by remember { mutableStateOf(false) }
    var seekDragging by remember { mutableStateOf(false) }
    var seekPreviewMs by remember { mutableStateOf<Float?>(null) }
    var showChannelSwitcher by remember { mutableStateOf(false) }
    var channelSwitcherQuery by remember { mutableStateOf("") }
    var showAudioSheet by remember { mutableStateOf(false) }
    var showVideoInfo by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showAspectBadge by remember { mutableStateOf(false) }
    var currentAspectLabel by remember { mutableStateOf("Fit") }
    var showSkipMenu by remember { mutableStateOf(false) }
    var showSpeedMenu by remember { mutableStateOf(false) }
    var showEqPanel by remember { mutableStateOf(false) }
    var showLocalSubsSheet by remember { mutableStateOf(false) }
    var appliedSubtitleName by remember { mutableStateOf<String?>(null) }
    var downloadPanelLanguage by remember { mutableStateOf("en") }
    val playerViewRef = remember { mutableStateOf<PlayerView?>(null) }
    val screenshotScope = rememberCoroutineScope()

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

    // Portrait content detection
    val isPortraitContent by remember {
        derivedStateOf {
            val vs = player?.videoSize
            vs != null && vs.height > vs.width
        }
    }
    var showPortraitBadge by remember { mutableStateOf(false) }
    LaunchedEffect(isPortraitContent) {
        if (isPortraitContent) {
            showPortraitBadge = true
            kotlinx.coroutines.delay(4_000)
            showPortraitBadge = false
        } else {
            showPortraitBadge = false
        }
    }

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

    // ---- Lifecycle observer: persist position on background ----
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE && state.isPlaying) {
                viewModel.flushResumePosition()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
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

    // ---- Show hint toast on first lock ----
    LaunchedEffect(locked) {
        if (locked && !lockHintShown) {
            lockHintShown = true
            android.widget.Toast.makeText(context, "Long-press anywhere to unlock", android.widget.Toast.LENGTH_SHORT).show()
        }
        if (!locked) {
            lockHintShown = false
        }
    }

    // ---- Player error listener: recover from subtitle crashes ----
    DisposableEffect(player) {
        val listener = object : androidx.media3.common.Player.Listener {
            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                android.util.Log.e("SubFix", "Player error: ${error.errorCodeName}", error)
                if (error.errorCodeName.contains("SUBTITLE") || error.message?.contains("subtitle", ignoreCase = true) == true) {
                    android.util.Log.d("SubFix", "Subtitle error detected — disabling text track")
                    player?.let { p ->
                        p.trackSelectionParameters = p.trackSelectionParameters.buildUpon()
                            .setTrackTypeDisabled(androidx.media3.common.C.TRACK_TYPE_TEXT, true)
                            .build()
                    }
                    val item = player?.currentMediaItem ?: return@onPlayerError
                    val pos = player?.currentPosition ?: return@onPlayerError
                    val cleanItem = item.buildUpon().setSubtitleConfigurations(emptyList()).build()
                    player?.setMediaItem(cleanItem, pos)
                    player?.prepare()
                    player?.play()
                    android.widget.Toast.makeText(context, "Subtitle caused error, removed", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }
        player?.addListener(listener)
        onDispose { player?.removeListener(listener) }
    }

    // ---- Auto-hide lock icon after 1s when locked ----
    LaunchedEffect(locked) {
        if (!locked) {
            isLockIconVisible = true
            isUnlocking = false
        }
    }
    LaunchedEffect(locked, isLockIconVisible) {
        if (locked && isLockIconVisible) {
            delay(1_000)
            isLockIconVisible = false
        }
    }

    // ---- 3s auto-hide for controls, 1s auto-hide for HUD ----
    LaunchedEffect(controlsVisible, locked, menuExpanded, showSpeedMenu) {
        if (controlsVisible && !locked && !menuExpanded && !showSpeedMenu) {
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
    val title by remember {
        derivedStateOf {
            val raw = state.title
            if (raw.isBlank() || raw.matches(Regex("^\\d+$"))) {
                player?.currentMediaItem?.localConfiguration?.uri
                    ?.lastPathSegment
                    ?.substringAfterLast("/")
                    ?.substringBeforeLast(".")
                    ?: "Video"
            } else raw
        }
    }
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
            // exclusively available through the speed dropdown in the control
            // bar. This eliminates an entire class
            // of playback-speed corruption bugs.
            .then(
                if (!locked) {
                    Modifier.pointerInput(Unit) {
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
                                } else if (abs(dx) > 24f && abs(dx) > abs(dy)) {
                                    sawDrag = true
                                    val duration = player?.duration ?: 0L
                                    if (duration > 0L) {
                                        val seekDeltaFraction = dx / size.width.toFloat()
                                        val seekDeltaMs = (seekDeltaFraction * 90_000L).toLong()
                                        val currentPos = player?.currentPosition ?: 0L
                                        val targetMs = (currentPos + seekDeltaMs).coerceIn(0L, duration)
                                        player?.seekTo(targetMs)
                                    }
                                }
                            }
                        }
                    }
                } else Modifier
            )
    ) {
        // === Video surface ===
        if (player != null) {
            VideoSurface(
                player = player,
                zoomProvider = { zoom },
                resizeMode = resizeMode,
                onPinch = { newZoom -> zoom = newZoom.coerceIn(0.5f, 4f) },
                onPlayerViewCreated = { playerViewRef.value = it }
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
                        .padding(top = 28.dp, bottom = 12.dp, start = 8.dp, end = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(onClick = onClose) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back", tint = Color.White, modifier = Modifier.size(22.dp))
                        }
                        Spacer(Modifier.width(8.dp))
                        val filePath = player?.currentMediaItem?.localConfiguration?.uri?.path ?: ""
                        val showName = remember(filePath) { extractShowName(filePath) }
                        val fileName = remember(filePath) { File(filePath).nameWithoutExtension }
                        val (episodeLabel, _) = remember(fileName) { extractEpisodeInfo(fileName) }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                title.ifBlank { "Video" },
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (episodeLabel.isNotBlank()) {
                                Text(
                                    episodeLabel,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = Color.White.copy(alpha = 0.60f),
                                    maxLines = 1
                                )
                            }
                        }
                        if (showChannelButton) {
                            IconButton(onClick = { showChannelSwitcher = true }) {
                                Icon(Icons.Rounded.LiveTv, "Channels", tint = Color.White, modifier = Modifier.size(22.dp))
                            }
                        }
                        IconButton(onClick = { showSubMenu = true }) {
                            Icon(Icons.Rounded.ClosedCaption, "Subtitles", tint = Color.White, modifier = Modifier.size(22.dp))
                        }
                        IconButton(onClick = {
                            val pv = playerViewRef.value
                            if (pv != null) {
                                screenshotScope.launch {
                                    takeScreenshot(context, pv)
                                }
                            }
                        }) {
                            Icon(Icons.Rounded.PhotoCamera, "Screenshot", tint = Color.White, modifier = Modifier.size(22.dp))
                        }
                        Spacer(Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(
                                    if (locked) Color.Black.copy(0.5f) else Color.Transparent,
                                    CircleShape
                                )
                                .combinedClickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = {
                                        if (locked) {
                                            android.widget.Toast.makeText(context, "Long-press anywhere to unlock", android.widget.Toast.LENGTH_SHORT).show()
                                        } else {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            locked = true
                                        }
                                    },
                                    onLongClick = {
                                        if (locked) {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            locked = false
                                        }
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                if (locked) Icons.Rounded.Lock else Icons.Rounded.LockOpen,
                                "Lock",
                                tint = if (locked) LocalAccentColor.current else Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(Modifier.width(4.dp))
                        IconButton(onClick = onEnterPip) {
                            Icon(Icons.Rounded.PictureInPicture, "PiP", tint = Color.White, modifier = Modifier.size(22.dp))
                        }
                        Box {
                            IconButton(onClick = { menuExpanded = true }) {
                                Icon(Icons.Rounded.MoreVert, "More options", tint = Color.White, modifier = Modifier.size(22.dp))
                            }
                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false },
                                containerColor = Color(0xFF1C1C1C),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.widthIn(min = 240.dp),
                                properties = PopupProperties(focusable = true)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Audio tracks", color = Color.White, fontSize = 15.sp) },
                                    leadingIcon = { Icon(Icons.Rounded.Audiotrack, null, tint = Color.White.copy(alpha = 0.85f), modifier = Modifier.size(22.dp)) },
                                    onClick = { menuExpanded = false; showAudioSheet = true }
                                )
                                DropdownMenuItem(
                                    text = { Text("Subtitles", color = Color.White, fontSize = 15.sp) },
                                    leadingIcon = { Icon(Icons.Rounded.ClosedCaption, null, tint = Color.White.copy(alpha = 0.85f), modifier = Modifier.size(22.dp)) },
                                    onClick = { menuExpanded = false; showSubMenu = true }
                                )
                                DropdownMenuItem(
                                    text = { Text("Equalizer", color = Color.White, fontSize = 15.sp) },
                                    leadingIcon = { Icon(Icons.Rounded.Equalizer, null, tint = Color.White.copy(alpha = 0.85f), modifier = Modifier.size(22.dp)) },
                                    onClick = { menuExpanded = false; showEqPanel = true }
                                )
                                DropdownMenuItem(
                                    text = { Text("Video info", color = Color.White, fontSize = 15.sp) },
                                    leadingIcon = { Icon(Icons.Rounded.Info, null, tint = Color.White.copy(alpha = 0.85f), modifier = Modifier.size(22.dp)) },
                                    onClick = { menuExpanded = false; showVideoInfo = true }
                                )
                                DropdownMenuItem(
                                    text = { Text("Screenshot", color = Color.White, fontSize = 15.sp) },
                                    leadingIcon = { Icon(Icons.Rounded.CameraAlt, null, tint = Color.White.copy(alpha = 0.85f), modifier = Modifier.size(22.dp)) },
                                    onClick = {
                                        menuExpanded = false
                                        val pv = playerViewRef.value
                                        if (pv != null) {
                                            screenshotScope.launch {
                                                takeScreenshot(context, pv)
                                            }
                                        }
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Share", color = Color.White, fontSize = 15.sp) },
                                    leadingIcon = { Icon(Icons.Rounded.Share, null, tint = Color.White.copy(alpha = 0.85f), modifier = Modifier.size(22.dp)) },
                                    onClick = {
                                        menuExpanded = false
                                        val uri = player?.currentMediaItem?.localConfiguration?.uri ?: return@DropdownMenuItem
                                        try {
                                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                type = "video/*"
                                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                            }
                                            if (uri.scheme == "content") {
                                                shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
                                            } else if (uri.scheme == "file") {
                                                val f = File(uri.path!!)
                                                if (!f.exists()) {
                                                    android.widget.Toast.makeText(context, "File not found", android.widget.Toast.LENGTH_SHORT).show()
                                                    return@DropdownMenuItem
                                                }
                                                val fileUri = androidx.core.content.FileProvider.getUriForFile(
                                                    context, "${context.packageName}.fileprovider", f
                                                )
                                                shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri)
                                            } else {
                                                android.widget.Toast.makeText(context, "Cannot share this file type", android.widget.Toast.LENGTH_SHORT).show()
                                                return@DropdownMenuItem
                                            }
                                            context.startActivity(Intent.createChooser(shareIntent, "Share video").apply {
                                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            })
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                            android.widget.Toast.makeText(context, "Share failed: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                                        }
                                    }
                                )
                                HorizontalDivider(color = Color.White.copy(alpha = 0.15f), thickness = 0.5.dp)
                                DropdownMenuItem(
                                    text = { Text("Delete file", color = Color(0xFFFF4444), fontSize = 15.sp) },
                                    leadingIcon = { Icon(Icons.Rounded.Delete, null, tint = Color(0xFFFF4444), modifier = Modifier.size(22.dp)) },
                                    onClick = { menuExpanded = false; showDeleteConfirm = true }
                                )
                            }
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
                    onOpenSpeed = { showSpeedMenu = true },
                    onOpenAspect = {
                        val modes = listOf(
                            AspectRatioFrameLayout.RESIZE_MODE_FIT,
                            AspectRatioFrameLayout.RESIZE_MODE_ZOOM,
                            AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH,
                            AspectRatioFrameLayout.RESIZE_MODE_FILL
                        )
                        val idx = modes.indexOf(resizeMode)
                        resizeMode = if (idx >= 0) modes[(idx + 1) % modes.size] else modes[0]
                        currentAspectLabel = aspectLabel(resizeMode)
                        showAspectBadge = true
                    },
                    onOpenSubtitles = { showSubMenu = true },
                    onToggleLock = {
                        if (!locked) locked = true
                    },
                    onOpenEqPanel = { showEqPanel = true },
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
                .padding(start = 12.dp)
        )
        VolumeHudLayer(
            stateProvider = { volumeHud },
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp)
        )

        // === Portrait content badge ===
        if (showPortraitBadge && isPortraitContent) {
            AnimatedVisibility(
                visible = showPortraitBadge,
                enter = fadeIn(tween(300)),
                exit = fadeOut(tween(300)),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 16.dp, bottom = 130.dp)
            ) {
                Text(
                    "Original format",
                    color = Color.White.copy(alpha = 0.60f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.50f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        // === Locked overlay ===
        if (locked) {
            val lockScope = rememberCoroutineScope()
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(isLockIconVisible, isUnlocking) {
                        detectTapGestures(
                            onTap = {
                                if (!isLockIconVisible) {
                                    isLockIconVisible = true
                                }
                            },
                            onLongPress = {
                                if (!isUnlocking) {
                                    isUnlocking = true
                                    lockScope.launch {
                                        delay(400)
                                        locked = false
                                        controlsVisible = true
                                        isUnlocking = false
                                    }
                                }
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                AnimatedVisibility(
                    visible = isLockIconVisible && !isUnlocking,
                    enter = fadeIn(animationSpec = tween(250)),
                    exit = fadeOut(animationSpec = tween(400))
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .background(
                                color = Color.Black.copy(alpha = 0.5f),
                                shape = CircleShape
                            )
                            .padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Lock,
                            contentDescription = "Locked",
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Long press to unlock",
                            color = Color.White,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                AnimatedVisibility(
                    visible = isUnlocking,
                    enter = fadeIn(animationSpec = tween(200)),
                    exit = fadeOut(animationSpec = tween(200))
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .background(
                                color = Color.Black.copy(alpha = 0.5f),
                                shape = CircleShape
                            )
                            .padding(24.dp)
                    ) {
                        Crossfade(
                            targetState = isUnlocking,
                            animationSpec = tween(300),
                            label = "lock_animation"
                        ) { unlocking ->
                            Icon(
                                imageVector = if (unlocking) Icons.Rounded.LockOpen else Icons.Rounded.Lock,
                                contentDescription = if (unlocking) "Unlocking" else "Locked",
                                tint = Color.White,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Unlocking...",
                            color = Color.White,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        // === Aspect badge ===
        if (showAspectBadge) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(currentAspectLabel, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.shadow(8.dp, RoundedCornerShape(12.dp))
                        .background(Color(0xCC000000), RoundedCornerShape(12.dp)).padding(horizontal = 30.dp, vertical = 16.dp))
            }
        }
        LaunchedEffect(showAspectBadge) {
            if (showAspectBadge) { delay(900); showAspectBadge = false }
        }

        // === Long-press skip menu (+85s) ===
        DropdownMenu(
            expanded = showSkipMenu,
            onDismissRequest = { showSkipMenu = false },
            modifier = Modifier.background(Color(0xFF2A2A2A), RoundedCornerShape(12.dp))
        ) {
            DropdownMenuItem(text = { Text("Skip +85s", color = Color.White) }, onClick = {
                showSkipMenu = false
                player?.seekTo((player?.currentPosition ?: 0L) + 85_000)
            })
            DropdownMenuItem(text = { Text("Skip +300s", color = Color.White) }, onClick = {
                showSkipMenu = false
                player?.seekTo((player?.currentPosition ?: 0L) + 300_000)
            })
        }
        LaunchedEffect(showSkipMenu) {
            if (showSkipMenu) { delay(6000); showSkipMenu = false }
        }

        // === Dialogs ===
        when (uiMode) {
            VideoUiMode.SPEED -> Unit
            VideoUiMode.ASPECT -> AspectDialog(resizeMode,
                { rm -> resizeMode = rm; uiMode = VideoUiMode.MAIN },
                { uiMode = VideoUiMode.MAIN })
            VideoUiMode.MAIN -> Unit
        }

        // === Speed dropdown menu ===
        if (showSpeedMenu) {
            DropdownMenu(
                expanded = showSpeedMenu,
                onDismissRequest = { showSpeedMenu = false },
                offset = DpOffset(x = 16.dp, y = (-8).dp),
                modifier = Modifier
                    .background(Color(0xFF1C1C1C), RoundedCornerShape(12.dp))
                    .width(140.dp),
                properties = PopupProperties(focusable = true)
            ) {
                listOf(0.5f, 0.75f, 1f, 1.25f, 1.5f, 1.75f, 2f).forEach { speed ->
                    val isSelected = speed == state.playbackSpeed
                    DropdownMenuItem(
                        text = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "${speed}x",
                                    color = if (isSelected) LocalAccentColor.current else Color.White,                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                                if (isSelected) {
                                    Icon(
                                        Icons.Rounded.Check,
                                        contentDescription = null,
                                        tint = LocalAccentColor.current,                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        },
                        onClick = {
                            viewModel.setSpeedPitched(speed)
                            showSpeedMenu = false
                        }
                    )
                }
            }
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
                        shape = LumenSquircle(28.dp),
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

        // === VLC-style subtitle system ===
        val scope = rememberCoroutineScope()
        val currentUri = player?.currentMediaItem?.localConfiguration?.uri
        val subtitleViewModel: com.lumen.player.ui.subtitles.SubtitleViewModel = hiltViewModel()
        val downloadState by subtitleViewModel.downloadState.collectAsStateWithLifecycle()

        val embeddedTracks = remember(tracks) {
            buildEmbeddedTrackList(tracks)
        }

        val activeTrackId = remember(tracks) {
            tracks?.groups?.firstOrNull { g -> g.type == C.TRACK_TYPE_TEXT }
                ?.let { group ->
                    for (i in 0 until group.length) {
                        if (group.isTrackSelected(i)) return@let "${group.mediaTrackGroup.id}.$i"
                    }
                    null
                }
        }

        val currentFilePath = remember(currentUri) {
            currentUri?.let { uri ->
                if (uri.scheme == "file") uri.path
                else try {
                    var p: String? = null
                    context.contentResolver.query(uri, arrayOf(android.provider.MediaStore.Video.VideoColumns.DATA), null, null, null)?.use {
                        if (it.moveToFirst()) p = it.getString(0)
                    }
                    p
                } catch (_: Exception) { null }
            }
        }

        val initialQuery = remember(currentUri, currentFilePath) {
            val raw = if (currentUri != null) {
                SubtitleHash.getFileName(context, currentUri)
                    ?: currentUri.lastPathSegment?.substringAfterLast('/')
                    ?: ""
            } else ""
            com.lumen.player.ui.subtitles.cleanTitleForSearch(raw)
        }

        val localSubs = remember(currentFilePath) {
            com.lumen.player.ui.subtitles.detectLocalSubtitles(currentFilePath)
        }

        fun detectLanguageFromFilename(name: String): String {
            val lower = name.lowercase()
            return when {
                lower.contains(".eng.") || lower.contains("english") || lower.contains("_en") || lower.startsWith("eng") -> "en"
                lower.contains(".ara.") || lower.contains("arabic") || lower.contains("_ar") -> "ar"
                lower.contains(".spa.") || lower.contains("spanish") || lower.contains(".lat.") || lower.contains("_es") -> "es"
                lower.contains(".fre.") || lower.contains(".fra.") || lower.contains("french") || lower.contains("_fr") -> "fr"
                lower.contains(".ger.") || lower.contains(".deu.") || lower.contains("german") || lower.contains("_de") -> "de"
                lower.contains(".ita.") || lower.contains("italian") || lower.contains("_it") -> "it"
                lower.contains(".kor.") || lower.contains("korean") || lower.contains("_ko") -> "ko"
                lower.contains(".chi.") || lower.contains(".zho.") || lower.contains("chinese") -> "zh"
                lower.contains(".jpn.") || lower.contains("japanese") -> "ja"
                else -> "en"
            }
        }

        fun scoreContent(content: String, expectArabic: Boolean, expectLatin: Boolean): Int {
            if (content.isBlank()) return 0
            var score = 0; var arabicCount = 0; var latinCount = 0; var asciiCount = 0; var replacementCount = 0; var weirdCount = 0
            for (c in content) {
                when {
                    c == '\uFFFD' -> replacementCount++
                    c.code in 0x0600..0x06FF -> arabicCount++
                    c.code in 0x00C0..0x017F -> latinCount++
                    c.code in 0x0020..0x007E -> asciiCount++
                    c.code in 0x0080..0x00BF -> weirdCount++
                    c == '\n' || c == '\r' || c == '\t' -> asciiCount++
                    c.code < 0x20 -> weirdCount++
                }
            }
            score += asciiCount
            if (expectArabic && arabicCount > 0) score += arabicCount * 3
            if (expectLatin && latinCount > 0) score += latinCount * 3
            score -= replacementCount * 10
            score -= weirdCount * 2
            if (expectLatin && arabicCount > asciiCount / 10) score -= arabicCount * 5
            if (expectArabic && latinCount > arabicCount / 2 && arabicCount > 0) score -= latinCount * 2
            return score.coerceAtLeast(0)
        }

        fun tryDecodeStrict(bytes: ByteArray, charset: java.nio.charset.Charset): String? {
            return try {
                val result = charset.newDecoder().onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(bytes)).toString()
                if (result.contains('\uFFFD')) null else result
            } catch (e: Exception) { null }
        }

        fun convertToUtf8IfNeeded(file: java.io.File): java.io.File {
            return try {
                val bytes = file.readBytes()
                val filename = file.name.lowercase()
                android.util.Log.d("SubFix", "═══ Encoding detection ═══")
                android.util.Log.d("SubFix", "File: ${file.name}, size: ${bytes.size}")
                val utf8Result = tryDecodeStrict(bytes, Charsets.UTF_8)
                if (utf8Result != null) {
                    android.util.Log.d("SubFix", "Valid UTF-8 — using original file")
                    return file
                }
                android.util.Log.d("SubFix", "Not valid UTF-8, scoring alternatives...")
                val isArabicHint = filename.contains(".ara") || filename.contains("arabic") || filename.contains(".ar.") || filename.startsWith("ar")
                val isFrenchHint = filename.contains(".fre") || filename.contains(".fra") || filename.contains("french") || filename.contains(".fr.") || filename.startsWith("fr")
                val isSpanishHint = filename.contains(".spa") || filename.contains("spanish") || filename.contains(".es.") || filename.contains(".lat") || filename.startsWith("es")
                val isGermanHint = filename.contains(".ger") || filename.contains(".deu") || filename.contains("german") || filename.contains(".de.")
                val isLatinHint = isFrenchHint || isSpanishHint || isGermanHint
                android.util.Log.d("SubFix", "Hints: AR=$isArabicHint FR=$isFrenchHint ES=$isSpanishHint DE=$isGermanHint")
                val encodings = when {
                    isArabicHint -> listOf("Windows-1256", "ISO-8859-6", "UTF-8", "Windows-1252")
                    isLatinHint -> listOf("Windows-1252", "ISO-8859-1", "ISO-8859-15", "UTF-8")
                    else -> listOf("Windows-1252", "Windows-1256", "ISO-8859-1", "ISO-8859-6")
                }
                data class Score(val encoding: String, val content: String, val score: Int)
                val candidates = mutableListOf<Score>()
                for (encoding in encodings) {
                    try {
                        val charset = java.nio.charset.Charset.forName(encoding)
                        val decoder = charset.newDecoder().onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT)
                        val content = try { decoder.decode(ByteBuffer.wrap(bytes)).toString() } catch (e: Exception) { String(bytes, charset) }
                        val score = scoreContent(content, isArabicHint, isLatinHint)
                        android.util.Log.d("SubFix", "$encoding score: $score")
                        candidates.add(Score(encoding, content, score))
                    } catch (e: Exception) { android.util.Log.w("SubFix", "$encoding decode failed") }
                }
                val best = candidates.maxByOrNull { it.score }
                if (best == null || best.score < 10) {
                    android.util.Log.w("SubFix", "No good encoding found, using original")
                    return file
                }
                android.util.Log.d("SubFix", "WINNER: ${best.encoding} (score=${best.score})")
                val outFile = java.io.File(file.parent, "utf8_${file.name}")
                outFile.writeText(best.content, Charsets.UTF_8)
                android.util.Log.d("SubFix", "Saved UTF-8 to: ${outFile.absolutePath}")
                outFile
            } catch (e: Exception) { android.util.Log.e("SubFix", "Encoding detection exception", e); file }
        }

        fun isValidSubtitleFile(file: java.io.File): Boolean {
            return try {
                if (!file.exists()) return false
                if (file.length() == 0L) return false
                if (file.length() > 2_000_000L) { android.util.Log.w("SubFix", "File too large: ${file.length()}"); return false }
                val sample = file.readBytes().take(500).toByteArray()
                val text = String(sample, Charsets.UTF_8)
                when (file.extension.lowercase()) {
                    "srt" -> {
                        val hasNumber = text.lines().any { it.trim().matches(Regex("^\\d+$")) }
                        val hasTimestamp = text.contains(" --> ")
                        if (!hasNumber || !hasTimestamp) { android.util.Log.w("SubFix", "Invalid SRT format"); return false }
                    }
                    "vtt" -> { if (!text.startsWith("WEBVTT")) { android.util.Log.w("SubFix", "Invalid VTT format"); return false } }
                }
                true
            } catch (e: Exception) { android.util.Log.e("SubFix", "Validation failed", e); false }
        }

        fun showErrorToast(msg: String) {
            android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
        }

        fun applySubtitleFile(file: java.io.File) {
            android.util.Log.d("SubFix", "═══ applySubtitleFile START ═══")

            if (!isValidSubtitleFile(file)) {
                android.util.Log.e("SubFix", "FAIL: invalid subtitle file")
                showErrorToast("Invalid subtitle file")
                return
            }

            try {
                val safeFile = convertToUtf8IfNeeded(file)
                android.util.Log.d("SubFix", "Using file: ${safeFile.absolutePath}")

                val p = player
                android.util.Log.d("SubFix", "4. player: $p")
                if (p == null) { android.util.Log.e("SubFix", "FAIL at step 4: player null"); return }

                if (!safeFile.exists()) { android.util.Log.e("SubFix", "FAIL at step 5: file not found"); return }
                if (safeFile.length() == 0L) { android.util.Log.e("SubFix", "FAIL at step 5: file empty"); return }
                android.util.Log.d("SubFix", "5. file exists and non-empty")

                val mimeType = when (safeFile.extension.lowercase()) {
                    "srt" -> androidx.media3.common.MimeTypes.APPLICATION_SUBRIP
                    "vtt" -> androidx.media3.common.MimeTypes.TEXT_VTT
                    "ass", "ssa" -> androidx.media3.common.MimeTypes.TEXT_SSA
                    else -> androidx.media3.common.MimeTypes.APPLICATION_SUBRIP
                }
                val language = detectLanguageFromFilename(safeFile.name)
                val fileUri = android.net.Uri.fromFile(safeFile)
                android.util.Log.d("SubFix", "6. fileUri: $fileUri MimeType: $mimeType Language: $language")

                val subtitleConfig = androidx.media3.common.MediaItem.SubtitleConfiguration.Builder(fileUri)
                    .setMimeType(mimeType)
                    .setLanguage(language)
                    .setSelectionFlags(androidx.media3.common.C.SELECTION_FLAG_DEFAULT)
                    .setRoleFlags(androidx.media3.common.C.ROLE_FLAG_SUBTITLE)
                    .build()
                android.util.Log.d("SubFix", "7. subtitleConfig built")

                val currentItem = p.currentMediaItem
                android.util.Log.d("SubFix", "8. currentItem: $currentItem")
                if (currentItem == null) { android.util.Log.e("SubFix", "FAIL at step 8: no media item"); return }

                val currentPosition = p.currentPosition
                val wasPlaying = p.isPlaying
                android.util.Log.d("SubFix", "9. pos=$currentPosition playing=$wasPlaying")

                p.trackSelectionParameters = p.trackSelectionParameters.buildUpon()
                    .setPreferredTextLanguage(language)
                    .setSelectUndeterminedTextLanguage(true)
                    .setTrackTypeDisabled(androidx.media3.common.C.TRACK_TYPE_TEXT, false)
                    .build()
                android.util.Log.d("SubFix", "10. track params set before prepare")

                val newItem = currentItem.buildUpon().setSubtitleConfigurations(listOf(subtitleConfig)).build()
                val subCount = newItem.localConfiguration?.subtitleConfigurations?.size
                android.util.Log.d("SubFix", "11. newItem built, subs: $subCount")

                p.setMediaItem(newItem, currentPosition)
                android.util.Log.d("SubFix", "12. setMediaItem done")

                p.prepare()
                android.util.Log.d("SubFix", "13. prepare done")

                if (wasPlaying) p.play()
                appliedSubtitleName = safeFile.name
                android.util.Log.d("SubFix", "14. apply complete, applied: $appliedSubtitleName")

            } catch (e: Exception) {
                android.util.Log.e("SubFix", "Apply crashed", e)
                showErrorToast("Subtitle file is corrupted")
                player?.let { p ->
                    p.trackSelectionParameters = p.trackSelectionParameters.buildUpon()
                        .setTrackTypeDisabled(androidx.media3.common.C.TRACK_TYPE_TEXT, true)
                        .build()
                }
            }
        }

        LaunchedEffect(Unit) {
            subtitleViewModel.onApplySubtitle = { file ->
                android.util.Log.d("SubFix", "Callback fired with: ${file.name}")
                applySubtitleFile(file)
            }
        }

        DisposableEffect(Unit) {
            onDispose { subtitleViewModel.onApplySubtitle = null }
        }

        val subtitleFilePicker = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocument(),
            onResult = { uri ->
                if (uri == null) return@rememberLauncherForActivityResult
                scope.launch(Dispatchers.IO) {
                    try {
                        val fileName = SubtitleHash.getFileName(context, uri)
                            ?: "subtitle_${System.currentTimeMillis()}.srt"
                        val destDir = java.io.File(context.getExternalFilesDir(null), "subtitles")
                        destDir.mkdirs()
                        val destFile = java.io.File(destDir, fileName)
                        context.contentResolver.openInputStream(uri)?.use { input ->
                            destFile.outputStream().use { output -> input.copyTo(output) }
                        }
                        withContext(Dispatchers.Main) {
                            val mime = detectMime(destFile.name)
                            viewModel.addSubtitle(android.net.Uri.fromFile(destFile), mime)
                            android.widget.Toast.makeText(context, "Subtitle applied", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            android.widget.Toast.makeText(context, "Failed: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        )

        LaunchedEffect(showDownloadScreen) {
            if (showDownloadScreen && initialQuery.isNotBlank()) {
                val file = currentFilePath?.let { java.io.File(it) }
                subtitleViewModel.searchByFile(file, initialQuery, "en")
            }
        }

        fun applyLocalSubtitleFile(file: java.io.File) {
            android.util.Log.d("SubFix", "=== applyLocalSubtitle called ===")
            android.util.Log.d("SubFix", "1. File: ${file.absolutePath}")
            android.util.Log.d("SubFix", "2. File exists: ${file.exists()}")
            android.util.Log.d("SubFix", "3. File size: ${file.length()}")
            android.util.Log.d("SubFix", "4. Player state: ${player?.playbackState}")
            applySubtitleFile(file)
        }

        if (showSubMenu) {
            SubtitleMenu(
                visible = showSubMenu,
                embeddedTracks = embeddedTracks,
                activeTrackId = activeTrackId,
                onDismiss = { showSubMenu = false },
                onDisable = {
                    player?.trackSelectionParameters?.let { p ->
                        player.trackSelectionParameters = p.buildUpon()
                            .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
                            .build()
                    }
                },
                onSelectTrack = { track ->
                    viewModel.selectTrack(track.groupId, track.trackIndex)
                },
                onShowDelayDialog = { showDelayDialog = true },
                onPickFile = {
                    subtitleFilePicker.launch(arrayOf("*/*"))
                },
                onShowLocalSubs = { showLocalSubsSheet = true },
                onShowDownload = { showDownloadScreen = true }
            )
        }

        SubtitleDelayDialog(
            visible = showDelayDialog,
            delayMs = subtitleDelayMs,
            onDelayChange = { subtitleDelayMs = it; subtitleDelayUs = it * 1000L; viewModel.setSubtitleOffset(subtitleDelayUs) },
            onReset = { subtitleDelayMs = 0; subtitleDelayUs = 0L; viewModel.setSubtitleOffset(0L) },
            onDismiss = { showDelayDialog = false }
        )

        // Orientation-aware subtitle panel
        val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
        if (showDownloadScreen) {
            if (isLandscape) {
                Box(Modifier.fillMaxWidth(0.40f).fillMaxHeight().align(Alignment.CenterEnd).background(Color(0xFF0E0E12))) {
                    SubtitlePanelContent(
                        localSubs = localSubs,
                        onlineSubs = downloadState.results,
                        appliedSubtitleName = appliedSubtitleName,
                        currentLanguage = downloadPanelLanguage,
                        onLanguageChange = { lang ->
                            downloadPanelLanguage = lang
                            subtitleViewModel.searchByFile(currentFilePath?.let { java.io.File(it) }, initialQuery, lang)
                        },
                        isLoading = downloadState.isLoading,
                        onLocalApply = { applyLocalSubtitleFile(it) },
                        onOnlineDownload = { result ->
                            android.util.Log.d("SubFix", "═══ Online result tapped ═══")
                            android.util.Log.d("SubFix", "URL: ${result.downloadUrl}")
                            android.util.Log.d("SubFix", "Language: ${result.language} Name: ${result.releaseName}")
                            subtitleViewModel.downloadAndApply(context, result)
                        },
                        onSearchTap = { showSearchForm = true },
                        onDismiss = { showDownloadScreen = false; subtitleViewModel.resetDownload() }
                    )
                }
            } else {
                ModalBottomSheet(
                    onDismissRequest = { showDownloadScreen = false; subtitleViewModel.resetDownload() },
                    containerColor = Color(0xFF0E0E12),
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                    modifier = Modifier.fillMaxHeight(0.6f)
                ) {
                    SubtitlePanelContent(
                        localSubs = localSubs,
                        onlineSubs = downloadState.results,
                        appliedSubtitleName = appliedSubtitleName,
                        currentLanguage = downloadPanelLanguage,
                        onLanguageChange = { lang ->
                            downloadPanelLanguage = lang
                            subtitleViewModel.searchByFile(currentFilePath?.let { java.io.File(it) }, initialQuery, lang)
                        },
                        isLoading = downloadState.isLoading,
                        onLocalApply = { applyLocalSubtitleFile(it) },
                        onOnlineDownload = { result ->
                            android.util.Log.d("SubFix", "═══ Online result tapped ═══")
                            android.util.Log.d("SubFix", "URL: ${result.downloadUrl}")
                            android.util.Log.d("SubFix", "Language: ${result.language} Name: ${result.releaseName}")
                            subtitleViewModel.downloadAndApply(context, result)
                        },
                        onSearchTap = { showSearchForm = true },
                        onDismiss = { showDownloadScreen = false; subtitleViewModel.resetDownload() }
                    )
                }
            }
        }

        SubtitleSearchForm(
            visible = showSearchForm,
            initialName = initialQuery,
            initialSeason = "",
            initialEpisode = "",
            initialLanguage = "en",
            onDismiss = { showSearchForm = false },
            onSearch = { name, season, episode, lang, _ ->
                val q = buildString {
                    append(name.trim())
                    if (!season.isNullOrBlank()) append(" S${season.padStart(2, '0')}")
                    if (!episode.isNullOrBlank()) append("E${episode.padStart(2, '0')}")
                }
                subtitleViewModel.searchByFile(null, q, lang)
                showSearchForm = false
            }
        )

        if (showLocalSubsSheet) {
            val subExtensions = setOf("srt", "vtt", "ass", "ssa", "sub")
            val localSubFiles = remember(showLocalSubsSheet) {
                listOfNotNull(
                    currentFilePath?.let { java.io.File(it).parentFile },
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    context.getExternalFilesDir(null)
                ).flatMap { dir ->
                    if (dir == null || !dir.exists()) emptyList()
                    else dir.walkTopDown().maxDepth(3).filter { it.isFile && it.extension.lowercase() in subExtensions }.toList()
                }.distinctBy { it.absolutePath }.sortedByDescending { it.lastModified() }
            }

            ModalBottomSheet(
                onDismissRequest = { showLocalSubsSheet = false },
                containerColor = Color(0xFF1C1C1C),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                modifier = Modifier.fillMaxHeight(0.55f)
            ) {
                Column(Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
                    Text(
                        "LOCAL SUBTITLES",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = Color(0xFFFFAB40),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    if (localSubFiles.isEmpty()) {
                        Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("No subtitle files found nearby", fontSize = 13.sp, color = Color.White.copy(0.5f))
                        }
                    } else {
                        LazyColumn(Modifier.fillMaxWidth()) {
                            items(localSubFiles) { file ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().height(48.dp)
                                        .clickable { applyLocalSubtitleFile(file); showLocalSubsSheet = false }
                                        .padding(horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Rounded.Description, null, tint = Color(0xFFFFAB40), modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(12.dp))
                                    Text(file.name, fontSize = 13.sp, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                                    Text("${file.length() / 1024}KB", fontSize = 10.sp, color = Color.White.copy(0.4f))
                                    Spacer(Modifier.width(10.dp))
                                    Icon(Icons.Rounded.PlayArrow, null, tint = Color(0xFFFFAB40), modifier = Modifier.size(16.dp))
                                }
                                HorizontalDivider(thickness = 0.5.dp, color = Color.White.copy(0.06f), modifier = Modifier.padding(horizontal = 16.dp))
                            }
                        }
                    }
                }
            }
        }

        // === Inline equalizer panel (bottom sheet) ===
        if (showEqPanel) {
            InlineEqualizerPanel(onDismiss = { showEqPanel = false })
        }

        // === Audio tracks sheet ===
        if (showAudioSheet) {
            val groups = player?.currentTracks?.groups?.filter { it.type == C.TRACK_TYPE_AUDIO }.orEmpty()
            ModalBottomSheet(
                onDismissRequest = { showAudioSheet = false },
                containerColor = Color(0xFF1C1C1C),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            ) {
                Column(Modifier.fillMaxWidth().padding(bottom = 32.dp)) {
                    Text("Audio tracks", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(20.dp))
                    if (groups.isEmpty()) {
                        Text("No audio tracks available", color = Color.White.copy(alpha = 0.60f), fontSize = 15.sp,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp))
                    } else {
                        groups.forEachIndexed { gIdx, group ->
                            for (tIdx in 0 until group.length) {
                                val fmt = group.getTrackFormat(tIdx)
                                val isSelected = group.isTrackSelected(tIdx)
                                val label = buildString {
                                    append(fmt?.language?.uppercase() ?: "Track ${gIdx + 1}")
                                    fmt?.label?.let { if (it.isNotBlank()) append(" · $it") }
                                    fmt?.channelCount?.takeIf { it > 0 }?.let { append(" · ${it}ch") }
                                }
                                Row(
                                    Modifier.fillMaxWidth().clickable {
                                        player?.let { p ->
                                            p.trackSelectionParameters = p
                                                .trackSelectionParameters
                                                .buildUpon()
                                                .setOverrideForType(TrackSelectionOverride(group.mediaTrackGroup, tIdx))
                                                .build()
                                        }
                                        showAudioSheet = false
                                    }.padding(horizontal = 20.dp, vertical = 14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(label, fontSize = 15.sp,
                                        color = if (isSelected) LocalAccentColor.current else Color.White)
                                    if (isSelected) {
                                        Icon(Icons.Rounded.Check, null, tint = LocalAccentColor.current, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // === Video info sheet ===
        if (showVideoInfo) {
            val filePath = player?.currentMediaItem?.localConfiguration?.uri?.path ?: ""
            val videoFormat = player?.currentTracks?.groups?.flatMap { g ->
                (0 until g.length).map { g.getTrackFormat(it) }
            }?.firstOrNull { it?.sampleMimeType?.startsWith("video/") == true }
            val audioFormat = player?.currentTracks?.groups?.flatMap { g ->
                (0 until g.length).map { g.getTrackFormat(it) }
            }?.firstOrNull { it?.sampleMimeType?.startsWith("audio/") == true }
            val file = remember(filePath) { File(filePath) }
            val fileSize = remember(file) { formatFileSize(file.length()) }
            ModalBottomSheet(
                onDismissRequest = { showVideoInfo = false },
                containerColor = Color(0xFF1C1C1C),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            ) {
                Column(Modifier.fillMaxWidth().padding(bottom = 32.dp)) {
                    Text("Video info", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(20.dp))
                    @Composable
                    fun InfoRow(label: String, value: String) {
                        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(label, fontSize = 15.sp, color = Color.White.copy(alpha = 0.60f))
                            Text(value, fontSize = 15.sp, color = Color.White)
                        }
                    }
                    InfoRow("Title", extractShowName(filePath))
                    InfoRow("File name", file.name)
                    InfoRow("File size", fileSize)
                    InfoRow("Duration", TimeFormatter.msToClock(player?.duration ?: 0L))
                    videoFormat?.let { f ->
                        InfoRow("Resolution", "${f.width} × ${f.height}")
                        f.sampleMimeType?.let { InfoRow("Video codec", it.substringAfter("video/")) }
                    }
                    audioFormat?.let { f ->
                        f.sampleMimeType?.let { InfoRow("Audio codec", it.substringAfter("audio/")) }
                        if (f.channelCount > 0) InfoRow("Channels", "${f.channelCount}")
                    }
                }
            }
        }
        if (showDeleteConfirm) {
            val filePath = player?.currentMediaItem?.localConfiguration?.uri?.path ?: ""
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                containerColor = Color(0xFF1C1C1C),
                shape = RoundedCornerShape(16.dp),
                title = { Text("Delete video?", color = Color.White, fontWeight = FontWeight.SemiBold) },
                text = {
                    Text(
                        "This will permanently delete\n\"${File(filePath).name}\"",
                        color = Color.White.copy(alpha = 0.70f)
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        val file = File(filePath)
                        try {
                            if (file.delete()) {
                                android.widget.Toast.makeText(context, "Deleted", android.widget.Toast.LENGTH_SHORT).show()
                                showDeleteConfirm = false
                                onClose()
                            } else {
                                android.widget.Toast.makeText(context, "Could not delete file", android.widget.Toast.LENGTH_SHORT).show()
                                showDeleteConfirm = false
                            }
                        } catch (e: Exception) {
                            android.widget.Toast.makeText(context, "Error: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                            showDeleteConfirm = false
                        }
                    }) {
                        Text("Delete", color = Color(0xFFFF4444), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirm = false }) {
                        Text("Cancel", color = Color.White.copy(alpha = 0.70f))
                    }
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
                    .clip(LumenSquircle(11.dp))
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

        // Track layers: base, buffered (lighter), played (amber)
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .height(trackHeight)
                .clip(RoundedCornerShape(trackHeight / 2))
                .background(Color.White.copy(alpha = 0.12f))
        ) {
            Box(
                Modifier
                    .fillMaxWidth(bufferedFraction.coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .background(Color.White.copy(alpha = 0.30f))
            )
            Box(
                Modifier
                    .fillMaxWidth(playedFraction.coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .background(LocalAccentColor.current)
            )
        }

        // Glow behind thumb while dragging
        if (isActive) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = thumbXDp)
                    .size(thumbSize * 1.8f)
                    .clip(CircleShape)
                    .background(LocalAccentColor.current.copy(alpha = 0.25f))
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
@OptIn(ExperimentalFoundationApi::class)
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
    onOpenEqPanel: () -> Unit,
    locked: Boolean,
    resizeMode: Int,
    modifier: Modifier = Modifier
) {
    val s = stateProvider()
    val duration = s.durationMs.coerceAtLeast(0L)
    val context = LocalContext.current

    Box(
        modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    0f to Color.Transparent,
                    1f to Color.Black.copy(alpha = 0.92f)
                )
            )
            .padding(top = 8.dp, bottom = 24.dp, start = 12.dp, end = 12.dp)
    ) {
        Column(Modifier.fillMaxWidth()) {

            // Seek bar
            VideoSeekBar(
                positionProvider = { s.positionMs },
                bufferedProvider = { s.bufferedMs },
                duration = duration,
                seekDragging = seekDragging,
                seekFraction = seekPreviewFraction,
                onDraggingChange = onSeekDraggingChange,
                onPreviewChange = onSeekPreviewChange,
                onSeekCommit = onSeekCommit,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp)
            )

            // Timestamps
            Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 2.dp)) {
                val displayMs = if (seekPreviewFraction != null)
                    (seekPreviewFraction * duration).toLong() else s.positionMs
                Text(TimeFormatter.msToClock(displayMs), color = Color.White,
                    style = MaterialTheme.typography.labelSmall)
                Spacer(Modifier.weight(1f))
                Text(TimeFormatter.msToClock(duration), color = Color.White,
                    style = MaterialTheme.typography.labelSmall)
            }

            Spacer(Modifier.height(8.dp))

            // Transport row
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPrevious, enabled = s.hasPrevious, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Rounded.SkipPrevious, "Previous",
                        tint = if (s.hasPrevious) Color.White else Color.White.copy(alpha = 0.3f),
                        modifier = Modifier.size(30.dp))
                }
                FilledIconButton(
                    onClick = onPlayPause,
                    modifier = Modifier.size(60.dp),
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = Color.White, contentColor = Color.Black)
                ) {
                    Crossfade(s.isPlaying, animationSpec = tween(150), label = "pp") { playing ->
                        Icon(if (playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            "Play/Pause", modifier = Modifier.size(34.dp))
                    }
                }
                IconButton(onClick = onNext, enabled = s.hasNext, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Rounded.SkipNext, "Next",
                        tint = if (s.hasNext) Color.White else Color.White.copy(alpha = 0.3f),
                        modifier = Modifier.size(30.dp))
                }
            }

            Spacer(Modifier.height(12.dp))

            // Bottom row: left icons + right skip intro
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // LEFT SIDE: 1x, Fit, EQ
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SmallControlButton(Icons.Rounded.Speed, formatSpeed(s.playbackSpeed), onOpenSpeed)
                    SmallControlButton(Icons.Rounded.AspectRatio, aspectLabel(resizeMode), onOpenAspect)
                    SmallControlButton(Icons.Rounded.GraphicEq, "EQ", onOpenEqPanel)
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .combinedClickable(
                                onClick = {},
                                onLongClick = {
                                    val activity = context as? Activity
                                    activity?.let {
                                        it.requestedOrientation = if (
                                            it.requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE ||
                                            it.requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                                        ) ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                                        else ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                                    }
                                }
                            )
                    )
                }

                // RIGHT SIDE: +85s skip intro button (click skip, long-pick duration)
                var showSkipPicker by remember { mutableStateOf(false) }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .combinedClickable(
                            onClick = { onSeekBy(85_000L) },
                            onLongClick = { showSkipPicker = true }
                        )
                        .padding(horizontal = 8.dp)
                ) {
                    Icon(Icons.Rounded.SkipNext, "Skip Intro",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp))
                    Text("+85s", color = MaterialTheme.colorScheme.primary,
                        fontSize = 10.sp,
                        style = MaterialTheme.typography.labelSmall)
                }
                if (showSkipPicker) {
                    DropdownMenu(
                        expanded = showSkipPicker,
                        onDismissRequest = { showSkipPicker = false },
                        modifier = Modifier
                            .background(Color(0xFF1E1E1E))
                            .width(120.dp)
                    ) {
                        listOf(30, 60, 85, 90, 120, 180, 300).forEach { seconds ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "+${seconds}s",
                                        color = if (seconds == 85) MaterialTheme.colorScheme.primary else Color.White,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                },
                                onClick = {
                                    onSeekBy(seconds * 1000L)
                                    showSkipPicker = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VerticalHud(hud: HudInfo, modifier: Modifier = Modifier) {
    Surface(
        color = Color.Black.copy(alpha = 0.55f),
        shape = RoundedCornerShape(50.dp),
        modifier = modifier
    ) {
        Column(
            Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(hud.icon, null, tint = Color.White, modifier = Modifier.size(16.dp))
            // Vertical bar: 6dp wide, 90dp tall, filled from the bottom up
            Box(
                Modifier
                    .width(6.dp)
                    .height(90.dp)
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
            shape = LumenSquircle(16.dp),
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
    AspectRatioFrameLayout.RESIZE_MODE_FILL -> "Wide"
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
    onPinch: (zoom: Float) -> Unit,
    onPlayerViewCreated: (PlayerView) -> Unit = {}
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
            }.also { onPlayerViewCreated(it) }
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

@SuppressLint("SimpleDateFormat")
private suspend fun takeScreenshot(context: Context, playerView: PlayerView) {
    withContext(Dispatchers.IO) {
        try {
            val surfaceView = playerView.videoSurfaceView ?: return@withContext
            val bmp = captureViewToBitmap(surfaceView)
            if (bmp == null) {
                withContext(Dispatchers.Main) {
                    android.widget.Toast.makeText(context, "Screenshot failed (capture returned null)", android.widget.Toast.LENGTH_SHORT).show()
                }
                return@withContext
            }
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val fileName = "Lumen_$timestamp.jpg"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Lumen")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
                val resolver = context.contentResolver
                val outUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                if (outUri == null) {
                    withContext(Dispatchers.Main) {
                        android.widget.Toast.makeText(context, "Screenshot failed (no URI)", android.widget.Toast.LENGTH_SHORT).show()
                    }
                    return@withContext
                }
                resolver.openOutputStream(outUri)?.use { out ->
                    bmp.compress(Bitmap.CompressFormat.JPEG, 95, out)
                }
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(outUri, values, null, null)
                withContext(Dispatchers.Main) {
                    android.widget.Toast.makeText(context, "Screenshot saved to Pictures/Lumen", android.widget.Toast.LENGTH_SHORT).show()
                }
            } else {
                val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val lumenDir = java.io.File(picturesDir, "Lumen").apply { mkdirs() }
                val file = java.io.File(lumenDir, fileName)
                file.outputStream().use { out ->
                    bmp.compress(Bitmap.CompressFormat.JPEG, 95, out)
                }
                MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath), arrayOf("image/jpeg"), null)
                withContext(Dispatchers.Main) {
                    android.widget.Toast.makeText(context, "Screenshot saved", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                android.widget.Toast.makeText(context, "Screenshot failed: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }
}

private suspend fun captureViewToBitmap(view: View): Bitmap? = suspendCancellableCoroutine { cont ->
    try {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val handlerThread = HandlerThread("PixelCopyHelper")
        handlerThread.start()
        val handler = Handler(handlerThread.looper)
        when (view) {
            is SurfaceView -> {
                PixelCopy.request(view, bitmap, { result ->
                    handlerThread.quitSafely()
                    if (result == PixelCopy.SUCCESS) {
                        cont.resume(bitmap)
                    } else {
                        bitmap.recycle()
                        cont.resume(null)
                    }
                }, handler)
            }
            is TextureView -> {
                val tvBitmap = view.getBitmap(view.width, view.height)
                handlerThread.quitSafely()
                cont.resume(tvBitmap)
            }
            else -> {
                handlerThread.quitSafely()
                bitmap.recycle()
                cont.resume(null)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        cont.resume(null)
    }
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

// ══════════════════════════════════════════
// INLINE EQUALIZER PANEL
// ══════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InlineEqualizerPanel(
    onDismiss: () -> Unit,
    viewModel: EqualizerViewModel = hiltViewModel()
) {
    val state by viewModel.ui.collectAsStateWithLifecycle()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color(0xFF0E0E12),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
                .heightIn(max = 520.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                Modifier.fillMaxWidth().padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Equalizer",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
                Switch(checked = state.enabled, onCheckedChange = viewModel::setEnabled)
            }

            if (state.bandCount == 0) {
                Text(
                    "Equalizer unavailable — start playback first.",
                    color = Color.White.copy(alpha = 0.5f),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(vertical = 24.dp)
                )
            } else {
                Column(Modifier.alpha(if (state.enabled) 1f else 0.4f)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        for (band in 0 until state.bandCount) {
                            val current = state.bandLevels.getOrNull(band)?.toFloat() ?: 0f
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxHeight().width(44.dp)
                            ) {
                                Box(Modifier.weight(1f).graphicsLayer(rotationZ = -90f)) {
                                    Slider(
                                        value = current,
                                        onValueChange = { viewModel.setBand(band, it.toInt().toShort()) },
                                        valueRange = state.minMb.toFloat()..state.maxMb.toFloat(),
                                        enabled = state.enabled,
                                        modifier = Modifier.width(180.dp)
                                    )
                                }
                                Text(
                                    formatHz(state.bandFreqs.getOrNull(band) ?: 0),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }

                    if (state.presets.isNotEmpty()) {
                        Text(
                            "Preset",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        )
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            state.presets.forEachIndexed { idx, name ->
                                FilterChip(
                                    selected = idx == state.selectedPreset,
                                    onClick = { if (state.enabled) viewModel.usePreset(idx) },
                                    enabled = state.enabled,
                                    label = { Text(name) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatHz(hz: Int): String =
    if (hz >= 1000) "${hz / 1000}k" else "$hz"

// ══════════════════════════════════════════
// HELPERS
// ══════════════════════════════════════════

private fun extractShowName(filePath: String): String {
    val file = File(filePath)
    val parent = file.parentFile?.name ?: ""
    val blacklist = setOf("download", "downloads", "movies", "videos", "anime", "media")
    if (parent.isNotBlank() && parent.lowercase() !in blacklist) return parent
    return file.nameWithoutExtension
        .replace(Regex("\\[.*?\\]"), "")
        .replace(Regex("(?i)(S\\d+E\\d+|E\\d+|EP\\d+|Episode\\s*\\d+).*"), "")
        .replace(Regex("[._-]+"), " ")
        .trim()
}

private fun extractEpisodeInfo(fileName: String): Pair<String, Int?> {
    val regex = Regex("""(?i)(?:S(\d+))?\s*E(\d+)|EP\s*(\d+)|Episode\s*(\d+)""")
    val match = regex.find(fileName)
    if (match != null) {
        val groups = match.groupValues.drop(1).filter { it.isNotBlank() }
        if (groups.isNotEmpty()) return "Episode ${groups.last()}" to groups.last().toIntOrNull()
    }
    return "" to null
}

private fun formatFileSize(bytes: Long): String = when {
    bytes >= 1_073_741_824L -> "%.2f GB".format(bytes / 1_073_741_824.0)
    bytes >= 1_048_576L -> "%.0f MB".format(bytes / 1_048_576.0)
    bytes >= 1024L -> "%.0f KB".format(bytes / 1024.0)
    else -> "$bytes B"
}

private fun buildEmbeddedTrackList(tracks: Tracks?): List<EmbeddedTrack> {
    if (tracks == null) return emptyList()
    val out = mutableListOf<EmbeddedTrack>()
    tracks.groups.forEach { group ->
        val tg = group.mediaTrackGroup
        if (tg.type != C.TRACK_TYPE_TEXT) return@forEach
        for (i in 0 until tg.length) {
            val f = tg.getFormat(i)
            val label = f.label?.toString() ?: "Track ${i + 1}"
            val lang = f.language?.uppercase() ?: "UND"
            out += EmbeddedTrack(
                id = "${tg.id}.$i",
                groupId = tg.id,
                trackIndex = i,
                language = lang,
                label = label,
                index = i,
                isActive = group.isTrackSelected(i)
            )
        }
    }
    return out
}

private fun detectMime(path: String): String = when {
    path.endsWith(".srt", true) -> "application/x-subrip"
    path.endsWith(".vtt", true) -> "text/vtt"
    path.endsWith(".ass", true) || path.endsWith(".ssa", true) -> "text/x-ssa"
    else -> "application/x-subrip"
}

@Composable
private fun SmallControlButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(icon, contentDescription = label, tint = Color.White,
            modifier = Modifier.size(20.dp))
        Spacer(Modifier.height(2.dp))
        Text(label, color = Color.White, fontSize = 9.sp,
            maxLines = 1, overflow = TextOverflow.Clip,
            style = MaterialTheme.typography.labelSmall)
    }
}
