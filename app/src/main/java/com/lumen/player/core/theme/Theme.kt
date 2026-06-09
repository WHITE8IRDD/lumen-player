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

private val LumenDark = darkColorScheme(
    primary = LumenPrimary,
    onPrimary = Color(0xFF1E1B2E),
    primaryContainer = LumenPrimaryDim,
    onPrimaryContainer = Color(0xFFEADDFF),
    secondary = LumenTertiary,
    onSecondary = Color(0xFF003733),
    secondaryContainer = Color(0xFF005048),
    onSecondaryContainer = Color(0xFF6FF7E0),
    tertiary = LumenTertiary,
    background = LumenNearBlack,
    onBackground = LumenOnBackground,
    surface = LumenSurface,
    onSurface = LumenOnBackground,
    surfaceVariant = LumenSurfaceVariant,
    onSurfaceVariant = LumenOnSurfaceVariant,
    outline = LumenOutline,
    error = LumenError,
    onError = Color(0xFF601410)
)

private val LumenLight = lightColorScheme(
    primary = LumenPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEADDFF),
    onPrimaryContainer = Color(0xFF21005D),
    secondary = Color(0xFF006B5E),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF6FF7E0),
    onSecondaryContainer = Color(0xFF00201B),
    background = Color(0xFFFFFBFE),
    onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFFFBFE),
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFF79747E),
    error = Color(0xFFBA1A1A),
    onError = Color.White
)

@Composable
fun LumenTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val view = LocalView.current

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDarkColorScheme(view.context) else dynamicLightColorScheme(view.context)
        darkTheme -> LumenDark
        else -> LumenLight
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
