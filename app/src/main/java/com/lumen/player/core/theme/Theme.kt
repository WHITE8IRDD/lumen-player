package com.lumen.player.core.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun LumenTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    accentColor: Color = Color(0xFF00E5C8),
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    val bright = 0.299f * accentColor.red + 0.587f * accentColor.green + 0.114f * accentColor.blue
    val onAccent = if (bright > 0.5f) Color(0xFF1C1B1F) else Color.White

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) {
                dynamicDarkColorScheme(view.context).copy(
                    primary = accentColor,
                    secondary = accentColor,
                    tertiary = accentColor,
                    background = LumenDarkBackground,
                    surface = LumenDarkSurface,
                    surfaceVariant = LumenDarkSurfaceVariant,
                    surfaceContainer = LumenDarkSurfaceContainer,
                    surfaceContainerHigh = Color(0xFF1F1F28),
                    surfaceContainerHighest = Color(0xFF252530),
                    onSurface = LumenDarkOnSurface,
                    onSurfaceVariant = LumenDarkOnSurfaceVariant,
                    outline = LumenDarkOutline
                )
            } else {
                dynamicLightColorScheme(view.context).copy(
                    primary = accentColor,
                    secondary = accentColor,
                    tertiary = accentColor
                )
            }
        }
        darkTheme -> darkColorScheme(
            primary = accentColor,
            onPrimary = onAccent,
            primaryContainer = Color(0xFF2A2A4A),
            onPrimaryContainer = Color(0xFFDDE5FF),
            secondary = accentColor,
            onSecondary = Color(0xFF002B29),
            secondaryContainer = accentColor.copy(alpha = 0.2f),
            onSecondaryContainer = accentColor,
            tertiary = accentColor,
            background = LumenDarkBackground,
            onBackground = LumenDarkOnSurface,
            surface = LumenDarkSurface,
            onSurface = LumenDarkOnSurface,
            surfaceVariant = LumenDarkSurfaceVariant,
            onSurfaceVariant = LumenDarkOnSurfaceVariant,
            surfaceContainer = LumenDarkSurfaceContainer,
            surfaceContainerHigh = Color(0xFF1F1F28),
            surfaceContainerHighest = Color(0xFF252530),
            outline = LumenDarkOutline,
            error = Color(0xFFE84D8A),
            onError = Color(0xFF2A1020)
        )
        else -> lightColorScheme(
            primary = accentColor,
            onPrimary = onAccent,
            primaryContainer = Color(0xFFE0E0FF),
            onPrimaryContainer = Color(0xFF1A0033),
            secondary = accentColor,
            onSecondary = Color.White,
            secondaryContainer = accentColor.copy(alpha = 0.2f),
            onSecondaryContainer = accentColor,
            background = Color(0xFFFFFFFF),
            onBackground = Color(0xFF0A0A0F),
            surface = Color(0xFFF5F5F7),
            onSurface = Color(0xFF0A0A0F),
            surfaceVariant = Color(0xFFEEEEF1),
            onSurfaceVariant = Color(0xFF5A5A65),
            surfaceContainer = Color(0xFFFAFAFA),
            surfaceContainerHigh = Color(0xFFF0F0F3),
            surfaceContainerHighest = Color(0xFFE8E8EC),
            outline = Color(0xFFD0D0D8),
            error = Color(0xFFBA1A1A),
            onError = Color.White
        )
    }

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = LumenTypography,
        shapes = LumenShapes,
        content = content
    )
}
