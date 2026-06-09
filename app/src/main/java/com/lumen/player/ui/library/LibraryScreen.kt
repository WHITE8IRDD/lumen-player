package com.lumen.player.ui.library

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TextButton
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import coil.compose.AsyncImage
import com.lumen.player.R
import com.lumen.player.domain.model.MediaItem
import com.lumen.player.domain.model.MediaType
import com.lumen.player.ui.components.MediaListItem
import com.lumen.player.ui.folders.FoldersViewModel
import com.lumen.player.ui.playlist.PlaylistViewModel
import android.content.Intent
import android.provider.DocumentsContract
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.SnackbarResult
import com.lumen.player.core.util.TimeFormatter
import com.lumen.player.ui.components.LumenInfoDialog
import com.lumen.player.ui.components.LumenInputDialog
import java.util.Locale
import kotlinx.coroutines.launch

private val tabTitles = listOf("Videos", "Audio", "Folders", "Playlists")

@UnstableApi
@Composable
fun LibraryScreen(
    hasPermission: Boolean,
    onItemClick: (MediaItem) -> Unit,
    onFolderClick: (String) -> Unit,
    onPlaylistClick: (Long) -> Unit = {},
    isGridView: Boolean,
    onToggleViewMode: () -> Unit,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val pagerState = rememberPagerState(pageCount = { tabTitles.size })
    val scope = rememberCoroutineScope()

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(R.string.library),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        ScrollableTabRow(
            selectedTabIndex = pagerState.currentPage,
            edgePadding = 16.dp,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface,
            divider = {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(0.5.dp)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                )
            },
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                    height = 3.dp,
                    color = MaterialTheme.colorScheme.tertiary
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    text = {
                        Text(
                            title,
                            fontWeight = if (pagerState.currentPage == index)
                                FontWeight.SemiBold else FontWeight.Medium,
                            fontSize = MaterialTheme.typography.titleSmall.fontSize,
                            color = if (pagerState.currentPage == index)
                                MaterialTheme.colorScheme.onSurface
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    selectedContentColor = Color.Transparent,
                    unselectedContentColor = Color.Transparent
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) { page ->
            when (page) {
                0 -> VideosTabContent(
                    hasPermission = hasPermission,
                    onVideoClick = onItemClick,
                    onFolderClick = onFolderClick,
                    isGridView = isGridView,
                    onToggleViewMode = onToggleViewMode,
                    viewModel = viewModel
                )
                1 -> AudioTabContent(
                    hasPermission = hasPermission,
                    onItemClick = onItemClick,
                    viewModel = viewModel
                )
                2 -> FoldersTabContent(onFolderClick = onFolderClick)
                3 -> PlaylistsTabContent(onPlaylistClick = onPlaylistClick)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@UnstableApi
@Composable
private fun VideosTabContent(
    hasPermission: Boolean,
    onVideoClick: (MediaItem) -> Unit,
    onFolderClick: (String) -> Unit,
    isGridView: Boolean,
    onToggleViewMode: () -> Unit,
    viewModel: LibraryViewModel
) {
    val items by viewModel.videoItems.collectAsStateWithLifecycle()
    val folders by viewModel.videoFolders.collectAsStateWithLifecycle()
    val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()
    var selectedIds by remember { mutableStateOf(setOf<Long>()) }
    var selectionMode by remember { mutableStateOf(false) }
    var sort by remember { mutableStateOf(VideoSort.DATE) }
    var overflowExpanded by remember { mutableStateOf(false) }
    var showSortSubmenu by remember { mutableStateOf(false) }
    var showHiddenItems by remember { mutableStateOf(false) }
    var isGroupedByFolder by remember { mutableStateOf(true) }
    var showPropertiesDialog by remember { mutableStateOf<MediaItem?>(null) }
    val context = LocalContext.current

    val displayItems = remember(items, sort) {
        when (sort) {
            VideoSort.TITLE -> items.sortedBy { it.title.lowercase(Locale.getDefault()) }
            VideoSort.DATE -> items.sortedByDescending { it.dateAdded }
            VideoSort.DURATION -> items.sortedByDescending { it.durationMs }
            VideoSort.SIZE -> items.sortedByDescending { it.sizeBytes }
            VideoSort.RESOLUTION -> items.sortedByDescending { it.sizeBytes }
        }
    }
    val selectedItems = remember(displayItems, selectedIds) {
        displayItems.filter { it.id in selectedIds }
    }

    val inSelectionMode = selectedIds.isNotEmpty() || selectionMode

    val activity = context as? androidx.activity.ComponentActivity
    val deleteLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { _ -> selectedIds = emptySet(); selectionMode = false }

    var showPlaylistSheet by remember { mutableStateOf(false) }
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }
    val playlistVm: PlaylistViewModel = hiltViewModel()
    val allPlaylists by playlistVm.playlists.collectAsStateWithLifecycle()
    val sheetScope = rememberCoroutineScope()
    val snackbarState = remember { SnackbarHostState() }

    if (items.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (isScanning) {
                Column(horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    CircularProgressIndicator(modifier = Modifier.size(40.dp))
                    Text("Scanning your videos\u2026",
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Rounded.Movie, null, Modifier.size(56.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("No videos on this device",
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    FilledTonalButton(onClick = viewModel::rescan, shape = RoundedCornerShape(12.dp)) {
                        Icon(Icons.Rounded.Refresh, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Scan again")
                    }
                }
            }
        }
        return
    }

    Column(Modifier.fillMaxSize()) {
        SelectionActionBar(
            selectedCount = selectedIds.size,
            selectionMode = selectionMode,
            onClear = { selectedIds = emptySet(); selectionMode = false },
            onPlayNext = {
                viewModel.playNext(selectedItems)
                selectedIds = emptySet()
                selectionMode = false
            },
            onAddToQueue = {
                viewModel.addToQueue(selectedItems)
                selectedIds = emptySet()
                selectionMode = false
            },
            onShare = {
                viewModel.share(context, selectedItems)
                selectedIds = emptySet()
                selectionMode = false
            },
            onDelete = {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                    val intentSender = viewModel.createDeleteSender(context, selectedItems)
                    intentSender?.let { deleteLauncher.launch(it) } ?: run { selectedIds = emptySet(); selectionMode = false }
                } else {
                    viewModel.requestDelete(context, selectedItems)
                    selectedIds = emptySet()
                    selectionMode = false
                }
            },
            onAddToPlaylist = { showPlaylistSheet = true }
        )

        if (!inSelectionMode) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${items.size} videos",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Box {
                    IconButton(onClick = { overflowExpanded = true }) {
                        Icon(Icons.Rounded.MoreVert, contentDescription = "More")
                    }
                    DropdownMenu(
                        expanded = overflowExpanded,
                        onDismissRequest = { overflowExpanded = false; showSortSubmenu = false }
                    ) {
                        if (showSortSubmenu) {
                            VideoSort.values().forEach { s ->
                                DropdownMenuItem(
                                    text = { Text(s.label()) },
                                    onClick = {
                                        sort = s
                                        overflowExpanded = false
                                        showSortSubmenu = false
                                    }
                                )
                            }
                        } else {
                            DropdownMenuItem(
                                text = { Text("Sort by") },
                                onClick = { showSortSubmenu = true }
                            )
                            DropdownMenuItem(
                                text = { Text(if (isGroupedByFolder) "Ungroup folders" else "Group by folder") },
                                onClick = {
                                    isGroupedByFolder = !isGroupedByFolder
                                    overflowExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(if (isGridView) "List view" else "Grid view") },
                                onClick = {
                                    onToggleViewMode()
                                    overflowExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Select multiple") },
                                onClick = { selectionMode = true; overflowExpanded = false }
                            )
                            DropdownMenuItem(
                                text = { Text(if (showHiddenItems) "Hide hidden items" else "Show hidden items") },
                                onClick = { showHiddenItems = !showHiddenItems; overflowExpanded = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Rescan library") },
                                onClick = { viewModel.rescan(); overflowExpanded = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Settings") },
                                onClick = { overflowExpanded = false }
                            )
                        }
                    }
                }
                SingleChoiceSegmentedButtonRow {
                    SegmentedButton(
                        selected = isGridView,
                        onClick = { if (!isGridView) onToggleViewMode() },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                    ) {
                        Text("Grid", maxLines = 1)
                    }
                    SegmentedButton(
                        selected = !isGridView,
                        onClick = { if (isGridView) onToggleViewMode() },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                    ) {
                        Text("List", maxLines = 1)
                    }
                }
            }
        }

        if (isGridView && isGroupedByFolder) {
            FolderGrid(
                folders = folders,
                onFolderClick = { onFolderClick(it.path) },
                onSingleVideoClick = { item ->
                    if (inSelectionMode) {
                        selectionMode = true
                        selectedIds = if (item.id in selectedIds) selectedIds - item.id else selectedIds + item.id
                    } else {
                        android.util.Log.d("CLICK", "title=${item.title} isVideo=${item.isVideo} mimeType=${item.mimeType} uri=${item.uri}")
                        val idx = displayItems.indexOf(item).coerceAtLeast(0)
                        viewModel.playAtIndex(displayItems, idx)
                        onVideoClick(item)
                    }
                },
                onVideoLongClick = { item ->
                    selectedIds = selectedIds + item.id
                },
                onAddToQueue = { item ->
                    viewModel.addToQueue(listOf(item))
                },
                onAddToPlaylist = { item ->
                    selectedIds = setOf(item.id)
                    showPlaylistSheet = true
                },
                onShare = { item ->
                    viewModel.share(context, listOf(item))
                },
                onDelete = { item ->
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                        val intentSender = viewModel.createDeleteSender(context, listOf(item))
                        intentSender?.let { deleteLauncher.launch(it) }
                    } else {
                        viewModel.requestDelete(context, listOf(item))
                    }
                },
                onPlayNext = { item ->
                    viewModel.addToQueue(listOf(item))
                },
                onOpenFileLocation = { item ->
                    try {
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.setDataAndType(
                            DocumentsContract.buildDocumentUri(
                                "com.android.externalstorage.documents",
                                "primary:" + item.folderPath.removePrefix("/storage/emulated/0/")
                            ),
                            "application/zip"
                        )
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    } catch (_: Exception) { }
                },
                onProperties = { item ->
                    showPropertiesDialog = item
                },
                onHideFromHome = { item ->
                    viewModel.hideFromHome(item)
                    sheetScope.launch {
                        val result = snackbarState.showSnackbar(
                            message = "Hidden from Home",
                            actionLabel = "Undo"
                        )
                        if (result == SnackbarResult.ActionPerformed) {
                            viewModel.unhideFromHome(item)
                        }
                    }
                },
                selectedIds = selectedIds
            )
        } else if (isGridView) {
            VideoGrid(
                items = displayItems,
                onClick = { item ->
                    if (inSelectionMode) {
                        selectionMode = true
                        selectedIds = if (item.id in selectedIds) selectedIds - item.id else selectedIds + item.id
                    } else {
                        android.util.Log.d("CLICK", "title=${item.title} isVideo=${item.isVideo} mimeType=${item.mimeType} uri=${item.uri}")
                        val idx = displayItems.indexOf(item).coerceAtLeast(0)
                        viewModel.playAtIndex(displayItems, idx)
                        onVideoClick(item)
                    }
                },
                onLongClick = { item ->
                    selectedIds = selectedIds + item.id
                },
                onAddToQueue = { item ->
                    viewModel.addToQueue(listOf(item))
                },
                onAddToPlaylist = { item ->
                    selectedIds = setOf(item.id)
                    showPlaylistSheet = true
                },
                onShare = { item ->
                    viewModel.share(context, listOf(item))
                },
                onDelete = { item ->
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                        val intentSender = viewModel.createDeleteSender(context, listOf(item))
                        intentSender?.let { deleteLauncher.launch(it) }
                    } else {
                        viewModel.requestDelete(context, listOf(item))
                    }
                },
                onPlayNext = { item ->
                    viewModel.addToQueue(listOf(item))
                },
                onOpenFileLocation = { item ->
                    try {
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.setDataAndType(
                            DocumentsContract.buildDocumentUri(
                                "com.android.externalstorage.documents",
                                "primary:" + item.folderPath.removePrefix("/storage/emulated/0/")
                            ),
                            "application/zip"
                        )
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    } catch (_: Exception) { }
                },
                onProperties = { item ->
                    showPropertiesDialog = item
                },
                onHideFromHome = { item ->
                    viewModel.hideFromHome(item)
                    sheetScope.launch {
                        val result = snackbarState.showSnackbar(
                            message = "Hidden from Home",
                            actionLabel = "Undo"
                        )
                        if (result == SnackbarResult.ActionPerformed) {
                            viewModel.unhideFromHome(item)
                        }
                    }
                },
                selectedIds = selectedIds
            )
        } else {
            VideoList(
                items = displayItems,
                onClick = { item ->
                    if (inSelectionMode) {
                        selectionMode = true
                        selectedIds = if (item.id in selectedIds) selectedIds - item.id else selectedIds + item.id
                    } else {
                        android.util.Log.d("CLICK", "title=${item.title} isVideo=${item.isVideo} mimeType=${item.mimeType} uri=${item.uri}")
                        val idx = displayItems.indexOf(item).coerceAtLeast(0)
                        viewModel.playAtIndex(displayItems, idx)
                        onVideoClick(item)
                    }
                },
                onLongClick = { item ->
                    selectedIds = selectedIds + item.id
                },
                onAddToQueue = { item ->
                    viewModel.addToQueue(listOf(item))
                },
                onAddToPlaylist = { item ->
                    selectedIds = setOf(item.id)
                    showPlaylistSheet = true
                },
                onShare = { item ->
                    viewModel.share(context, listOf(item))
                },
                onDelete = { item ->
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                        val intentSender = viewModel.createDeleteSender(context, listOf(item))
                        intentSender?.let { deleteLauncher.launch(it) }
                    } else {
                        viewModel.requestDelete(context, listOf(item))
                    }
                },
                onPlayNext = { item ->
                    viewModel.addToQueue(listOf(item))
                },
                onOpenFileLocation = { item ->
                    try {
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.setDataAndType(
                            DocumentsContract.buildDocumentUri(
                                "com.android.externalstorage.documents",
                                "primary:" + item.folderPath.removePrefix("/storage/emulated/0/")
                            ),
                            "application/zip"
                        )
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    } catch (_: Exception) { }
                },
                onProperties = { item ->
                    showPropertiesDialog = item
                },
                onHideFromHome = { item ->
                    viewModel.hideFromHome(item)
                    sheetScope.launch {
                        val result = snackbarState.showSnackbar(
                            message = "Hidden from Home",
                            actionLabel = "Undo"
                        )
                        if (result == SnackbarResult.ActionPerformed) {
                            viewModel.unhideFromHome(item)
                        }
                    }
                },
                selectedIds = selectedIds
            )
        }
        SnackbarHost(
            hostState = snackbarState
        )
    }

    if (showPlaylistSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPlaylistSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
        ) {
            Column(Modifier.padding(bottom = 32.dp)) {
                Text(
                    "Add to playlist",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                )
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable {
                            showPlaylistSheet = false
                            showCreatePlaylistDialog = true
                        }
                        .padding(horizontal = 24.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.Add, null, Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.tertiary)
                    Spacer(Modifier.width(12.dp))
                    Text("+ Create new playlist",
                        color = MaterialTheme.colorScheme.tertiary,
                        style = MaterialTheme.typography.bodyLarge)
                }
                allPlaylists.forEach { playlist ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable {
                                val ids = selectedIds.toList()
                                val name = playlist.name
                                viewModel.addToPlaylist(playlist.id, ids)
                                selectedIds = emptySet()
                                selectionMode = false
                                showPlaylistSheet = false
                                sheetScope.launch {
                                    val result = snackbarState.showSnackbar(
                                        message = "Added ${ids.size} items to \"$name\"",
                                        actionLabel = "Undo"
                                    )
                                    if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                                        viewModel.removeFromPlaylist(playlist.id, ids)
                                    }
                                }
                            }
                            .padding(horizontal = 24.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.QueueMusic, null, Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(playlist.name, style = MaterialTheme.typography.bodyLarge,
                                maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("${playlist.itemCount} items",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }

    if (showCreatePlaylistDialog) {
        LumenInputDialog(
            onDismiss = {
                newPlaylistName = ""
                showCreatePlaylistDialog = false
            },
            onConfirm = { name ->
                val ids = selectedIds.toList()
                playlistVm.createPlaylist(name) { newId ->
                    viewModel.addToPlaylist(newId, ids)
                    selectedIds = emptySet()
                    selectionMode = false
                    sheetScope.launch {
                        val result = snackbarState.showSnackbar(
                            message = "Added ${ids.size} items to \"$name\"",
                            actionLabel = "Undo"
                        )
                        if (result == SnackbarResult.ActionPerformed) {
                            viewModel.removeFromPlaylist(newId, ids)
                        }
                    }
                }
                newPlaylistName = ""
                showCreatePlaylistDialog = false
            },
            title = "New playlist",
            label = "Playlist name",
            confirmLabel = "Create"
        )
    }

    if (showPropertiesDialog != null) {
        val item = showPropertiesDialog!!
        LumenInfoDialog(
            onDismiss = { showPropertiesDialog = null },
            title = item.cleanTitle.ifEmpty { item.title }
        ) {
            PropertiesRow("Path", item.folderPath)
            PropertiesRow("Size", humanSize(item.sizeBytes))
            PropertiesRow("Duration", TimeFormatter.format(item.durationMs))
            if (item.year != null) PropertiesRow("Year", item.year.toString())
            if (item.seasonEpisode != null) PropertiesRow("Season/Episode", item.seasonEpisode!!)
            PropertiesRow("Type", item.mimeType)
            if (item.posterUrl != null) PropertiesRow("Poster", "TMDB")
        }
    }
}

@Composable
private fun PropertiesRow(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

@UnstableApi
@Composable
private fun AudioTabContent(
    hasPermission: Boolean,
    onItemClick: (MediaItem) -> Unit,
    viewModel: LibraryViewModel
) {
    val items by viewModel.audioItems.collectAsStateWithLifecycle()
    val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val showFab by remember { derivedStateOf { listState.firstVisibleItemIndex > 2 } }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalButton(
                    onClick = { viewModel.play(items, 0) },
                    enabled = items.isNotEmpty()
                ) {
                    Icon(Icons.Rounded.GraphicEq, null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.play_all))
                }
                Spacer(Modifier.width(8.dp))
                FilledTonalButton(
                    onClick = viewModel::shuffleAll,
                    enabled = items.isNotEmpty()
                ) {
                    Icon(Icons.Rounded.Shuffle, null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.shuffle_all))
                }
                Spacer(Modifier.weight(1f))
                Text(
                    stringResource(R.string.songs_count, items.size),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(8.dp))

            when {
                hasPermission && isScanning && items.isEmpty() -> ScanningPlaceholder()
                hasPermission && items.isEmpty() -> EmptyLibraryPlaceholder(onRescan = viewModel::rescan)
                else -> LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                    items(items, key = { it.id }) { item ->
                        MediaListItem(
                            item = item,
                            onClick = {
                                viewModel.play(items, items.indexOf(item))
                                onItemClick(item)
                            }
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = showFab && items.isNotEmpty(),
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 16.dp)
        ) {
            SmallFloatingActionButton(
                onClick = { scope.launch { listState.animateScrollToItem(0) } },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
            ) {
                Icon(Icons.Rounded.KeyboardArrowUp, contentDescription = "Scroll to top")
            }
        }
    }
}

@Composable
private fun FoldersTabContent(
    onFolderClick: (String) -> Unit,
    viewModel: FoldersViewModel = hiltViewModel()
) {
    val folders by viewModel.folders.collectAsStateWithLifecycle()

    LazyColumn(Modifier.fillMaxSize()) {
        items(folders, key = { it.path }) { folder ->
            Row(
                Modifier.fillMaxWidth()
                    .clickable { onFolderClick(folder.path) }
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (folder.firstItemArtUri != null) {
                    AsyncImage(
                        model = folder.firstItemArtUri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp))
                    )
                } else {
                    Icon(Icons.Rounded.Folder, null, Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(folder.displayName, style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(folder.path, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Text("${folder.mediaCount}", style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlaylistsTabContent(
    onPlaylistClick: (Long) -> Unit = {},
    viewModel: PlaylistViewModel = hiltViewModel()
) {
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()
    var showCreateDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }

    Box(Modifier.fillMaxSize()) {
        if (playlists.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Rounded.QueueMusic, null, Modifier.size(56.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("No playlists yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    FilledTonalButton(
                        onClick = { showCreateDialog = true },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Rounded.Add, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Create your first playlist")
                    }
                }
            }
        } else {
            LazyColumn(Modifier.fillMaxSize().padding(bottom = 80.dp)) {
                items(playlists, key = { it.id }) { playlist ->
                    Row(
                        Modifier.fillMaxWidth()
                            .clickable { onPlaylistClick(playlist.id) }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.QueueMusic, null, Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(playlist.name, style = MaterialTheme.typography.bodyLarge,
                                maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("${playlist.itemCount} tracks",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            SmallFloatingActionButton(
                onClick = { showCreateDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 16.dp),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Create playlist")
            }
        }
    }

    if (showCreateDialog) {
        LumenInputDialog(
            onDismiss = {
                newPlaylistName = ""
                showCreateDialog = false
            },
            onConfirm = { name ->
                viewModel.createPlaylist(name)
                newPlaylistName = ""
                showCreateDialog = false
            },
            title = "New playlist",
            label = "Playlist name",
            confirmLabel = "Create"
        )
    }
}

@Composable
private fun ScanningPlaceholder() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            CircularProgressIndicator(Modifier.size(40.dp))
            Text("Scanning your media\u2026", style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun EmptyLibraryPlaceholder(onRescan: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Rounded.MusicNote, null, Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("No audio on this device", style = MaterialTheme.typography.bodyLarge)
            FilledTonalButton(onClick = onRescan, shape = RoundedCornerShape(12.dp)) {
                Icon(Icons.Rounded.Refresh, null, Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Scan again")
            }
        }
    }
}
