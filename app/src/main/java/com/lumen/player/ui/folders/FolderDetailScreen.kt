package com.lumen.player.ui.folders

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.rounded.ViewAgenda
import androidx.compose.material.icons.rounded.ViewModule
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.lumen.player.ui.library.SelectionActionBar
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import com.lumen.player.domain.model.MediaItem
import com.lumen.player.ui.library.VideoGrid
import com.lumen.player.ui.library.VideoList
import com.lumen.player.ui.library.VideoSort
import com.lumen.player.ui.components.CircularIconButton
import com.lumen.player.data.metadata.MediaTitleParser
import com.lumen.player.ui.components.FloatingGlassCard
import com.lumen.player.ui.components.LumenBlob
import com.lumen.player.ui.components.LumenEmptyState
import com.lumen.player.ui.components.Mood
import com.lumen.player.ui.components.FolderDetailAccents
import com.lumen.player.ui.components.VideoCardSkeleton
import com.lumen.player.core.theme.LumenOcean
import com.lumen.player.core.util.TimeFormatter
import java.util.Locale

@UnstableApi
@Composable
fun FolderDetailScreen(
    onBack: () -> Unit,
    onVideoClick: (MediaItem) -> Unit = {},
    isGridView: Boolean = true,
    onToggleViewMode: () -> Unit = {},
    viewModel: FolderDetailViewModel = hiltViewModel()
) {
    val items by viewModel.items.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    var sort by remember { mutableStateOf(VideoSort.TITLE) }
    var sortMenuExpanded by remember { mutableStateOf(false) }
    var selectedIds by remember { mutableStateOf(setOf<Long>()) }
    val inSelectionMode = selectedIds.isNotEmpty()

    val sorted = remember(items, sort) {
        when (sort) {
            VideoSort.TITLE -> items.sortedBy { it.title.lowercase(Locale.getDefault()) }
            VideoSort.DATE -> items.sortedByDescending { it.dateAdded }
            VideoSort.DURATION -> items.sortedByDescending { it.durationMs }
            VideoSort.SIZE -> items.sortedByDescending { it.sizeBytes }
            VideoSort.RESOLUTION -> items.sortedByDescending { it.sizeBytes }
        }
    }

    val context = LocalContext.current
    val selectedItems = remember(sorted, selectedIds) {
        sorted.filter { it.id in selectedIds }
    }

    val deleteLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { _ -> selectedIds = emptySet() }

    val open: (MediaItem) -> Unit = { item ->
        if (inSelectionMode) {
            selectedIds = if (item.id in selectedIds) selectedIds - item.id else selectedIds + item.id
        } else {
            val idx = sorted.indexOfFirst { it.id == item.id }.coerceAtLeast(0)
            viewModel.play(sorted, idx)
            onVideoClick(item)
        }
    }

    Column(Modifier.fillMaxSize()) {
        SelectionActionBar(
            selectedCount = selectedIds.size,
            onClear = { selectedIds = emptySet() },
            onPlayNext = {
                viewModel.playNext(selectedItems)
                selectedIds = emptySet()
            },
            onAddToQueue = {
                viewModel.addToQueue(selectedItems)
                selectedIds = emptySet()
            },
            onShare = {
                viewModel.share(context, selectedItems)
                selectedIds = emptySet()
            },
            onDelete = {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                    val intentSender = viewModel.createDeleteSender(context, selectedItems)
                    intentSender?.let { deleteLauncher.launch(it) } ?: run { selectedIds = emptySet() }
                } else {
                    viewModel.requestDelete(context, selectedItems)
                    selectedIds = emptySet()
                }
            }
        )

        if (!inSelectionMode) {
            Row(
                Modifier.fillMaxWidth().padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularIconButton(onClick = onBack, icon = Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                Spacer(Modifier.weight(1f))
                Box {
                    IconButton(onClick = { sortMenuExpanded = true }) {
                        Icon(Icons.AutoMirrored.Rounded.Sort, contentDescription = "Sort")
                    }
                    DropdownMenu(
                        expanded = sortMenuExpanded,
                        onDismissRequest = { sortMenuExpanded = false }
                    ) {
                        VideoSort.values().forEach { s ->
                            DropdownMenuItem(
                                text = { Text(s.label()) },
                                onClick = {
                                    sort = s
                                    sortMenuExpanded = false
                                }
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
                        Icon(Icons.Rounded.ViewModule, contentDescription = "Grid")
                    }
                    SegmentedButton(
                        selected = !isGridView,
                        onClick = { if (isGridView) onToggleViewMode() },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                    ) {
                        Icon(Icons.Rounded.ViewAgenda, contentDescription = "List")
                    }
                }
            }
            var cleanFolderName by remember { mutableStateOf(viewModel.folderName) }
            var folderYear by remember { mutableStateOf<String?>(null) }
            LaunchedEffect(viewModel.folderName) {
                val meta = MediaTitleParser().resolve(viewModel.folderName)
                cleanFolderName = meta.cleanTitle.ifEmpty { viewModel.folderName }
                folderYear = meta.year?.toString()
            }
            FloatingGlassCard(
                title = cleanFolderName,
                subtitle = buildString {
                    if (folderYear != null) append("$folderYear · ")
                    append("${items.size} videos · ")
                    append(TimeFormatter.format(items.sumOf { it.durationMs }))
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(8.dp))
        }

        Crossfade(targetState = isLoading, animationSpec = tween(200)) { loading ->
            if (loading) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize().padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(6) { VideoCardSkeleton() }
                }
            } else if (items.isEmpty()) {
                LumenEmptyState(
                    blobColor = LumenOcean,
                    blobMood = Mood.SLEEPING,
                    title = "no media in this folder",
                    accents = { FolderDetailAccents(LumenOcean) }
                )
            } else if (isGridView) {
                VideoGrid(
                    items = sorted,
                    onClick = open,
                    selectedIds = selectedIds,
                    onLongClick = { item -> selectedIds = selectedIds + item.id }
                )
            } else {
                VideoList(
                    items = sorted,
                    onClick = open,
                    selectedIds = selectedIds,
                    onLongClick = { item -> selectedIds = selectedIds + item.id }
                )
            }
        }
    }
}

private fun VideoSort.label(): String = when (this) {
    VideoSort.TITLE -> "title"
    VideoSort.DATE -> "date added"
    VideoSort.DURATION -> "duration"
    VideoSort.SIZE -> "size"
    VideoSort.RESOLUTION -> "resolution"
}
