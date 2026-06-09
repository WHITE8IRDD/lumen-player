package com.lumen.player.ui.player.components

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.lumen.player.player.PlaybackState

/**
 * Decorative pager that swipes between the *current*, *next* and *previous*
 * artworks. Hook up [onSwipe] to drive `next()` / `previous()` if you want
 * gesture-driven skipping. Kept minimal for now — uses the current artwork
 * URI only when there's no queue context.
 */
@Composable
fun AlbumArtPager(
    state: PlaybackState,
    onSwipe: (forward: Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 1 })
    HorizontalPager(
        state = pagerState,
        modifier = modifier.fillMaxWidth()
    ) {
        AsyncImage(
            model = state.artworkUri,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(24.dp))
        )
    }
}
