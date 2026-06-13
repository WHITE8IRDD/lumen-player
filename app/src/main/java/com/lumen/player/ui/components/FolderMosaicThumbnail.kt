package com.lumen.player.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import coil.compose.SubcomposeAsyncImage

@Composable
fun FolderMosaicThumbnail(
    model: Any?,
    folderName: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    if (model != null) {
        SubcomposeAsyncImage(
            model = model,
            contentDescription = null,
            contentScale = contentScale,
            modifier = modifier,
            loading = { ShimmerBox(modifier = Modifier.fillMaxSize()) },
            error = {
                FolderThumbnailPlaceholder(folderName = folderName)
            }
        )
    } else {
        FolderThumbnailPlaceholder(folderName = folderName)
    }
}

@Composable
private fun FolderThumbnailPlaceholder(folderName: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        val initial = folderName.firstOrNull()?.uppercase() ?: "F"
        Text(
            text = initial,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
        )
    }
}
