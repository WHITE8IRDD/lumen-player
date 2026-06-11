package com.lumen.player.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lumen.player.core.theme.CategoryBlue
import com.lumen.player.core.theme.CategoryCyan
import com.lumen.player.core.theme.CategoryGreen
import com.lumen.player.core.theme.CategoryOrange
import com.lumen.player.core.theme.CategoryPink
import com.lumen.player.core.theme.CategoryPurple
import com.lumen.player.core.theme.CategoryRed
import com.lumen.player.core.theme.CategoryYellow

private val categoryPalette = listOf(
    CategoryYellow, CategoryGreen, CategoryBlue, CategoryRed,
    CategoryPurple, CategoryOrange, CategoryPink, CategoryCyan
)

fun colorForCategory(category: String): Color {
    val index = kotlin.math.abs(category.hashCode()) % categoryPalette.size
    return categoryPalette[index]
}

@Composable
fun CategoryChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    color: Color = colorForCategory(label),
    modifier: Modifier = Modifier
) {
    val borderColor = if (selected) color else color.copy(alpha = 0.5f)
    val textColor = if (selected) color else color.copy(alpha = 0.85f)
    val bgColor = if (selected) color.copy(alpha = 0.15f) else Color.Transparent

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(bgColor)
            .border(
                width = if (selected) 2.dp else 1.5.dp,
                color = borderColor,
                shape = RoundedCornerShape(percent = 50)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}
