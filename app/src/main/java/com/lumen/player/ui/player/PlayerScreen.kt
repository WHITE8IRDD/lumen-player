package com.lumen.player.ui.player

import android.app.Activity
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ClosedCaption
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Equalizer
import androidx.compose.material.icons.rounded.Lyrics
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PlaylistPlay
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.lumen.player.R
import com.lumen.player.core.util.TimeFormatter
import com.lumen.player.ui.player.components.LyricsView
import com.lumen.player.ui.player.components.TrackSelectorSheet
import kotlinx.coroutines.delay

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    onCollapse: () -> Unit,
    onOpenEqualizer: () -> Unit = {},
    onOpenQueue: () -> Unit = {},
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    val lyrics by viewModel.lyrics.collectAsStateWithLifecycle()
    val tracks by viewModel.tracks.collectAsStateWithLifecycle()

    val activity = context as? Activity
    DisposableEffect(ui.controlsVisible) {
        @Suppress("DEPRECATION")
        fun updateFullscreen(visible: Boolean) {
            if (visible) {
                activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            } else {
                activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            }
        }
        updateFullscreen(ui.controlsVisible)
        onDispose { updateFullscreen(true) }
    }

    BackHandler { onCollapse() }

    LaunchedEffect(ui.controlsVisible) {
        if (ui.controlsVisible) {
            delay(3_000)
            viewModel.setControlsVisible(false)
        }
    }

    var baseSpeed by remember { mutableStateOf(1f) }

    Box(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .playerGestures(
                    locked = false,
                    onTapToggle = { viewModel.setControlsVisible(!ui.controlsVisible) },
                    onDoubleTapForward = { viewModel.seekBy(10_000); viewModel.setControlsVisible(true) },
                    onDoubleTapBack = { viewModel.seekBy(-10_000); viewModel.setControlsVisible(true) },
                    onLongPressStart = {
                        baseSpeed = state.playbackSpeed
                        viewModel.setSpeed(2f)
                    },
                    onLongPressEnd = { viewModel.setSpeed(baseSpeed) },
                    onVerticalLeft = {},
                    onVerticalRight = { viewModel.adjustVolume(it) },
                    onPinchZoom = {}
                )
    ) {
        AsyncImage(
            model = state.artworkUri,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .blur(60.dp)
        )

        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color.Black.copy(alpha = 0.5f),
                        0.5f to Color.Black.copy(alpha = 0.7f),
                        1f to Color.Black.copy(alpha = 0.92f)
                    )
                )
        )

        Column(
            Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars)
        ) {
            TopBar(
                onCollapse = onCollapse,
                onOpenEqualizer = onOpenEqualizer,
                onOpenQueue = onOpenQueue
            )

            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                when (ui.uiMode) {
                    UiMode.LYRICS -> LyricsView(
                        lines = lyrics,
                        positionMs = state.positionMs
                    )
                    else -> {
                        AnimatedAlbumArt(
                            artworkUri = state.artworkUri,
                            isPlaying = state.isPlaying
                        )
                    }
                }
            }

            Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
                Text(
                    state.title.ifBlank { stringResource(R.string.nothing_playing) },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    maxLines = 2
                )
                Text(
                    listOf(state.artist, state.album).filter { it.isNotBlank() }.joinToString(" • "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.75f),
                    maxLines = 1
                )
            }

            Spacer(Modifier.height(12.dp))

            if (state.isBuffering) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().height(2.dp)
                )
            }

            ScrubbingSlider(
                progress = if (state.durationMs > 0L)
                    state.positionMs.toFloat() / state.durationMs.toFloat()
                else 0f,
                onSeek = { frac -> viewModel.seekTo((frac * state.durationMs).toLong()) }
            )

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(TimeFormatter.msToClock(state.positionMs), color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.labelMedium)
                Text(TimeFormatter.msToClock(state.durationMs), color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.labelMedium)
            }

            Spacer(Modifier.height(8.dp))

            PlayerControlsBar(
                isPlaying = state.isPlaying,
                shuffle = state.shuffle,
                repeatMode = state.repeatMode,
                hasPrevious = state.hasPrevious,
                hasNext = state.hasNext,
                onPlayPause = viewModel::playPause,
                onNext = viewModel::next,
                onPrevious = viewModel::previous,
                onShuffle = viewModel::toggleShuffle,
                onRepeat = viewModel::cycleRepeat
            )

            Spacer(Modifier.height(4.dp))

            SecondaryActions(
                speed = state.playbackSpeed,
                lyricsMode = ui.uiMode == UiMode.LYRICS,
                onSpeed = viewModel::cyclePlaybackSpeed,
                onLyrics = { viewModel.setUiMode(if (ui.uiMode == UiMode.LYRICS) UiMode.MAIN else UiMode.LYRICS) },
                onTracks = { viewModel.setUiMode(UiMode.TRACKS) },
                sleepTimerViewModel = viewModel
            )
        }

        ui.volume?.let { HudIndicator(Icons.Rounded.VolumeUp, it, "${(it * 100).toInt()}%") }

        when (ui.uiMode) {
            UiMode.TRACKS -> TrackModeSheet(
                tracks = tracks,
                onSelect = { gid, idx -> viewModel.selectTrack(gid, idx) },
                onDismiss = { viewModel.setUiMode(UiMode.MAIN) }
            )
            else -> Unit
        }
    }
}

@Composable
private fun TopBar(
    onCollapse: () -> Unit,
    onOpenEqualizer: () -> Unit,
    onOpenQueue: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onCollapse) {
            Icon(
                Icons.Rounded.ExpandMore,
                contentDescription = stringResource(R.string.cd_collapse),
                tint = Color.White
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            stringResource(R.string.now_playing),
            color = Color.White,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.weight(1f))
        IconButton(onClick = onOpenEqualizer) {
            Icon(Icons.Rounded.Equalizer, contentDescription = stringResource(R.string.cd_equalizer), tint = Color.White)
        }
        IconButton(onClick = onOpenQueue) {
            Icon(Icons.Rounded.PlaylistPlay, contentDescription = stringResource(R.string.cd_queue), tint = Color.White)
        }
    }
}

@Composable
private fun AnimatedAlbumArt(
    artworkUri: android.net.Uri?,
    isPlaying: Boolean
) {
    val artScale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isPlaying) 1f else 0.85f,
        animationSpec = androidx.compose.animation.core.tween(450),
        label = "artScale"
    )
    val artCorner by androidx.compose.animation.core.animateDpAsState(
        targetValue = if (isPlaying) 24.dp else 36.dp,
        label = "artCorner"
    )
    Box(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = artworkUri,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(1f)
                .graphicsLayer {
                    scaleX = artScale
                    scaleY = artScale
                }
                .clip(RoundedCornerShape(artCorner))
        )
    }
}

@Composable
private fun HudIndicator(icon: ImageVector, value: Float, label: String) {
    Box(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black.copy(alpha = 0.55f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Color.White)
            }
            Slider(
                value = value,
                onValueChange = {},
                valueRange = 0f..1f,
                enabled = false,
                colors = SliderDefaults.colors(
                    disabledThumbColor = Color.White,
                    disabledActiveTrackColor = Color.White,
                    disabledInactiveTrackColor = Color.White.copy(alpha = 0.3f)
                ),
                modifier = Modifier.width(160.dp)
            )
            Text(label, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        }
    }
}

@Composable
private fun PlayerControlsBar(
    isPlaying: Boolean,
    shuffle: Boolean,
    repeatMode: Int,
    hasPrevious: Boolean,
    hasNext: Boolean,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onShuffle: () -> Unit,
    onRepeat: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onShuffle) {
            Icon(
                Icons.Rounded.Shuffle,
                contentDescription = stringResource(R.string.cd_shuffle),
                tint = if (shuffle) MaterialTheme.colorScheme.primary else Color.White
            )
        }
        IconButton(onClick = onPrevious, enabled = hasPrevious) {
            Icon(
                Icons.Rounded.SkipPrevious,
                contentDescription = stringResource(R.string.cd_previous),
                tint = if (hasPrevious) Color.White else Color.White.copy(alpha = 0.3f),
                modifier = Modifier.size(36.dp)
            )
        }
        FilledIconButton(
            onClick = onPlayPause,
            modifier = Modifier.size(72.dp),
            shape = CircleShape,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = Color.White,
                contentColor = Color.Black
            )
        ) {
            Icon(
                if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                contentDescription = stringResource(R.string.cd_play_pause),
                modifier = Modifier.size(40.dp)
            )
        }
        IconButton(onClick = onNext, enabled = hasNext) {
            Icon(
                Icons.Rounded.SkipNext,
                contentDescription = stringResource(R.string.cd_next),
                tint = if (hasNext) Color.White else Color.White.copy(alpha = 0.3f),
                modifier = Modifier.size(36.dp)
            )
        }
        IconButton(onClick = onRepeat) {
            val (icon, tint) = when (repeatMode) {
                androidx.media3.common.Player.REPEAT_MODE_ONE ->
                    Icons.Rounded.RepeatOne to MaterialTheme.colorScheme.primary
                androidx.media3.common.Player.REPEAT_MODE_ALL ->
                    Icons.Rounded.Repeat to MaterialTheme.colorScheme.primary
                else -> Icons.Rounded.Repeat to Color.White
            }
            Icon(icon, contentDescription = stringResource(R.string.cd_repeat_mode), tint = tint)
        }
    }
}

@Composable
private fun SecondaryActions(
    speed: Float,
    lyricsMode: Boolean,
    onSpeed: () -> Unit,
    onLyrics: () -> Unit,
    onTracks: () -> Unit,
    sleepTimerViewModel: PlayerViewModel
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(onClick = onSpeed) {
            Icon(Icons.Rounded.Speed, contentDescription = null, tint = Color.White)
            Spacer(Modifier.width(4.dp))
            Text(String.format("%.2fx", speed), color = Color.White)
        }
        IconButton(onClick = onLyrics) {
            Icon(
                Icons.Rounded.Lyrics,
                contentDescription = stringResource(R.string.cd_lyrics),
                tint = if (lyricsMode) MaterialTheme.colorScheme.primary else Color.White
            )
        }
        IconButton(onClick = onTracks) {
            Icon(Icons.Rounded.ClosedCaption, contentDescription = stringResource(R.string.cd_subtitles), tint = Color.White)
        }
        SleepTimerMenu(sleepTimerViewModel)
    }
}

@Composable
private fun SleepTimerMenu(viewModel: PlayerViewModel) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Rounded.Schedule, contentDescription = stringResource(R.string.cd_sleep_timer), tint = Color.White)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            listOf(0, 5, 10, 15, 30, 45, 60).forEach { mins ->
                DropdownMenuItem(
                    text = { Text(if (mins == 0) stringResource(R.string.sleep_off) else "$mins min") },
                    onClick = {
                        viewModel.setSleepTimer(mins)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun ScrubbingSlider(
    progress: Float,
    onSeek: (Float) -> Unit
) {
    var drag by remember { mutableStateOf<Float?>(null) }
    Slider(
        value = (drag ?: progress).coerceIn(0f, 1f),
        onValueChange = { drag = it },
        onValueChangeFinished = {
            drag?.let { onSeek(it) }
            drag = null
        },
        colors = SliderDefaults.colors(
            thumbColor = Color.White,
            activeTrackColor = Color.White,
            inactiveTrackColor = Color.White.copy(alpha = 0.25f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
    )
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun TrackModeSheet(
    tracks: androidx.media3.common.Tracks?,
    onSelect: (String?, Int) -> Unit,
    onDismiss: () -> Unit
) {
    var sheet by remember { mutableStateOf(0) }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                TextButton(onClick = { sheet = 0 }) { Text(stringResource(R.string.audio_track)) }
                TextButton(onClick = { sheet = 1 }) { Text(stringResource(R.string.subtitle_track)) }
                TextButton(onClick = { sheet = 2 }) { Text(stringResource(R.string.video_track)) }
            }
            when (sheet) {
                0 -> TrackSelectorSheet(stringResource(R.string.audio_track), androidx.media3.common.C.TRACK_TYPE_AUDIO, tracks, {}, onSelect)
                1 -> TrackSelectorSheet(stringResource(R.string.subtitle_track), androidx.media3.common.C.TRACK_TYPE_TEXT, tracks, {}, onSelect)
                else -> TrackSelectorSheet(stringResource(R.string.video_track), androidx.media3.common.C.TRACK_TYPE_VIDEO, tracks, {}, onSelect)
            }
        }
    }
}


