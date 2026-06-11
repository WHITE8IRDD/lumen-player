package com.lumen.player.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

class NavTab(
    val route: String,
    val labelRes: Int,
    val inactiveIcon: ImageVector,
    val activeIcon: ImageVector,
    val color: Color,
    val deepColor: Color
)

@Composable
fun GlassNavigationBar(
    currentRoute: String?,
    tabs: List<NavTab>,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .navigationBarsPadding()
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(28.dp),
                ambientColor = Color.Black.copy(alpha = 0.15f),
                spotColor = Color.Black.copy(alpha = 0.2f)
            )
            .clip(RoundedCornerShape(28.dp)),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp)
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEach { tab ->
                val selected = currentRoute == tab.route
                BottomNavItem(
                    selected = selected,
                    icon = {
                        if (selected) {
                            ShapeIcon(
                                icon = tab.activeIcon,
                                gradientStart = tab.color,
                                gradientEnd = tab.deepColor,
                                size = 32.dp,
                                iconSize = 18.dp
                            )
                        } else {
                            Icon(
                                imageVector = tab.inactiveIcon,
                                contentDescription = stringResource(tab.labelRes),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    },
                    label = stringResource(tab.labelRes),
                    signatureColor = tab.color,
                    onClick = {
                        if (!selected) onTabSelected(tab.route)
                    }
                )
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    selected: Boolean,
    icon: @Composable () -> Unit,
    label: String,
    signatureColor: Color,
    onClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val pillAlpha = when {
        selected && isDark -> 0.30f
        selected -> 0.15f
        else -> 0f
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(if (selected) signatureColor.copy(alpha = pillAlpha) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            icon()
            Spacer(Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (selected) signatureColor
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
            )
        }
    }
}
