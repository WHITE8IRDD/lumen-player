package com.lumen.player

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import com.lumen.player.core.theme.LumenTheme
import com.lumen.player.domain.repository.SettingsRepository
import com.lumen.player.domain.repository.ThemeMode
import com.lumen.player.player.PlayerController
import com.lumen.player.ui.navigation.LumenNavHost
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@UnstableApi
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var playerController: PlayerController
    @Inject lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        playerController.initialize()

        setContent {
            val settings by settingsRepository.settings.collectAsStateWithLifecycle()

            val fontScale = when (settings.fontSize) {
                "SMALL" -> 0.9f
                "LARGE" -> 1.15f
                else -> 1.0f
            }

            val isDark = when (settings.themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                else -> isSystemInDarkTheme()
            }

            val accentColor = runCatching {
                val hex = settings.accentColorHex.removePrefix("#")
                Color(("FF$hex").toLong(16))
            }.getOrDefault(Color(0xFF00E5C8))

            LumenTheme(
                darkTheme = isDark,
                dynamicColor = settings.dynamicColor,
                accentColor = accentColor
            ) {
                CompositionLocalProvider(
                    LocalDensity provides Density(
                        density = LocalDensity.current.density,
                        fontScale = LocalDensity.current.fontScale * fontScale
                    )
                ) {
                val ctx = LocalContext.current

                LumenNavHost(hasMediaPermission = MediaPermissions.isGranted(ctx))
                }
            }
        }
    }

    override fun onDestroy() {
        if (isFinishing) playerController.release()
        super.onDestroy()
    }
}
