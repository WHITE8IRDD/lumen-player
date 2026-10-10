package com.lumen.player

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity

import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.lumen.player.ui.theme.LocalAccentColor
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import com.lumen.player.core.theme.LumenAccentDefault
import com.lumen.player.core.theme.LumenTheme
import com.lumen.player.domain.repository.SettingsRepository
import com.lumen.player.player.PlayerController
import com.lumen.player.ui.navigation.LumenNavHost
import com.lumen.player.ui.theme.LumenFontChoice
import com.lumen.player.ui.theme.LumenTextCase
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@UnstableApi
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var playerController: PlayerController
    @Inject lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        val isReady = mutableStateOf(false)
        splashScreen.setKeepOnScreenCondition { !isReady.value }

        enableEdgeToEdge()

        // ───────────────────────────────────────────────────────
        // ADAPTIVE REFRESH RATE — auto-detects device capability
        // Picks the highest refresh rate the device supports
        // Falls back gracefully on any device
        // ───────────────────────────────────────────────────────
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val display = display
                val supportedModes = display?.supportedModes ?: emptyArray()
                val bestMode = supportedModes.maxByOrNull { it.refreshRate }
                if (bestMode != null) {
                    window.attributes = window.attributes.apply {
                        preferredDisplayModeId = bestMode.modeId
                    }
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                @Suppress("DEPRECATION")
                val display = windowManager.defaultDisplay
                val supportedRates = display.supportedRefreshRates
                val maxRate = supportedRates.maxOrNull() ?: 60f
                window.attributes = window.attributes.apply {
                    @Suppress("DEPRECATION")
                    preferredRefreshRate = maxRate
                }
            }
        } catch (_: Exception) {
            // App continues at default refresh rate
        }

        playerController.initialize()

        setContent {
            LaunchedEffect(Unit) {
                isReady.value = true
            }
            val settings by settingsRepository.settings.collectAsStateWithLifecycle()

            val fontScale = when (settings.fontSize) {
                "SMALL" -> 0.9f
                "LARGE" -> 1.15f
                else -> 1.0f
            }

            val accentColor = runCatching {
                val hex = settings.accentColorHex.removePrefix("#")
                Color(("FF$hex").toLong(16))
            }.getOrDefault(LumenAccentDefault)

            val fontFamily = remember(settings.fontFamilyChoice) {
                try { LumenFontChoice.valueOf(settings.fontFamilyChoice).family }
                catch (_: Exception) { LumenFontChoice.SERIF_DISPLAY.family }
            }
            val textCase = remember(settings.textCaseChoice) {
                try { LumenTextCase.valueOf(settings.textCaseChoice) }
                catch (_: Exception) { LumenTextCase.LOWERCASE }
            }

            LumenTheme(
                accentColor = accentColor,
                fontFamily = fontFamily,
                textCase = textCase,
                weightOffset = settings.weightOffset
            ) {
                CompositionLocalProvider(
                    LocalDensity provides Density(
                        density = LocalDensity.current.density,
                        fontScale = LocalDensity.current.fontScale * fontScale
                    ),
                    LocalAccentColor provides accentColor
                ) {
                val ctx = LocalContext.current

                LumenNavHost(hasMediaPermission = MediaPermissions.isGranted(ctx))
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if (playerController.isPlaying) {
            playerController.pause()
        }
    }

    override fun onResume() {
        super.onResume()
        if (playerController.currentItemId == null) {
            playerController.stopAndClear()
        }
    }

    override fun onDestroy() {
        if (isFinishing) playerController.release()
        super.onDestroy()
    }
}
