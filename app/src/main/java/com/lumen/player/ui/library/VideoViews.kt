package com.lumen.player.ui.library

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.key
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.PlaylistAdd
import androidx.compose.material.icons.rounded.QueuePlayNext
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.util.UnstableApi
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.lumen.player.core.theme.LumenOnSurfaceDim
import com.lumen.player.core.util.TimeFormatter
import com.lumen.player.domain.model.Folder
import com.lumen.player.domain.model.MediaItem
import com.lumen.player.ui.components.ShimmerBox
import com.lumen.player.ui.util.FolderThumbnail
import com.lumen.player.ui.util.FolderThumbnailResolver
import com.lumen.player.ui.util.FolderThumbnailType
import com.lumen.player.ui.util.MosaicFetcherInput
import com.lumen.player.ui.util.VideoThumbnail



@OptIn(ExperimentalFoundationApi::class)
@UnstableApi
@Composable
fun VideoGrid(
    items: List<MediaItem>,
    onClick: (MediaItem) -> Unit,
    selectedIds: Set<Long> = emptySet(),
    onLongClick: ((MediaItem) -> Unit)? = null,
    onAddToQueue: ((MediaItem) -> Unit)? = null,
    onAddToPlaylist: ((MediaItem) -> Unit)? = null,
    onShare: ((MediaItem) -> Unit)? = null,
    onDelete: ((MediaItem) -> Unit)? = null,
    onPlayNext: ((MediaItem) -> Unit)? = null,
    onOpenFileLocation: ((MediaItem) -> Unit)? = null,
    onProperties: ((MediaItem) -> Unit)? = null,
    onHideFromHome: ((MediaItem) -> Unit)? = null,
    contentPadding: PaddingValues? = null
) {
    val effectiveContentPadding = contentPadding ?: PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 16.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = effectiveContentPadding,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(items, key = { it.id }) { item ->
            VideoGridCard(
                item = item,
                onClick = { onClick(item) },
                isSelected = item.id in selectedIds,
                onLongClick = { onLongClick?.invoke(item) },
                onAddToQueue = { onAddToQueue?.invoke(item) },
                onAddToPlaylist = { onAddToPlaylist?.invoke(item) },
                onShare = { onShare?.invoke(item) },
                onDelete = { onDelete?.invoke(item) },
                onPlayNext = { onPlayNext?.invoke(item) },
                onOpenFileLocation = { onOpenFileLocation?.invoke(item) },
                onProperties = { onProperties?.invoke(item) },
                onHideFromHome = { onHideFromHome?.invoke(item) }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@UnstableApi
@Composable
fun VideoGridCard(
    item: MediaItem,
    onClick: () -> Unit,
    isSelected: Boolean = false,
    onLongClick: (() -> Unit)? = null,
    onAddToQueue: (() -> Unit)? = null,
    onAddToPlaylist: (() -> Unit)? = null,
    onShare: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    onPlayNext: (() -> Unit)? = null,
    onOpenFileLocation: (() -> Unit)? = null,
    onProperties: (() -> Unit)? = null,
    onHideFromHome: (() -> Unit)? = null
) {
    var showMenu by remember { mutableStateOf(false) }
    val progress = if (item.durationMs > 0L)
        (item.lastPositionMs.toFloat() / item.durationMs).coerceIn(0f, 1f)
    else 0f
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 11f)
                    .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                SubcomposeAsyncImage(
                    model = item.posterUrl ?: VideoThumbnail(item.id, item.uri),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    loading = { ShimmerBox(modifier = Modifier.fillMaxSize()) }
                )
                if (progress > 0f) {
                    Box(
                        Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .height(3.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color.Black.copy(alpha = 0.35f))
                    ) {
                        Box(
                            Modifier
                                .fillMaxWidth(progress)
                                .height(3.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(MaterialTheme.colorScheme.tertiary)
                        )
                    }
                }
                if (item.lastPositionMs > 0L) {
                    Box(
                        Modifier
                            .align(Alignment.TopStart)
                            .padding(6.dp)
                            .size(8.dp)
                            .clip(RoundedCornerShape(50))
                            .background(MaterialTheme.colorScheme.tertiary)
                    )
                }
                if (isSelected) {
                    Box(
                        Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.tertiary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.Check, contentDescription = "Selected",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                if (!isSelected) {
                    Box(Modifier.align(Alignment.TopEnd).padding(2.dp)) {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                Icons.Rounded.MoreVert, contentDescription = "More options",
                                tint = Color.White
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Play") },
                                onClick = { showMenu = false; onClick() }
                            )
                            DropdownMenuItem(
                                text = { Text("Play next") },
                                onClick = { showMenu = false; onPlayNext?.invoke() },
                                enabled = onPlayNext != null
                            )
                            DropdownMenuItem(
                                text = { Text("Add to queue") },
                                onClick = { showMenu = false; onAddToQueue?.invoke() },
                                enabled = onAddToQueue != null
                            )
                            DropdownMenuItem(
                                text = { Text("Add to playlist") },
                                onClick = { showMenu = false; onAddToPlaylist?.invoke() },
                                enabled = onAddToPlaylist != null
                            )
                            DropdownMenuItem(
                                text = { Text("Open file location") },
                                onClick = { showMenu = false; onOpenFileLocation?.invoke() },
                                enabled = onOpenFileLocation != null
                            )
                            DropdownMenuItem(
                                text = { Text("Properties") },
                                onClick = { showMenu = false; onProperties?.invoke() },
                                enabled = onProperties != null
                            )
                            DropdownMenuItem(
                                text = { Text("Hide from Home") },
                                onClick = { showMenu = false; onHideFromHome?.invoke() },
                                enabled = onHideFromHome != null
                            )
                            DropdownMenuItem(
                                text = { Text("Share") },
                                onClick = { showMenu = false; onShare?.invoke() },
                                enabled = onShare != null
                            )
                            DropdownMenuItem(
                                text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                                onClick = { showMenu = false; onDelete?.invoke() },
                                enabled = onDelete != null
                            )
                        }
                    }
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
@UnstableApi
@Composable
fun VideoList(
    items: List<MediaItem>,
    onClick: (MediaItem) -> Unit,
    selectedIds: Set<Long> = emptySet(),
    onLongClick: ((MediaItem) -> Unit)? = null,
    onAddToQueue: ((MediaItem) -> Unit)? = null,
    onAddToPlaylist: ((MediaItem) -> Unit)? = null,
    onShare: ((MediaItem) -> Unit)? = null,
    onDelete: ((MediaItem) -> Unit)? = null,
    onPlayNext: ((MediaItem) -> Unit)? = null,
    onOpenFileLocation: ((MediaItem) -> Unit)? = null,
    onRename: ((MediaItem) -> Unit)? = null,
    onProperties: ((MediaItem) -> Unit)? = null,
    onHideFromHome: ((MediaItem) -> Unit)? = null,
    contentPadding: PaddingValues? = null
) {
    val effectiveContentPadding = contentPadding ?: PaddingValues(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 16.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
    LazyColumn(
        contentPadding = effectiveContentPadding,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(items, key = { it.id }) { item ->
            VideoListRow(
                item = item,
                onClick = { onClick(item) },
                isSelected = item.id in selectedIds,
                onLongClick = { onLongClick?.invoke(item) },
                onAddToQueue = { onAddToQueue?.invoke(item) },
                onAddToPlaylist = { onAddToPlaylist?.invoke(item) },
                onShare = { onShare?.invoke(item) },
                onDelete = { onDelete?.invoke(item) },
                onPlayNext = { onPlayNext?.invoke(item) },
                onOpenFileLocation = { onOpenFileLocation?.invoke(item) },
                onProperties = { onProperties?.invoke(item) },
                onHideFromHome = { onHideFromHome?.invoke(item) }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@UnstableApi
@Composable
fun VideoListRow(
    item: MediaItem,
    onClick: () -> Unit,
    isSelected: Boolean = false,
    onLongClick: (() -> Unit)? = null,
    onAddToQueue: (() -> Unit)? = null,
    onAddToPlaylist: (() -> Unit)? = null,
    onShare: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    onPlayNext: (() -> Unit)? = null,
    onOpenFileLocation: (() -> Unit)? = null,
    onProperties: (() -> Unit)? = null,
    onHideFromHome: (() -> Unit)? = null
) {
    var showMenu by remember { mutableStateOf(false) }
    val progress = if (item.durationMs > 0L)
        (item.lastPositionMs.toFloat() / item.durationMs).coerceIn(0f, 1f)
    else 0f
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(width = 96.dp, height = 54.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                val ctx = LocalContext.current
                val thumbPxW = with(LocalDensity.current) { 96.dp.roundToPx() }
                val thumbPxH = with(LocalDensity.current) { 54.dp.roundToPx() }
                val thumbModel = remember(item.id, item.posterUrl, item.uri) {
                    val data = item.posterUrl ?: VideoThumbnail(item.id, item.uri)
                    ImageRequest.Builder(ctx)
                        .data(data)
                        .size(thumbPxW, thumbPxH)
                        .crossfade(true)
                        .build()
                }
                SubcomposeAsyncImage(
                    model = thumbModel,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    loading = { ShimmerBox(modifier = Modifier.fillMaxSize()) }
                )

                if (progress > 0f) {
                    Box(
                        Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .height(3.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color.Black.copy(alpha = 0.35f))
                    ) {
                        Box(
                            Modifier
                                .fillMaxWidth(progress)
                                .height(3.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(MaterialTheme.colorScheme.tertiary)
                        )
                    }
                }
                if (isSelected) {
                    Box(
                        Modifier
                            .align(Alignment.TopEnd)
                            .padding(3.dp)
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.tertiary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.Check, contentDescription = "Selected",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    item.cleanTitle.ifEmpty { item.title },
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        humanSize(item.sizeBytes),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (item.durationMs > 0L) {
                        Text(" \u00B7 ", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            TimeFormatter.format(item.durationMs),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        Icons.Rounded.MoreVert, contentDescription = "More options",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Play") },
                        onClick = { showMenu = false; onClick() }
                    )
                    DropdownMenuItem(
                        text = { Text("Play next") },
                        onClick = { showMenu = false; onPlayNext?.invoke() },
                        enabled = onPlayNext != null
                    )
                    DropdownMenuItem(
                        text = { Text("Add to queue") },
                        onClick = { showMenu = false; onAddToQueue?.invoke() },
                        enabled = onAddToQueue != null
                    )
                    DropdownMenuItem(
                        text = { Text("Add to playlist") },
                        onClick = { showMenu = false; onAddToPlaylist?.invoke() },
                        enabled = onAddToPlaylist != null
                    )
                    DropdownMenuItem(
                        text = { Text("Open file location") },
                        onClick = { showMenu = false; onOpenFileLocation?.invoke() },
                        enabled = onOpenFileLocation != null
                    )
                    DropdownMenuItem(
                        text = { Text("Properties") },
                        onClick = { showMenu = false; onProperties?.invoke() },
                        enabled = onProperties != null
                    )
                    DropdownMenuItem(
                        text = { Text("Hide from Home") },
                        onClick = { showMenu = false; onHideFromHome?.invoke() },
                        enabled = onHideFromHome != null
                    )
                    DropdownMenuItem(
                        text = { Text("Share") },
                        onClick = { showMenu = false; onShare?.invoke() },
                        enabled = onShare != null
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                        onClick = { showMenu = false; onDelete?.invoke() },
                        enabled = onDelete != null
                    )
                }
            }
        }
    }
}

@Composable
fun FolderCard(
    folder: Folder,
    videos: List<MediaItem>,
    onClick: () -> Unit
) {
    val thumbType = remember(folder.path, videos) {
        FolderThumbnailResolver.resolve(folder, videos)
    }

    var cleanDisplayName by remember { mutableStateOf(folder.displayName) }
    LaunchedEffect(folder.displayName) {
        val parser = com.lumen.player.data.metadata.MediaTitleParser()
        val meta = parser.resolve(folder.displayName)
        cleanDisplayName = meta.cleanTitle.ifEmpty { folder.displayName }
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
            is FolderThumbnailType.Placeholder -> null
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 11f)
                    .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (model != null) {
                    SubcomposeAsyncImage(
                        model = model,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        loading = { ShimmerBox(modifier = Modifier.fillMaxSize()) },
                        error = {
                            Icon(
                                Icons.Rounded.Folder, contentDescription = null,
                                modifier = Modifier.align(Alignment.Center).size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                } else {
                    Icon(
                        Icons.Rounded.Folder, contentDescription = null,
                        modifier = Modifier.align(Alignment.Center).size(48.dp),
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
                    cleanDisplayName,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "${folder.mediaCount} videos \u00B7 ${TimeFormatter.format(folder.totalDurationMs)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FolderGrid(
    folders: List<Folder>,
    videosByFolder: Map<String, List<MediaItem>>,
    onFolderClick: (Folder) -> Unit,
    onSingleVideoClick: (MediaItem) -> Unit,
    onVideoLongClick: ((MediaItem) -> Unit)? = null,
    onAddToQueue: ((MediaItem) -> Unit)? = null,
    onAddToPlaylist: ((MediaItem) -> Unit)? = null,
    onShare: ((MediaItem) -> Unit)? = null,
    onDelete: ((MediaItem) -> Unit)? = null,
    onPlayNext: ((MediaItem) -> Unit)? = null,
    onOpenFileLocation: ((MediaItem) -> Unit)? = null,
    onProperties: ((MediaItem) -> Unit)? = null,
    onHideFromHome: ((MediaItem) -> Unit)? = null,
    selectedIds: Set<Long> = emptySet(),
    contentPadding: PaddingValues? = null
) {
    val effectiveContentPadding = contentPadding ?: PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 16.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
    val singleFolders = remember(folders) { folders.filter { it.mediaCount == 1 && it.firstVideoUri != null } }
    val multiFolders = remember(folders) { folders.filter { it.mediaCount != 1 || it.firstVideoUri == null } }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = effectiveContentPadding,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(singleFolders, key = { it.path }) { folder ->
            val item = folder.toSingleVideo()
            VideoGridCard(
                item = item,
                onClick = { onSingleVideoClick(item) },
                isSelected = item.id in selectedIds,
                onLongClick = { onVideoLongClick?.invoke(item) },
                onAddToQueue = { onAddToQueue?.invoke(item) },
                onAddToPlaylist = { onAddToPlaylist?.invoke(item) },
                onShare = { onShare?.invoke(item) },
                onDelete = { onDelete?.invoke(item) },
                onPlayNext = { onPlayNext?.invoke(item) },
                onOpenFileLocation = { onOpenFileLocation?.invoke(item) },
                onProperties = { onProperties?.invoke(item) },
                onHideFromHome = { onHideFromHome?.invoke(item) }
            )
        }
        items(multiFolders, key = { it.path }) { folder ->
            val folderVideos = videosByFolder[folder.path].orEmpty()
            FolderCard(
                folder = folder,
                videos = folderVideos,
                onClick = { onFolderClick(folder) }
            )
        }
    }
}

@Composable
fun SelectionActionBar(
    selectedCount: Int,
    selectionMode: Boolean = false,
    onClear: () -> Unit,
    onPlayNext: () -> Unit,
    onAddToQueue: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    onAddToPlaylist: (() -> Unit)? = null
) {
    AnimatedVisibility(
        visible = selectedCount > 0 || selectionMode,
        enter = slideInVertically { it } + fadeIn(),
        exit = slideOutVertically { it } + fadeOut()
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.97f))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClear) {
                Icon(Icons.Rounded.Close, contentDescription = "Exit selection")
            }
            Text(
                "$selectedCount selected",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier.weight(1f)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SelectionActionButton(
                    icon = Icons.Rounded.QueuePlayNext,
                    label = "Play next",
                    onClick = onPlayNext
                )
                SelectionActionButton(
                    icon = Icons.Rounded.PlaylistAdd,
                    label = "Add to queue",
                    onClick = onAddToQueue
                )
                SelectionActionButton(
                    icon = Icons.Rounded.Share,
                    label = "Share",
                    onClick = onShare
                )
                if (onAddToPlaylist != null) {
                    SelectionActionButton(
                        icon = Icons.Rounded.PlaylistAdd,
                        label = "Add to playlist",
                        onClick = onAddToPlaylist
                    )
                }
                SelectionActionButton(
                    icon = Icons.Rounded.Delete,
                    label = "Delete",
                    onClick = onDelete,
                    isDestructive = true
                )
            }
        }
    }
}

@Composable
private fun SelectionActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = Modifier.height(48.dp),
        shape = RoundedCornerShape(10.dp),
        colors = if (isDestructive) ButtonDefaults.filledTonalButtonColors(
            contentColor = MaterialTheme.colorScheme.error
        ) else ButtonDefaults.filledTonalButtonColors()
    ) {
        Icon(icon, contentDescription = label, modifier = Modifier.size(18.dp))
    }
}

fun humanSize(bytes: Long): String {
    if (bytes <= 0) return "\u2014"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    var b = bytes.toDouble()
    var i = 0
    while (b >= 1024 && i < units.size - 1) { b /= 1024; i++ }
    return if (i == 0) "$bytes ${units[i]}"
    else String.format(java.util.Locale.getDefault(), "%.1f %s", b, units[i])
}

fun Folder.toSingleVideo(): MediaItem = MediaItem(
    id = firstVideoId ?: 0L,
    uri = firstVideoUri ?: Uri.EMPTY,
    title = firstVideoTitle ?: displayName,
    cleanTitle = firstVideoTitle ?: displayName,
    year = null,
    seasonEpisode = null,
    artist = null,
    album = null,
    albumArtUri = firstVideoUri,
    durationMs = firstVideoDurationMs,
    mimeType = "video/*",
    sizeBytes = firstVideoSizeBytes,
    folderPath = path,
    type = com.lumen.player.domain.model.MediaType.VIDEO,
    dateAdded = dateAdded,
    trackNumber = null,
    lastPositionMs = (firstVideoProgress * firstVideoDurationMs).toLong(),
    playCount = 0,
    isFavorite = false,
    isVideo = true,
    isHiddenFromHome = false,
    isHiddenFromContinueWatching = false,
    posterUrl = firstVideoPosterUrl
)

enum class VideoSort { TITLE, DATE, DURATION, SIZE, RESOLUTION }

fun VideoSort.label(): String = when (this) {
    VideoSort.TITLE -> "Title"
    VideoSort.DATE -> "Date added"
    VideoSort.DURATION -> "Duration"
    VideoSort.SIZE -> "Size"
    VideoSort.RESOLUTION -> "Resolution"
}
