package com.lumen.player

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity

import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
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

            val accentColor = runCatching {
                val hex = settings.accentColorHex.removePrefix("#")
                Color(("FF$hex").toLong(16))
            }.getOrDefault(Color(0xFFFFC857))

            val fontFamily = remember(settings.fontFamilyChoice) {
                try { LumenFontChoice.valueOf(settings.fontFamilyChoice).family }
                catch (_: Exception) { LumenFontChoice.SERIF_DISPLAY.family }
            }
            val textCase = remember(settings.textCaseChoice) {
                try { LumenTextCase.valueOf(settings.textCaseChoice) }
                catch (_: Exception) { LumenTextCase.LOWERCASE }
            }

            Log.d("LumenTheme", "Rendering with fontFamilyChoice=${settings.fontFamilyChoice} textCaseChoice=${settings.textCaseChoice} weightOffset=${settings.weightOffset}")

            LumenTheme(
                dynamicColor = settings.dynamicColor,
                accentColor = accentColor,
                fontFamily = fontFamily,
                textCase = textCase,
                weightOffset = settings.weightOffset
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
