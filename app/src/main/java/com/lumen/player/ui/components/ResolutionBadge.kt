package com.lumen.player.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ResolutionBadge(
    resolution: String?,
    modifier: Modifier = Modifier
) {
    if (resolution.isNullOrBlank()) return

    val normalized = resolution.uppercase()

    val (displayText, bgColor, fgColor) = when (normalized) {
        "4K", "2160P", "UHD" -> Triple(
            "4K",
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.onPrimary
        )
        "1080P" -> Triple(
            "1080p",
            Color.Black.copy(alpha = 0.78f),
            Color.White
        )
        else -> return
    }

    Surface(
        modifier = modifier,
        color = bgColor,
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = displayText,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = fgColor,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
