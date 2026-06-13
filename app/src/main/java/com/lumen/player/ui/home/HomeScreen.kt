package com.lumen.player.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import com.lumen.player.ui.components.LumenSquircle
import com.lumen.player.ui.components.ResolutionBadge
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil.compose.SubcomposeAsyncImage
import com.lumen.player.R
import com.lumen.player.core.util.TimeFormatter
import com.lumen.player.domain.model.Folder
import com.lumen.player.domain.model.MediaItem
import com.lumen.player.player.PlaybackState
import com.lumen.player.ui.components.DialogVariant
import com.lumen.player.ui.components.GlassNavigationBar
import com.lumen.player.ui.components.NavTab
import com.lumen.player.ui.components.LumenConfirmationDialog
import com.lumen.player.ui.components.SectionLabel
import com.lumen.player.ui.components.MiniPlayer
import com.lumen.player.ui.components.ShimmerBox
import com.lumen.player.ui.components.FolderMosaicThumbnail
import com.lumen.player.core.theme.LumenAmber
import com.lumen.player.core.theme.LumenAmberMuted
import com.lumen.player.ui.components.GradientBlob
import com.lumen.player.ui.components.HomeAccents
import com.lumen.player.ui.components.HomeSectionRowSkeleton
import com.lumen.player.ui.components.LumenBlob
import com.lumen.player.ui.components.LumenEmptyState
import com.lumen.player.ui.components.Mood
import com.lumen.player.ui.navigation.Destinations
import com.lumen.player.ui.util.FolderThumbnailResolver
import com.lumen.player.ui.util.FolderThumbnailType
import com.lumen.player.ui.util.MosaicFetcherInput
import com.lumen.player.ui.util.VideoThumbnail
import com.lumen.player.ui.util.pressableScale

private val NAV_TABS = listOf(
    NavTab(Destinations.HOME, R.string.tab_home, Icons.Rounded.Home, Icons.Rounded.Home),
    NavTab(Destinations.LIBRARY, R.string.tab_library, Icons.Rounded.LibraryMusic, Icons.Rounded.LibraryMusic),
    NavTab(Destinations.SETTINGS, R.string.tab_settings, Icons.Rounded.Settings, Icons.Rounded.Settings)
)

@UnstableApi
@Composable
fun HomeScaffold(
    rootNav: NavHostController,
    playback: PlaybackState,
    onPlayPauseMini: () -> Unit,
    onExpandPlayer: () -> Unit,
    onDismissMini: () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    val backStack by rootNav.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    Scaffold(
        bottomBar = {
            Column(Modifier.navigationBarsPadding()) {
                AnimatedVisibility(
                    visible = playback.currentItemId != null,
                    enter = slideInVertically { it },
                    exit = slideOutVertically { it }
                ) {
                    MiniPlayer(
                        state = playback,
                        onPlayPause = onPlayPauseMini,
                        onClick = onExpandPlayer,
                        onDismiss = onDismissMini
                    )
                }
                GlassNavigationBar(
                    currentRoute = currentRoute,
                    tabs = NAV_TABS,
                    onTabSelected = { route ->
                        rootNav.navigate(route) {
                            popUpTo(rootNav.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        },
        content = content
    )
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@UnstableApi
@Composable
fun HomeScreen(
    onSearchClick: () -> Unit = {},
    onItemClick: (MediaItem) -> Unit = {},
    onFolderClick: (Folder) -> Unit = {},
    onSeeAll: (String) -> Unit = {},
    onLongClick: ((MediaItem) -> Unit)? = null,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val cw by viewModel.continueWatching.collectAsStateWithLifecycle()
    val recent by viewModel.recentlyAdded.collectAsStateWithLifecycle()
    val folders by viewModel.topFolders.collectAsStateWithLifecycle()
    val audio by viewModel.recentAudio.collectAsStateWithLifecycle()
    val refreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

    val homeVideosByFolder = remember(cw, recent) {
        (cw + recent).groupBy { it.folderPath }
    }
    var contextMenuItem by remember { mutableStateOf<MediaItem?>(null) }
    var showHideFromHomeDialog by remember { mutableStateOf<MediaItem?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    if (showHideFromHomeDialog != null) {
        val item = showHideFromHomeDialog!!
        LumenConfirmationDialog(
            onDismiss = { showHideFromHomeDialog = null },
            onConfirm = {
                showHideFromHomeDialog = null
                viewModel.hideFromHome(item)
                scope.launch {
                    val result = snackbarHostState.showSnackbar(
                        message = "hidden from home",
                        actionLabel = "undo"
                    )
                    if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                        viewModel.unhideFromHome(item)
                    }
                }
            },
            title = "Hide from home?",
            body = "${item.cleanTitle.ifEmpty { item.title }} will be hidden from home carousels. It will still be available in your library.",
            confirmLabel = "Hide",
            variant = DialogVariant.NEUTRAL,
            icon = Icons.Rounded.VisibilityOff
        )
    }

    Box {
        val allEmpty = cw.isEmpty() && recent.isEmpty() && folders.isEmpty() && audio.isEmpty()

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 24.dp, bottom = 16.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
        ) {
            item {
                HomeGreetingSection()
            }

            item {
                SearchBar(onClick = onSearchClick)
            }

            if (refreshing && allEmpty) {
                item { HomeSectionRowSkeleton() }
                item { HomeSectionRowSkeleton() }
                item { HomeSectionRowSkeleton() }
            } else if (!allEmpty) {
                if (cw.isNotEmpty()) {
                    item { SectionHeader("Continue watching", color = LumenAmber, onSeeAll = { onSeeAll("continue_watching") }) }
                    item {
                        ContinueWatchingRow(
                            items = cw,
                            onClick = onItemClick,
                            onLongClick = { contextMenuItem = it }
                        )
                    }
                }

                if (recent.isNotEmpty()) {
                    item { SectionHeader("Recently added", color = LumenAmber, onSeeAll = { onSeeAll("recently_added") }) }
                    item {
                        RecentlyAddedRow(
                            items = recent,
                            onClick = onItemClick,
                            onLongClick = { contextMenuItem = it }
                        )
                    }
                }

                if (folders.isNotEmpty()) {
                    item { SectionHeader("Your folders", color = LumenAmber, onSeeAll = { onSeeAll("folders") }) }
                    item {
                        HomeFolderRow(
                            folders = folders,
                            videosByFolder = homeVideosByFolder,
                            onClick = onFolderClick
                        )
                    }
                }

                if (audio.isNotEmpty()) {
                    item { SectionHeader("Recent audio", color = LumenAmber, onSeeAll = { onSeeAll("recent_audio") }) }
                    item {
                        RecentAudioColumn(
                            items = audio,
                            onClick = onItemClick
                        )
                    }
                }
            } else {
                item { HomeEmptyState(refreshing = refreshing, onRefresh = viewModel::refresh) }
            }

            item {
                Box(Modifier.fillMaxWidth().height(1.dp))
            }
        }

        GradientBlob(
            color = LumenAmberMuted,
            size = 180.dp,
            alpha = 0.25f,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-60).dp, y = 60.dp)
        )

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
            snackbar = { data ->
                Snackbar(
                    snackbarData = data,
                    shape = LumenSquircle(12.dp),
                    containerColor = MaterialTheme.colorScheme.surface
                )
            }
        )

        contextMenuItem?.let { item ->
            ModalBottomSheet(
                onDismissRequest = { contextMenuItem = null },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            ) {
                Column(Modifier.padding(bottom = 24.dp)) {
                    Text(
                        item.cleanTitle.ifBlank { item.title },
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                contextMenuItem = null
                                onItemClick(item)
                            }
                            .padding(horizontal = 24.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.PlayArrow, contentDescription = null, Modifier.size(24.dp))
                        Spacer(Modifier.width(16.dp))
                        Text("play", style = MaterialTheme.typography.bodyLarge)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                contextMenuItem = null
                                viewModel.hideFromContinueWatching(item)
                            }
                            .padding(horizontal = 24.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.VisibilityOff, contentDescription = null, Modifier.size(24.dp))
                        Spacer(Modifier.width(16.dp))
                        Text("hide from this list", style = MaterialTheme.typography.bodyLarge)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                contextMenuItem = null
                                showHideFromHomeDialog = item
                            }
                            .padding(horizontal = 24.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.VisibilityOff, contentDescription = null, Modifier.size(24.dp))
                        Spacer(Modifier.width(16.dp))
                        Text("hide from home", style = MaterialTheme.typography.bodyLarge)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { contextMenuItem = null }
                            .padding(horizontal = 24.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null, Modifier.size(24.dp))
                        Spacer(Modifier.width(16.dp))
                        Text("add to playlist", style = MaterialTheme.typography.bodyLarge)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { contextMenuItem = null }
                            .padding(horizontal = 24.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.Share, contentDescription = null, Modifier.size(24.dp))
                        Spacer(Modifier.width(16.dp))
                        Text("share", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }
}

private val morningSubtitles = listOf(
    "What shall we start the day with?",
    "Ready for something good?",
    "Set the mood for the day",
    "Morning pick, your choice"
)

private val afternoonSubtitles = listOf(
    "Afternoon break time?",
    "What are you in the mood for?",
    "Take a moment, watch something",
    "Midday escape?"
)

private val eveningSubtitles = listOf(
    "What are you in the mood for?",
    "Wind down with something good",
    "Evening picks await",
    "Let's find something cozy"
)

private val nightSubtitles = listOf(
    "Still up? Let's find something",
    "Late night picks",
    "One more episode?",
    "Night owl mode activated"
)

private enum class TimeSlot { MORNING, AFTERNOON, EVENING, NIGHT }

private fun getCurrentTimeSlot(): TimeSlot {
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 5..11 -> TimeSlot.MORNING
        in 12..16 -> TimeSlot.AFTERNOON
        in 17..21 -> TimeSlot.EVENING
        else -> TimeSlot.NIGHT
    }
}

private fun getGreetingTitle(slot: TimeSlot): String = when (slot) {
    TimeSlot.MORNING -> "Good morning"
    TimeSlot.AFTERNOON -> "Good afternoon"
    TimeSlot.EVENING -> "Good evening"
    TimeSlot.NIGHT -> "Good night"
}

private fun getSubtitlePool(slot: TimeSlot): List<String> = when (slot) {
    TimeSlot.MORNING -> morningSubtitles
    TimeSlot.AFTERNOON -> afternoonSubtitles
    TimeSlot.EVENING -> eveningSubtitles
    TimeSlot.NIGHT -> nightSubtitles
}

@Composable
private fun HomeGreetingSection() {
    val timeSlot = remember { getCurrentTimeSlot() }
    val title = remember(timeSlot) { getGreetingTitle(timeSlot) }
    val subtitle = remember(timeSlot) {
        getSubtitlePool(timeSlot).random()
    }
    Box(modifier = Modifier.fillMaxWidth()) {
        GradientBlob(
            color = LumenAmberMuted,
            size = 200.dp,
            alpha = 0.25f,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 80.dp, y = (-40).dp)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun SearchBar(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clickable(onClick = onClick),
        shape = LumenSquircle(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            Modifier
                .height(52.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Rounded.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(12.dp))
            Text(
                "Search your media...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String, color: Color, onSeeAll: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 8.dp, top = 16.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        TextButton(onClick = onSeeAll) {
            Text("See all", color = color, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ContinueWatchingRow(
    items: List<MediaItem>,
    onClick: (MediaItem) -> Unit,
    onLongClick: (MediaItem) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items, key = { it.id }) { item ->
            ContinueWatchingCard(item = item, onClick = { onClick(item) }, onLongClick = { onLongClick(item) })
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ContinueWatchingCard(item: MediaItem, onClick: () -> Unit, onLongClick: () -> Unit) {
    val progress = if (item.durationMs > 0L)
        (item.lastPositionMs.toFloat() / item.durationMs).coerceIn(0f, 1f) else 0f
    val interactionSource = remember { MutableInteractionSource() }
    Card(
        modifier = Modifier
            .width(200.dp)
            .pressableScale(interactionSource)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = LumenSquircle(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                SubcomposeAsyncImage(
                    model = VideoThumbnail(item.id, item.uri),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    loading = { ShimmerBox(modifier = Modifier.fillMaxSize()) }
                )
                Box(
                    Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(70.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f))
                            )
                        )
                )
                Box(
                    Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(Color.Black.copy(alpha = 0.35f))
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth(progress)
                            .height(3.dp)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
                ResolutionBadge(
                    resolution = item.resolution,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                )
            }
            Column(Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                Text(
                    text = item.cleanTitle.ifBlank { item.title },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val sub = buildString {
                    item.year?.let { append(it) }
                    if (item.year != null && item.seasonEpisode != null) append(" \u00B7 ")
                    item.seasonEpisode?.let { append(it) }
                }
                if (sub.isNotEmpty()) {
                    Text(
                        text = sub,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RecentlyAddedRow(
    items: List<MediaItem>,
    onClick: (MediaItem) -> Unit,
    onLongClick: (MediaItem) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items, key = { it.id }) { item ->
            RecentlyAddedCard(item = item, onClick = { onClick(item) }, onLongClick = { onLongClick(item) })
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RecentlyAddedCard(item: MediaItem, onClick: () -> Unit, onLongClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    Card(
        modifier = Modifier
            .width(180.dp)
            .pressableScale(interactionSource)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = LumenSquircle(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                SubcomposeAsyncImage(
                    model = VideoThumbnail(item.id, item.uri),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    loading = { ShimmerBox(modifier = Modifier.fillMaxSize()) }
                )
                Box(
                    Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(70.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f))
                            )
                        )
                )
                if (item.lastPositionMs > 0L && item.durationMs > 0L) {
                    val progress = (item.lastPositionMs.toFloat() / item.durationMs).coerceIn(0f, 1f)
                    Box(
                        Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .height(3.dp)
                            .background(Color.Black.copy(alpha = 0.35f))
                    ) {
                        Box(
                            Modifier
                                .fillMaxWidth(progress)
                                .height(3.dp)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                }
                ResolutionBadge(
                    resolution = item.resolution,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                )
            }
            Column(Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                Text(
                    text = item.cleanTitle.ifBlank { item.title },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val sub = buildString {
                    item.year?.let { append(it) }
                    if (item.year != null && item.seasonEpisode != null) append(" \u00B7 ")
                    item.seasonEpisode?.let { append(it) }
                }
                if (sub.isNotEmpty()) {
                    Text(
                        text = sub,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        TimeFormatter.format(item.durationMs),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeFolderRow(
    folders: List<Folder>,
    videosByFolder: Map<String, List<MediaItem>>,
    onClick: (Folder) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(folders, key = { it.path }) { folder ->
            HomeFolderCard(
                folder = folder,
                videos = videosByFolder[folder.path].orEmpty(),
                onClick = { onClick(folder) }
            )
        }
    }
}

@Composable
private fun HomeFolderCard(folder: Folder, videos: List<MediaItem>, onClick: () -> Unit) {
    val thumbType = remember(folder.path, videos) {
        FolderThumbnailResolver.resolve(folder, videos)
    }
    val model = remember(thumbType) {
        when (thumbType) {
            is FolderThumbnailType.TmdbPoster -> thumbType.url
            is FolderThumbnailType.Mosaic -> MosaicFetcherInput(
                folderPath = folder.path,
                videoIds = thumbType.videoIds,
                uris = thumbType.thumbnailUris
            )
            is FolderThumbnailType.Single -> VideoThumbnail(
                id = folder.firstVideoId ?: 0L,
                uri = thumbType.videoUri
            )
            is FolderThumbnailType.Placeholder -> folder.firstVideoUri?.let {
                VideoThumbnail(id = folder.firstVideoId ?: 0L, uri = it)
            }
        }
    }
    val interactionSource = remember { MutableInteractionSource() }
    Card(
        modifier = Modifier
            .width(140.dp)
            .pressableScale(interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = LumenSquircle(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                FolderMosaicThumbnail(
                    model = model,
                    folderName = folder.displayName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp),
                    shape = RoundedCornerShape(percent = 50),
                    color = Color.Black.copy(alpha = 0.6f)
                ) {
                    Text(
                        "${folder.mediaCount} videos",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
            Column(Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                Text(
                    folder.displayName,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun RecentAudioColumn(
    items: List<MediaItem>,
    onClick: (MediaItem) -> Unit
) {
    Column(
        Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items.take(5).forEach { item ->
            RecentAudioRow(item = item, onClick = { onClick(item) })
        }
    }
}

@Composable
private fun RecentAudioRow(item: MediaItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = LumenSquircle(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(48.dp)
                    .clip(LumenSquircle(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (item.albumArtUri != null) {
                    SubcomposeAsyncImage(
                        model = item.albumArtUri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        loading = { ShimmerBox(modifier = Modifier.fillMaxSize()) }
                    )
                } else {
                    Icon(
                        Icons.Rounded.MusicNote, contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    item.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (item.artist != null) {
                    Text(
                        item.artist!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Text(
                TimeFormatter.format(item.durationMs),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun HomeEmptyState(refreshing: Boolean, onRefresh: () -> Unit) {
    LumenEmptyState(
        blobColor = LumenAmber,
        blobMood = Mood.CURIOUS,
        title = "Welcome to Lumen",
        subtitle = "Add media to your device and tap refresh",
        accents = { HomeAccents(LumenAmber) },
        actions = {
            if (refreshing) {
                ShimmerBox(
                    modifier = Modifier.size(32.dp),
                    shape = LumenSquircle(8.dp)
                )
            } else {
                androidx.compose.material3.FilledTonalButton(onClick = onRefresh) {
                    Icon(Icons.Rounded.Search, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Refresh")
                }
            }
        }
    )
}


