package com.lumen.player.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.lumen.player.core.theme.LumenTextSecondary
import com.lumen.player.core.util.TimeFormatter
import com.lumen.player.domain.model.MediaItem

@Composable
fun MediaListItem(
    item: MediaItem,
    onClick: () -> Unit,
    onMoreClick: (() -> Unit)? = null,
    trailingIcon: ImageVector? = null,
    nowPlaying: Boolean = false,
    modifier: Modifier = Modifier
) {
    val ctx = LocalContext.current
    val density = LocalDensity.current
    val thumbPx = with(density) { 56.dp.roundToPx() }
    val thumbRequest = remember(item.albumArtUri) {
        ImageRequest.Builder(ctx)
            .data(item.albumArtUri)
            .size(thumbPx)
            .crossfade(true)
            .build()
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(56.dp)
                .clip(LumenSquircle(12.dp))
        ) {
            SubcomposeAsyncImage(
                model = thumbRequest,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(56.dp),
                loading = { ShimmerBox(modifier = Modifier.size(56.dp)) }
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                item.cleanTitle ?: item.title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (nowPlaying)
                    MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    item.artist ?: "unknown",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, false)
                )
                if (item.durationMs > 0) {
                    Text(
                        "\u00B7",
                        color = LumenTextSecondary
                    )
                    Text(
                        TimeFormatter.format(item.durationMs),
                        style = MaterialTheme.typography.bodySmall,
                        color = LumenTextSecondary
                    )
                }
            }
        }
        trailingIcon?.let {
            Icon(
                it, null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp).padding(start = 8.dp)
            )
        }
        if (onMoreClick != null) {
            IconButton(onClick = onMoreClick) {
                Icon(
                    Icons.Rounded.MoreVert,
                    contentDescription = null
                )
            }
        }
    }
}
