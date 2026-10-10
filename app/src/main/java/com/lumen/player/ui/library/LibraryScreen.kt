package com.lumen.player.ui.library

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import com.lumen.player.ui.components.LumenSquircle
import com.lumen.player.ui.util.pressableScale
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.ViewList
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults

import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.unit.sp
import android.net.Uri
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.lumen.player.R
import com.lumen.player.domain.model.MediaItem
import com.lumen.player.domain.model.MediaType
import com.lumen.player.ui.components.AudioRowSkeleton
import com.lumen.player.ui.components.CategoryChip
import com.lumen.player.ui.components.FolderCardSkeleton
import com.lumen.player.ui.components.MediaListItem
import com.lumen.player.ui.components.SectionLabel
import com.lumen.player.ui.components.ShimmerBox
import com.lumen.player.ui.components.AudioAccents
import com.lumen.player.ui.components.FolderAccents
import com.lumen.player.ui.components.PlaylistAccents
import com.lumen.player.ui.components.StreamAccents
import com.lumen.player.ui.components.VideoAccents
import com.lumen.player.ui.components.VideoCardSkeleton
import com.lumen.player.ui.folders.FoldersViewModel
import com.lumen.player.ui.playlist.PlaylistViewModel
import android.content.Intent
import android.provider.DocumentsContract
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import com.lumen.player.core.util.TimeFormatter
import com.lumen.player.core.util.formatFileSize
import com.lumen.player.ui.components.LumenInfoDialog
import com.lumen.player.ui.components.LumenInputDialog
import java.util.Locale
import kotlinx.coroutines.launch
import com.lumen.player.ui.util.VideoThumbnail
import com.lumen.player.data.metadata.MediaTitleParser
import com.lumen.player.data.metadata.friendlyPath
import com.lumen.player.core.theme.LumenAmber
import com.lumen.player.core.theme.LumenLavender
import com.lumen.player.core.theme.LumenOcean
import com.lumen.player.core.theme.LumenSage
import com.lumen.player.ui.components.ColoredFAB
import com.lumen.player.ui.components.GradientBlob
import com.lumen.player.ui.components.LumenBlob
import com.lumen.player.ui.components.LumenEmptyState
import com.lumen.player.ui.components.Mood
import androidx.compose.foundation.layout.offset

private val tabTitles = listOf("videos", "audio", "folders", "playlists", "streams")
private val tabIcons = listOf(
    Icons.Rounded.Movie,
    Icons.Rounded.MusicNote,
    Icons.Rounded.Folder,
    Icons.Rounded.QueueMusic,
    Icons.Rounded.Language
)

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
    val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()

    LaunchedEffect(hasPermission) {
        if (hasPermission) viewModel.onPermissionGranted()
    }

    Box(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        GradientBlob(
            color = MaterialTheme.colorScheme.secondary,
            size = 250.dp,
            alpha = 0.2f,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 100.dp, y = (-100).dp)
        )
        Column(
            Modifier.fillMaxSize()
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.library),
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

        ScrollableTabRow(
            selectedTabIndex = pagerState.currentPage,
            edgePadding = 0.dp,
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
                    height = 2.dp,
                    color = MaterialTheme.colorScheme.secondary
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            tabTitles.forEachIndexed { index, title ->
                val selected = pagerState.currentPage == index
                Tab(
                    selected = selected,
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = tabIcons[index],
                            contentDescription = title,
                            tint = if (selected) MaterialTheme.colorScheme.secondary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    text = {
                        Text(
                            title,
                            fontWeight = if (selected)
                                FontWeight.SemiBold else FontWeight.Medium,
                            fontSize = MaterialTheme.typography.titleSmall.fontSize,
                            color = if (selected)
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
                2 -> FoldersTabContent(onFolderClick = onFolderClick, isScanning = isScanning)
                 3 -> PlaylistsTabContent(onPlaylistClick = onPlaylistClick)
                 4 -> StreamsTabContent(onStreamPlay = onItemClick)
            }
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
    val videosByFolder = remember(items) { items.groupBy { it.folderPath } }
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

    val inSelectionMode by remember { derivedStateOf { selectedIds.isNotEmpty() || selectionMode } }

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

    if (isScanning && items.isEmpty()) {
        VideoSkeletonGrid()
        return
    }

    if (items.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            LumenEmptyState(
                blobColor = MaterialTheme.colorScheme.secondary,
                blobMood = Mood.IDLE,
                title = "No videos yet",
                subtitle = "Add video files to your device to begin",
                accents = { VideoAccents(MaterialTheme.colorScheme.secondary) }
            )
        }
        return
    }

    Column(Modifier.fillMaxSize()) {
        val allSelected = displayItems.isNotEmpty() && displayItems.all { it.id in selectedIds }
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
                if (selectedItems.isNotEmpty()) {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                        val intentSender = viewModel.createDeleteSender(context, selectedItems)
                        intentSender?.let { deleteLauncher.launch(it) } ?: run { selectedIds = emptySet(); selectionMode = false }
                    } else {
                        viewModel.requestDelete(context, selectedItems)
                        selectedIds = emptySet()
                        selectionMode = false
                    }
                }
            },
            onAddToPlaylist = { showPlaylistSheet = true },
            onSelectAll = {
                // Toggle: select every video in the library, or clear the selection.
                selectedIds = if (allSelected) emptySet() else displayItems.map { it.id }.toSet()
                selectionMode = true
            },
            allSelected = allSelected
        )

        if (!inSelectionMode) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = buildString {
                        append("${items.size} videos")
                        val totalBytes = items.sumOf { it.sizeBytes }
                        if (totalBytes > 0L) {
                            append(" • ${formatFileSize(totalBytes)}")
                        }
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Box {
                    IconButton(onClick = { overflowExpanded = true }) {
                        Icon(Icons.Rounded.MoreVert, contentDescription = "more")
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
                        Text("grid", maxLines = 1)
                    }
                    SegmentedButton(
                        selected = !isGridView,
                        onClick = { if (isGridView) onToggleViewMode() },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                    ) {
                        Text("list", maxLines = 1)
                    }
                }
            }
        }

        if (isGridView && isGroupedByFolder) {
            FolderGrid(
                folders = folders,
                videosByFolder = videosByFolder,
                onFolderClick = { onFolderClick(it.path) },
                onSingleVideoClick = { item ->
                    if (inSelectionMode) {
                        selectionMode = true
                        selectedIds = if (item.id in selectedIds) selectedIds - item.id else selectedIds + item.id
                    } else {
                        val idx = displayItems.indexOfFirst { it.id == item.id }.coerceAtLeast(0)
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
message = "hidden from home",
                        actionLabel = "undo"
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
                        val idx = displayItems.indexOfFirst { it.id == item.id }.coerceAtLeast(0)
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
            message = "hidden from home",
                        actionLabel = "undo"
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
                        val idx = displayItems.indexOfFirst { it.id == item.id }.coerceAtLeast(0)
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
            message = "hidden from home",
                        actionLabel = "undo"
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
                    "add to playlist",
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
                    Text("+ create new playlist",
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
                                        message = "added ${ids.size} items to \"$name\"",
                                        actionLabel = "undo"
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
                            message = "added ${ids.size} items to \"$name\"",
                            actionLabel = "undo"
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
            label = "playlist name",
            confirmLabel = "Create"
        )
    }

    if (showPropertiesDialog != null) {
        val item = showPropertiesDialog!!
        LumenInfoDialog(
            onDismiss = { showPropertiesDialog = null },
            title = item.cleanTitle.ifEmpty { item.title }
        ) {
            PropertiesRow("path", item.folderPath)
            PropertiesRow("size", humanSize(item.sizeBytes))
            PropertiesRow("duration", TimeFormatter.format(item.durationMs))
            if (item.year != null) PropertiesRow("year", item.year.toString())
            if (item.seasonEpisode != null) PropertiesRow("season/episode", item.seasonEpisode!!)
            PropertiesRow("type", item.mimeType)
            if (item.posterUrl != null) PropertiesRow("poster", "TMDB")
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
            if (items.isNotEmpty()) {
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
            }

            Spacer(Modifier.height(8.dp))

            when {
                hasPermission && isScanning && items.isEmpty() -> AudioSkeletonList()
                hasPermission && items.isEmpty() -> {
                    EmptyLibraryPlaceholder(
                        onRescan = viewModel::rescan,
                        modifier = Modifier.navigationBarsPadding()
                    )
                }
                else -> LazyColumn(state = listState, modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 16.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())) {
                    items(items, key = { it.id }, contentType = { "audio" }) { item ->
                        MediaListItem(
                            item = item,
                            onClick = {
                                val idx = items.indexOfFirst { it.id == item.id }.coerceAtLeast(0)
                                viewModel.play(items, idx)
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
    isScanning: Boolean = false,
    viewModel: FoldersViewModel = hiltViewModel()
) {
    val folders by viewModel.folders.collectAsStateWithLifecycle()

    if (isScanning && folders.isEmpty()) {
        FolderSkeletonGrid()
        return
    }

    if (folders.isEmpty()) {
        LumenEmptyState(
            modifier = Modifier.navigationBarsPadding(),
            blobColor = MaterialTheme.colorScheme.secondary,
            blobMood = Mood.SLEEPING,
            title = "No folders yet",
            subtitle = "Videos are grouped by their folder on your device",
            accents = { FolderAccents(MaterialTheme.colorScheme.secondary) }
        )
        return
    }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 16.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())) {
        items(folders, key = { it.path }, contentType = { "folder" }) { folder ->
            val interactionSource = remember { MutableInteractionSource() }
            Row(
                Modifier.fillMaxWidth()
                    .pressableScale(interactionSource)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = { onFolderClick(folder.path) }
                    )
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(56.dp)) {
                    val thumbnailModel = remember(folder) {
                        when {
                            folder.firstVideoPosterUrl != null -> folder.firstVideoPosterUrl
                            folder.firstVideoUri != null -> VideoThumbnail(
                                id = folder.firstVideoId ?: 0L,
                                uri = folder.firstVideoUri
                            )
                            folder.firstItemArtUri != null && folder.firstItemArtUri != "null" && folder.firstItemArtUri.isNotBlank() -> folder.firstItemArtUri
                            else -> null
                        }
                    }
                    if (thumbnailModel != null) {
                        SubcomposeAsyncImage(
                            model = thumbnailModel,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(LumenSquircle(12.dp)),
                            loading = { FolderIconFallback() },
                            error = { FolderIconFallback() }
                        )
                    } else {
                        FolderIconFallback()
                    }
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(2.dp),
                        color = Color.Black.copy(alpha = 0.78f),
                        shape = RoundedCornerShape(3.dp)
                    ) {
                        Text(
                            "${folder.mediaCount}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    val cleanName = remember { mutableStateOf(folder.displayName) }
                    LaunchedEffect(folder.displayName) {
                        val meta = MediaTitleParser().resolve(folder.displayName)
                        cleanName.value = meta.cleanTitle.ifEmpty { folder.displayName }
                    }
                    Text(cleanName.value, style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(friendlyPath(folder.path), style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

@Composable
private fun FolderIconFallback() {
    Box(
        Modifier
            .fillMaxSize()
            .clip(LumenSquircle(12.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.Folder,
            contentDescription = null,
            modifier = Modifier.size(28.dp),
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun StreamsTabContent(
    onStreamPlay: (MediaItem) -> Unit,
    viewModel: StreamsViewModel = hiltViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val groups by viewModel.groupedStreams.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val viewMode by viewModel.viewMode.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var expandedGroups by remember { mutableStateOf(setOf<String>()) }

    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val m3uPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.importM3uFile(it, ctx) }
    }

    val bottomPadding = 16.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    val categories = remember(groups) {
        groups.map { it.name }.filter { it != "\u2605 Favorites" }
    }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    val filteredGroups = remember(groups, selectedCategory) {
        if (selectedCategory == null) groups
        else groups.filter { it.name == selectedCategory || it.name == "\u2605 Favorites" }
    }

    Box(Modifier.fillMaxSize()) {
        if (groups.isEmpty()) {
                LumenEmptyState(
                    modifier = Modifier.navigationBarsPadding(),
                    blobColor = LumenOcean,
                    blobMood = Mood.SLEEPING,
                    title = "No streams yet",
                    subtitle = "Add a network stream URL or import an M3U playlist",
                    accents = { StreamAccents(LumenOcean) },
                    actions = {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(
                                onClick = { m3uPicker.launch(arrayOf(
                                    "audio/x-mpegurl", "application/vnd.apple.mpegurl",
                                    "application/x-mpegurl", "*/*"
                                )) },
                                shape = LumenSquircle(12.dp)
                            ) {
                                Icon(Icons.Rounded.Folder, null, Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Import M3U")
                            }
                            FilledTonalButton(
                                onClick = { showAddDialog = true },
                                shape = LumenSquircle(12.dp)
                            ) {
                                Icon(Icons.Rounded.Add, null, Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Add URL")
                            }
                        }
                    }
                )
        } else {
            Box(Modifier.fillMaxSize()) {
                Column(Modifier.fillMaxSize()) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 8.dp, top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = query,
                            onValueChange = viewModel::setQuery,
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Search streams...") },
                            leadingIcon = { Icon(Icons.Rounded.Search, null) },
                            trailingIcon = {
                                if (query.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.setQuery("") }) {
                                        Icon(Icons.Rounded.Clear, null)
                                    }
                                }
                            },
                            singleLine = true,
                            shape = LumenSquircle(28.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent
                            )
                        )
                        IconButton(onClick = {
                            viewModel.setViewMode(
                                if (viewMode == StreamViewMode.LIST) StreamViewMode.GRID
                                else StreamViewMode.LIST
                            )
                        }) {
                            Icon(
                                if (viewMode == StreamViewMode.GRID) Icons.Rounded.ViewList
                                else Icons.Rounded.GridView,
                                "toggle view"
                            )
                        }
                    }
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            CategoryChip(
                                label = "all",
                                selected = selectedCategory == null,
                                onClick = { selectedCategory = null }
                            )
                        }
                        items(categories, key = { it }) { category ->
                            CategoryChip(
                                label = category,
                                selected = category == selectedCategory,
                                onClick = { selectedCategory = category }
                            )
                        }
                    }
                    if (viewMode == StreamViewMode.LIST) {
                        LazyColumn(Modifier.weight(1f).fillMaxWidth(), contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = bottomPadding)) {
                            filteredGroups.forEach { group ->
                                item(key = "header_${group.name}") {
                                    Row(
                                        Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                expandedGroups = if (group.name in expandedGroups)
                                                    expandedGroups - group.name
                                                else expandedGroups + group.name
                                            }
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            if (group.name in expandedGroups) Icons.Rounded.KeyboardArrowUp
                                            else Icons.Rounded.Folder,
                                            null, tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        SectionLabel(text = group.name, modifier = Modifier.weight(1f))
                                        Text("${group.streams.size}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                if (group.name in expandedGroups || expandedGroups.isEmpty()) {
                                    items(group.streams, key = { it.id }) { stream ->
                                        Row(
                                            Modifier
                                                .fillMaxWidth()
                                                .clickable { viewModel.playStream(stream, onStreamPlay) }
                                                .padding(horizontal = 16.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                Modifier
                                                    .size(48.dp)
                                                    .clip(LumenSquircle(12.dp))
                                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (stream.logoUrl != null) {
                                                    AsyncImage(model = stream.logoUrl,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(48.dp).clip(LumenSquircle(12.dp)))
                                                } else {
                                                    Icon(Icons.Rounded.Language, null, Modifier.size(24.dp),
                                                        tint = MaterialTheme.colorScheme.primary)
                                                }
                                            }
                                            Spacer(Modifier.width(12.dp))
                                            Column(Modifier.weight(1f)) {
                                                Text(stream.title, style = MaterialTheme.typography.bodyLarge,
                                                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                if (stream.category != null) {
                                                    Text(stream.category,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        maxLines = 1)
                                                }
                                            }
                                            IconButton(onClick = { viewModel.toggleFavorite(stream.id) }) {
                                                Icon(
                                                    if (stream.isFavorite) Icons.Rounded.Favorite
                                                    else Icons.Rounded.FavoriteBorder,
                                                    null,
                                                    tint = if (stream.isFavorite)
                                                        MaterialTheme.colorScheme.primary
                                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            IconButton(onClick = { viewModel.deleteStream(stream.id) }) {
                                                Icon(Icons.Rounded.Delete, null,
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = bottomPadding),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            filteredGroups.forEach { group ->
                                item(key = "header_grid_${group.name}", span = { GridItemSpan(2) }) {
                                    Row(
                                        Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                expandedGroups = if (group.name in expandedGroups)
                                                    expandedGroups - group.name
                                                else expandedGroups + group.name
                                            }
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            if (group.name in expandedGroups) Icons.Rounded.KeyboardArrowUp
                                            else Icons.Rounded.Folder,
                                            null, tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        SectionLabel(text = group.name, modifier = Modifier.weight(1f))
                                        Text("${group.streams.size}", style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                if (group.name in expandedGroups || expandedGroups.isEmpty()) {
                                    val streams = group.streams
                                    items(streams.size) { index ->
                                        val stream = streams[index]
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { viewModel.playStream(stream, onStreamPlay) },
                                            shape = LumenSquircle(12.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                                            )
                                        ) {
                                            Box {
                                                if (stream.logoUrl != null) {
                                                    AsyncImage(model = stream.logoUrl,
                                                        contentDescription = null,
                                                        modifier = Modifier.fillMaxWidth().height(100.dp)
                                                            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)))
                                                } else {
                                                    Box(Modifier.fillMaxWidth().height(100.dp),
                                                        contentAlignment = Alignment.Center) {
                                                        Icon(Icons.Rounded.Language, null, Modifier.size(40.dp),
                                                            tint = MaterialTheme.colorScheme.primary)
                                                    }
                                                }
                                                IconButton(
                                                    onClick = { viewModel.toggleFavorite(stream.id) },
                                                    modifier = Modifier.align(Alignment.TopEnd)
                                                ) {
                                                    Icon(
                                                        if (stream.isFavorite) Icons.Rounded.Favorite
                                                        else Icons.Rounded.FavoriteBorder,
                                                        null,
                                                        tint = if (stream.isFavorite) MaterialTheme.colorScheme.primary
                                                        else Color.White.copy(alpha = 0.7f)
                                                    )
                                                }
                                            }
                                            Text(stream.title,
                                                modifier = Modifier.padding(8.dp),
                                                style = MaterialTheme.typography.bodyMedium,
                                                maxLines = 2, overflow = TextOverflow.Ellipsis)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                ColoredFAB(
                    icon = Icons.Rounded.Add,
                    contentDescription = "Add stream",
                    color = MaterialTheme.colorScheme.secondary,
                    onClick = { showAddDialog = true },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 16.dp, bottom = 16.dp)
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 96.dp)
        )
    }

    val importState by viewModel.importState.collectAsStateWithLifecycle()
    when (val state = importState) {
        is M3uImportState.Importing -> {
            Dialog(
                onDismissRequest = {},
                properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnBackPress = false, dismissOnClickOutside = false)
            ) {
                Surface(shape = LumenSquircle(20.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 4.dp) {
                    Column(Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        ShimmerBox(Modifier.size(40.dp), shape = LumenSquircle(12.dp))
                        Spacer(Modifier.height(16.dp))
                        Text("Importing channels\u2026", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
        is M3uImportState.Success -> {
            LaunchedEffect(state) {
                snackbarHostState.showSnackbar("imported ${state.count} channels from ${state.fileName}")
                viewModel.dismissImportState()
            }
        }
        is M3uImportState.Error -> {
            LaunchedEffect(state) {
                snackbarHostState.showSnackbar("import failed: ${state.message}")
                viewModel.dismissImportState()
            }
        }
        is M3uImportState.Idle -> {}
    }

    if (showAddDialog) {
        var url by remember { mutableStateOf("") }
        var title by remember { mutableStateOf("") }
        Dialog(
            onDismissRequest = { showAddDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(0.85f).imePadding(),
                shape = LumenSquircle(20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp, shadowElevation = 8.dp
            ) {
                Column(Modifier.padding(24.dp)) {
                    Text("Add network stream", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(value = url, onValueChange = { url = it },
                        label = { Text("url") }, singleLine = true,
                        modifier = Modifier.fillMaxWidth(), shape = LumenSquircle(12.dp))
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(value = title, onValueChange = { title = it },
                        label = { Text("title (optional)") }, singleLine = true,
                        modifier = Modifier.fillMaxWidth(), shape = LumenSquircle(12.dp))
                    Spacer(Modifier.height(24.dp))
                    Row(modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
                        Spacer(Modifier.width(8.dp))
                        FilledTonalButton(
                            onClick = {
                                if (url.isNotBlank()) {
                                    viewModel.addAndPlay(url.trim(), title.trim().ifEmpty { url.trim() }, onStreamPlay)
                                    showAddDialog = false
                                }
                            },
                            shape = LumenSquircle(12.dp),
                            enabled = url.isNotBlank()
                        ) { Text("Add & play") }
                    }
                }
            }
        }
    }
}

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
                LumenEmptyState(
                    modifier = Modifier.navigationBarsPadding(),
                    blobColor = LumenSage,
                    blobMood = Mood.IDLE,
                    title = "No playlists yet",
                    accents = { PlaylistAccents(LumenSage) },
                    actions = {
                        FilledTonalButton(
                            onClick = { showCreateDialog = true },
                            shape = LumenSquircle(12.dp)
                        ) {
                            Icon(Icons.Rounded.Add, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Create your first playlist")
                        }
                    }
                )
        } else {
            LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 16.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())) {
                items(playlists, key = { it.id }, contentType = { "playlist" }) { playlist ->
                    Row(
                        Modifier.fillMaxWidth()
                            .clickable { onPlaylistClick(playlist.id) }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier
                                .size(48.dp)
                                .clip(LumenSquircle(12.dp))
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

            ColoredFAB(
                icon = Icons.Rounded.Add,
                contentDescription = "create playlist",
                color = MaterialTheme.colorScheme.secondary,
                onClick = { showCreateDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 16.dp)
            )
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
            label = "playlist name",
            confirmLabel = "Create"
        )
    }
}

@Composable
private fun VideoSkeletonGrid() {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(6) {
            VideoCardSkeleton()
        }
    }
}

@Composable
private fun AudioSkeletonList() {
    LazyColumn(Modifier.fillMaxSize()) {
        items(8) {
            AudioRowSkeleton()
        }
    }
}

@Composable
private fun FolderSkeletonGrid() {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(6) {
            FolderCardSkeleton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )
        }
    }
}

@Composable
private fun EmptyLibraryPlaceholder(onRescan: () -> Unit, modifier: Modifier = Modifier) {
    LumenEmptyState(
        modifier = modifier,
        blobColor = LumenLavender,
        blobMood = Mood.HAPPY,
        title = "No audio on this device",
        accents = { AudioAccents(LumenLavender) },
        actions = {
            FilledTonalButton(onClick = onRescan, shape = LumenSquircle(12.dp)) {
                Icon(Icons.Rounded.Refresh, null, Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Scan again")
            }
        }
    )
}
