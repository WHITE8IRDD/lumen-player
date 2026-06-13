package com.lumen.player.core.theme

import android.app.Activity
import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.core.view.WindowCompat
import com.lumen.player.ui.theme.LocalLumenTextCase
import com.lumen.player.ui.theme.LumenFontChoice
import com.lumen.player.ui.theme.LumenTextCase

@Composable
fun LumenTheme(
    dynamicColor: Boolean = false,
    accentColor: Color = Color(0xFFFFC857),
    fontFamily: FontFamily = LumenFontChoice.SERIF_DISPLAY.family,
    textCase: LumenTextCase = LumenTextCase.LOWERCASE,
    weightOffset: Int = 0,
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    val bright = 0.299f * accentColor.red + 0.587f * accentColor.green + 0.114f * accentColor.blue
    val onAccent = if (bright > 0.5f) Color(0xFF1C1B1F) else Color.White

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            dynamicDarkColorScheme(view.context).copy(
                primary = accentColor,
                secondary = accentColor,
                tertiary = accentColor,
                background = LumenBlack,
                surface = LumenSurfaceElevated,
                surfaceVariant = LumenSurfaceHigh,
                surfaceContainer = LumenSurfaceHigh,
                surfaceContainerHigh = Color(0xFF1F1F28),
                surfaceContainerHighest = Color(0xFF252530),
                onSurface = LumenTextPrimary,
                onSurfaceVariant = LumenTextSecondary,
                outline = LumenBorderSubtle
            )
        }
        else -> darkColorScheme(
            primary = accentColor,
            onPrimary = onAccent,
            primaryContainer = Color(0xFF2A2A4A),
            onPrimaryContainer = Color(0xFFDDE5FF),
            secondary = accentColor,
            onSecondary = Color(0xFF002B29),
            secondaryContainer = accentColor.copy(alpha = 0.2f),
            onSecondaryContainer = accentColor,
            tertiary = accentColor,
            background = LumenBlack,
            onBackground = LumenTextPrimary,
            surface = LumenSurfaceElevated,
            onSurface = LumenTextPrimary,
            surfaceVariant = LumenSurfaceHigh,
            onSurfaceVariant = LumenTextSecondary,
            surfaceContainer = LumenSurfaceHigh,
            surfaceContainerHigh = Color(0xFF1F1F28),
            surfaceContainerHighest = Color(0xFF252530),
            outline = LumenBorderSubtle,
            error = Color(0xFFE84D8A),
            onError = Color(0xFF2A1020)
        )
    }

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    val typography = remember(fontFamily, weightOffset) {
        buildLumenTypography(family = fontFamily, weightOffset = weightOffset)
    }

    CompositionLocalProvider(LocalLumenTextCase provides textCase) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            shapes = LumenShapes,
            content = content
        )
    }
}
