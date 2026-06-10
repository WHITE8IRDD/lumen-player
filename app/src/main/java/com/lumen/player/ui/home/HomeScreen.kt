package com.lumen.player.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil.compose.SubcomposeAsyncImage
import com.lumen.player.R
import com.lumen.player.core.theme.LumenOnSurfaceDim
import com.lumen.player.core.util.TimeFormatter
import com.lumen.player.domain.model.Folder
import com.lumen.player.domain.model.MediaItem
import com.lumen.player.player.PlaybackState
import com.lumen.player.ui.components.DialogVariant
import com.lumen.player.ui.components.GlassNavigationBar
import com.lumen.player.ui.components.LumenConfirmationDialog
import com.lumen.player.ui.components.MiniPlayer
import com.lumen.player.ui.components.ShimmerBox
import com.lumen.player.ui.navigation.Destinations
import com.lumen.player.ui.util.VideoThumbnail

private data class Tab(
    val route: String,
    val labelRes: Int,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

private val TABS = listOf(
    Tab(Destinations.HOME, R.string.tab_home, Icons.Rounded.Home),
    Tab(Destinations.LIBRARY, R.string.tab_library, Icons.Rounded.LibraryMusic),
    Tab(Destinations.SETTINGS, R.string.tab_settings, Icons.Rounded.Settings)
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
            Column {
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
                GlassNavigationBar {
                    TABS.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = {
                                if (currentRoute != tab.route) {
                                    rootNav.navigate(tab.route) {
                                        popUpTo(rootNav.graph.startDestinationId) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = stringResource(tab.labelRes)) },
                            label = { Text(stringResource(tab.labelRes)) }
                        )
                    }
                }
            }
        },
        content = content
    )
}

@OptIn(ExperimentalFoundationApi::class)
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
                        message = "Hidden from Home",
                        actionLabel = "Undo"
                    )
                    if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                        viewModel.unhideFromHome(item)
                    }
                }
            },
            title = "Hide from Home?",
            body = "${item.cleanTitle.ifEmpty { item.title }} will be hidden from Home carousels. It will still be available in Library.",
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
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 16.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
        ) {
            item {
                SearchBar(onClick = onSearchClick)
            }

            if (!allEmpty) {
                if (cw.isNotEmpty()) {
                    item { SectionHeader("Continue Watching", onSeeAll = { onSeeAll("continue_watching") }) }
                    item {
                        ContinueWatchingRow(
                            items = cw,
                            onClick = onItemClick,
                            onLongClick = { contextMenuItem = it }
                        )
                    }
                }

                if (recent.isNotEmpty()) {
                    item { SectionHeader("Recently Added", onSeeAll = { onSeeAll("recently_added") }) }
                    item {
                        RecentlyAddedRow(
                            items = recent,
                            onClick = onItemClick,
                            onLongClick = { contextMenuItem = it }
                        )
                    }
                }

                if (folders.isNotEmpty()) {
                    item { SectionHeader("Your Folders", onSeeAll = { onSeeAll("folders") }) }
                    item {
                        HomeFolderRow(
                            folders = folders,
                            onClick = onFolderClick
                        )
                    }
                }

                if (audio.isNotEmpty()) {
                    item { SectionHeader("Recent Audio", onSeeAll = { onSeeAll("recent_audio") }) }
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

            item { Spacer(Modifier.height(16.dp)) }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
            snackbar = { data ->
                Snackbar(
                    snackbarData = data,
                    shape = RoundedCornerShape(12.dp),
                    containerColor = MaterialTheme.colorScheme.surface
                )
            }
        )

        contextMenuItem?.let { item ->
            DropdownMenu(
                expanded = true,
                onDismissRequest = { contextMenuItem = null }
            ) {
                DropdownMenuItem(
                    text = { Text("Play") },
                    onClick = {
                        contextMenuItem = null
                        onItemClick(item)
                    },
                    leadingIcon = { Icon(Icons.Rounded.PlayArrow, contentDescription = null) }
                )
                DropdownMenuItem(
                    text = { Text("Hide from this list") },
                    onClick = {
                        contextMenuItem = null
                        viewModel.hideFromContinueWatching(item)
                    },
                    leadingIcon = { Icon(Icons.Rounded.VisibilityOff, contentDescription = null) }
                )
                DropdownMenuItem(
                    text = { Text("Hide from Home") },
                    onClick = {
                        contextMenuItem = null
                        showHideFromHomeDialog = item
                    },
                    leadingIcon = { Icon(Icons.Rounded.VisibilityOff, contentDescription = null) }
                )
                DropdownMenuItem(
                    text = { Text("Add to playlist") },
                    onClick = { contextMenuItem = null },
                    leadingIcon = { Icon(Icons.Rounded.Add, contentDescription = null) }
                )
                DropdownMenuItem(
                    text = { Text("Share") },
                    onClick = { contextMenuItem = null },
                    leadingIcon = { Icon(Icons.Rounded.Share, contentDescription = null) }
                )
            }
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
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
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
private fun SectionHeader(title: String, onSeeAll: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 8.dp, top = 20.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        TextButton(onClick = onSeeAll) {
            Text("See all", style = MaterialTheme.typography.labelLarge)
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
    Card(
        modifier = Modifier
            .width(200.dp)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
            Column(Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                Text(
                    item.cleanTitle.ifEmpty { item.title },
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (item.year != null || item.seasonEpisode != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (item.year != null) {
                            Text(
                                item.year.toString(),
                                style = MaterialTheme.typography.bodySmall,
                                color = LumenOnSurfaceDim
                            )
                        }
                        if (item.seasonEpisode != null) {
                            if (item.year != null) Text(" \u00B7 ", color = LumenOnSurfaceDim)
                            Text(
                                item.seasonEpisode!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = LumenOnSurfaceDim
                            )
                        }
                    }
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
    Card(
        modifier = Modifier
            .width(180.dp)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
            }
            Column(Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                Text(
                    item.cleanTitle.ifEmpty { item.title },
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    TimeFormatter.format(item.durationMs),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun HomeFolderRow(
    folders: List<Folder>,
    onClick: (Folder) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(folders, key = { it.path }) { folder ->
            HomeFolderCard(folder = folder, onClick = { onClick(folder) })
        }
    }
}

@Composable
private fun HomeFolderCard(folder: Folder, onClick: () -> Unit) {
    val thumbUri = folder.firstVideoUri
    Card(
        modifier = Modifier
            .width(140.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (thumbUri != null) {
                    SubcomposeAsyncImage(
                        model = com.lumen.player.ui.util.FolderThumbnail(
                            folderPath = folder.path,
                            firstVideoUri = thumbUri
                        ),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        loading = { ShimmerBox(modifier = Modifier.fillMaxSize()) }
                    )
                } else {
                    Icon(
                        Icons.Rounded.Folder, contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Box(
                    Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black.copy(alpha = 0.72f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Rounded.Movie, contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "${folder.mediaCount}",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
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
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
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
    Box(
        Modifier
            .fillMaxWidth()
            .padding(top = 80.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Rounded.Home,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "Welcome to Lumen",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Add media to your device and tap refresh",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
            Spacer(Modifier.height(24.dp))
            if (refreshing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                androidx.compose.material3.FilledTonalButton(onClick = onRefresh) {
                    Icon(Icons.Rounded.Search, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Refresh")
                }
            }
        }
    }
}
