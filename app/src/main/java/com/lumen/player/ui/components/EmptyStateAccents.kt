package com.lumen.player.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

private fun driftTransition(
    durationMs: Int = 10_000,
    delayMs: Int = 0
) = infiniteRepeatable<Float>(
    animation = tween(
        durationMillis = durationMs,
        delayMillis = delayMs,
        easing = FastOutSlowInEasing
    ),
    repeatMode = RepeatMode.Reverse
)

@Composable
private fun DriftOffset(
    durationMs: Int = 10_000,
    delayMs: Int = 0,
    content: @Composable (y: Dp) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "drift-$durationMs-$delayMs")
    val driftY by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = driftTransition(durationMs, delayMs),
        label = "drift-y"
    )
    content(driftY.dp)
}

@Composable
fun BoxScope.OrbitingAccents(
    icons: List<ImageVector>,
    blobColor: Color,
    alpha: Float = 0.22f
) {
    val radius = 110.dp
    val iconSize = 20.dp
    val radPerDeg = 0.0174533f
    val count = icons.size.coerceAtLeast(1)
    icons.forEachIndexed { index, icon ->
        val angle = (360f / count) * index - 90f
        val rad = angle * radPerDeg
        val offsetX = (radius.value * cos(rad.toDouble())).dp
        val offsetY = (radius.value * sin(rad.toDouble())).dp
        DriftOffset(durationMs = 10_000, delayMs = index * 1000) { drift ->
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier
                    .size(iconSize)
                    .align(Alignment.Center)
                    .offset(x = offsetX, y = offsetY + drift)
                    .alpha(alpha),
                tint = blobColor
            )
        }
    }
}

@Composable
fun BoxScope.HomeAccents(blobColor: Color) {
    DriftOffset(durationMs = 10_000) { drift ->
        Canvas(
            modifier = Modifier
                .size(24.dp)
                .align(Alignment.Center)
                .offset(x = 90.dp, y = (-70).dp + drift)
        ) {
            drawCircle(color = blobColor.copy(alpha = 0.20f), radius = size.minDimension / 2f)
        }
    }
    DriftOffset(durationMs = 12_000, delayMs = 2000) { drift ->
        Canvas(
            modifier = Modifier
                .size(18.dp)
                .align(Alignment.Center)
                .offset(x = (-80).dp, y = 85.dp + drift)
        ) {
            drawCircle(color = blobColor.copy(alpha = 0.20f), radius = size.minDimension / 2f)
        }
    }
}

@Composable
fun BoxScope.VideoAccents(blobColor: Color) {
    DriftOffset(durationMs = 10_000, delayMs = 1000) { drift ->
        Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = null,
            modifier = Modifier
                .size(28.dp)
                .align(Alignment.Center)
                .offset(x = 75.dp, y = 60.dp + drift)
                .alpha(0.20f),
            tint = blobColor
        )
    }
}

@Composable
fun BoxScope.AudioAccents(blobColor: Color) {
    OrbitingAccents(
        icons = listOf(
            Icons.Default.MusicNote,
            Icons.Default.PlayArrow,
            Icons.AutoMirrored.Filled.QueueMusic,
            Icons.Default.Add
        ),
        blobColor = blobColor
    )
}

@Composable
fun BoxScope.FolderAccents(blobColor: Color) {
    OrbitingAccents(
        icons = listOf(
            Icons.Default.Folder,
            Icons.AutoMirrored.Filled.InsertDriveFile,
            Icons.Default.Folder,
            Icons.AutoMirrored.Filled.InsertDriveFile
        ),
        blobColor = blobColor
    )
}

@Composable
fun BoxScope.PlaylistAccents(blobColor: Color) {
    OrbitingAccents(
        icons = listOf(
            Icons.AutoMirrored.Filled.QueueMusic,
            Icons.Default.Add,
            Icons.Default.MusicNote,
            Icons.Default.PlayArrow
        ),
        blobColor = blobColor
    )
}

@Composable
fun BoxScope.StreamAccents(blobColor: Color) {
    DriftOffset(durationMs = 10_000, delayMs = 1000) { drift ->
        Icon(
            imageVector = Icons.Default.Public,
            contentDescription = null,
            modifier = Modifier
                .size(28.dp)
                .align(Alignment.Center)
                .offset(x = 80.dp, y = (-70).dp + drift)
                .alpha(0.20f),
            tint = blobColor
        )
    }
    DriftOffset(durationMs = 8000) { drift ->
        Canvas(
            modifier = Modifier
                .size(20.dp)
                .align(Alignment.Center)
                .offset(x = (-95).dp, y = (-10).dp + drift)
        ) {
            val strokeWidth = 2.dp.toPx()
            drawArc(
                color = blobColor.copy(alpha = 0.20f),
                startAngle = 0f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(strokeWidth, strokeWidth),
                size = androidx.compose.ui.geometry.Size(
                    size.width - strokeWidth * 2,
                    size.height - strokeWidth * 2
                ),
                style = Stroke(width = strokeWidth)
            )
        }
    }
    DriftOffset(durationMs = 12_000, delayMs = 2000) { drift ->
        Canvas(
            modifier = Modifier
                .size(16.dp)
                .align(Alignment.Center)
                .offset(x = (-70).dp, y = 80.dp + drift)
        ) {
            val strokeWidth = 2.dp.toPx()
            drawArc(
                color = blobColor.copy(alpha = 0.18f),
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(strokeWidth, strokeWidth),
                size = androidx.compose.ui.geometry.Size(
                    size.width - strokeWidth * 2,
                    size.height - strokeWidth * 2
                ),
                style = Stroke(width = strokeWidth)
            )
        }
    }
}

@Composable
fun BoxScope.FolderDetailAccents(blobColor: Color) {
    OrbitingAccents(
        icons = listOf(
            Icons.AutoMirrored.Filled.InsertDriveFile,
            Icons.Default.PlayArrow,
            Icons.Default.Folder,
            Icons.Default.MusicNote
        ),
        blobColor = blobColor
    )
}

@Composable
fun BoxScope.PlaylistDetailAccents(blobColor: Color) {
    OrbitingAccents(
        icons = listOf(
            Icons.Default.MusicNote,
            Icons.Default.Add,
            Icons.AutoMirrored.Filled.QueueMusic,
            Icons.Default.PlayArrow
        ),
        blobColor = blobColor
    )
}
