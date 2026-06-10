package com.lumen.player

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.Density
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import com.lumen.player.core.theme.LumenTheme
import com.lumen.player.domain.repository.SettingsRepository
import com.lumen.player.domain.repository.ThemeMode
import com.lumen.player.player.PlayerController
import com.lumen.player.ui.components.PermissionDeniedDialog
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
                val lifecycle = LocalLifecycleOwner.current

                var granted by remember { mutableStateOf(MediaPermissions.isGranted(ctx)) }
                var showDenied by remember { mutableStateOf(false) }

                val launcher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestMultiplePermissions()
                ) { results ->
                    val allGranted = MediaPermissions.REQUIRED.all { results[it] == true }
                    granted = allGranted
                    if (!allGranted) showDenied = true
                }

                DisposableEffect(lifecycle) {
                    val observer = LifecycleEventObserver { _, event ->
                        if (event == Lifecycle.Event.ON_RESUME) {
                            val now = MediaPermissions.isGranted(ctx)
                            if (now != granted) {
                                granted = now
                                if (now) showDenied = false
                            }
                        }
                    }
                    lifecycle.lifecycle.addObserver(observer)
                    onDispose { lifecycle.lifecycle.removeObserver(observer) }
                }

                LumenNavHost(hasMediaPermission = granted)

                if (showDenied && !granted) {
                    PermissionDeniedDialog(
                        onOpenSettings = {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                .setData(Uri.fromParts("package", ctx.packageName, null))
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            ctx.startActivity(intent)
                        },
                        onDismiss = { showDenied = false }
                    )
                }
                }
            }
        }
    }

    override fun onDestroy() {
        if (isFinishing) playerController.release()
        super.onDestroy()
    }
}
