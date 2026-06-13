package com.lumen.player.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun VideoCardSkeleton(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f),
            shape = LumenSquircle(12.dp)
        )
        Spacer(Modifier.height(8.dp))
        ShimmerBox(
            modifier = Modifier.fillMaxWidth(0.8f).height(14.dp),
            shape = LumenSquircle(4.dp)
        )
        Spacer(Modifier.height(4.dp))
        ShimmerBox(
            modifier = Modifier.fillMaxWidth(0.5f).height(12.dp),
            shape = LumenSquircle(4.dp)
        )
    }
}

@Composable
fun AudioRowSkeleton(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ShimmerBox(
            modifier = Modifier.size(56.dp),
            shape = LumenSquircle(8.dp)
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            ShimmerBox(
                modifier = Modifier.fillMaxWidth(0.7f).height(14.dp),
                shape = LumenSquircle(4.dp)
            )
            Spacer(Modifier.height(6.dp))
            ShimmerBox(
                modifier = Modifier.fillMaxWidth(0.4f).height(12.dp),
                shape = LumenSquircle(4.dp)
            )
        }
    }
}

@Composable
fun PlaylistCardSkeleton(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        ShimmerBox(
            modifier = Modifier.fillMaxWidth().aspectRatio(1f),
            shape = LumenSquircle(16.dp)
        )
        Spacer(Modifier.height(8.dp))
        ShimmerBox(
            modifier = Modifier.fillMaxWidth(0.75f).height(14.dp),
            shape = LumenSquircle(4.dp)
        )
        Spacer(Modifier.height(4.dp))
        ShimmerBox(
            modifier = Modifier.fillMaxWidth(0.4f).height(12.dp),
            shape = LumenSquircle(4.dp)
        )
    }
}

@Composable
fun FolderCardSkeleton(modifier: Modifier = Modifier) {
    ShimmerBox(
        modifier = modifier.aspectRatio(1f),
        shape = LumenSquircle(16.dp)
    )
}

@Composable
fun SectionHeaderSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 8.dp, top = 24.dp, bottom = 12.dp)
    ) {
        ShimmerBox(
            modifier = Modifier.width(60.dp).height(10.dp),
            shape = LumenSquircle(4.dp)
        )
        Spacer(Modifier.height(8.dp))
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ShimmerBox(
                modifier = Modifier.width(140.dp).height(22.dp).weight(1f),
                shape = LumenSquircle(4.dp)
            )
            Spacer(Modifier.width(8.dp))
            ShimmerBox(
                modifier = Modifier.width(50.dp).height(12.dp),
                shape = LumenSquircle(4.dp)
            )
        }
    }
}

@Composable
fun HomeSectionRowSkeleton(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        SectionHeaderSkeleton()
        LazyRow(
            contentPadding = PaddingValues(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(3) {
                VideoCardSkeleton(modifier = Modifier.width(160.dp))
            }
        }
    }
}
