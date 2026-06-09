package com.lumen.player.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

/**
 * Wraps [Slider] with a "scrubbing" state. While the user drags, [onValueChange]
 * is called optimistically, but the bound value isn't pushed back to the player
 * until they release — preventing jitter.
 */
@Composable
fun ScrubbingSlider(
    progress: Float,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier,
    colors: SliderColors = SliderDefaults.colors(
        thumbColor = MaterialTheme.colorScheme.primary,
        activeTrackColor = MaterialTheme.colorScheme.primary,
        inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
    )
) {
    var dragValue by remember { mutableStateOf<Float?>(null) }

    Slider(
        value = (dragValue ?: progress).coerceIn(0f, 1f),
        onValueChange = { dragValue = it },
        onValueChangeFinished = {
            dragValue?.let(onSeek)
            dragValue = null
        },
        colors = colors,
        modifier = modifier
    )
}
