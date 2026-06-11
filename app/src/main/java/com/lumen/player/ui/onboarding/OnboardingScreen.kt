package com.lumen.player.ui.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lumen.player.core.theme.LumenBlue
import com.lumen.player.core.theme.LumenBlueDeep
import com.lumen.player.core.theme.LumenCyanDeep
import com.lumen.player.core.theme.LumenGreen
import com.lumen.player.core.theme.LumenGreenDeep
import com.lumen.player.core.theme.LumenOrangeDeep
import com.lumen.player.core.theme.LumenPurpleDeep
import com.lumen.player.core.theme.LumenYellow
import com.lumen.player.ui.components.LumenPrimaryButton
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()

    var dynamicColor by remember { mutableStateOf(false) }
    var tmdbEnabled by remember { mutableStateOf(false) }
    var hwAccel by remember { mutableStateOf(true) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        scope.launch { pagerState.animateScrollToPage(2) }
    }

    val slideColors = listOf(LumenYellow, LumenBlue, LumenGreen)

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                Modifier.fillMaxWidth().padding(top = 48.dp, end = 16.dp),
                contentAlignment = Alignment.TopEnd
            ) {
                if (pagerState.currentPage < 2) {
                    TextButton(onClick = {
                        scope.launch { pagerState.animateScrollToPage(2) }
                    }) {
                        Text("Skip", color = Color.White.copy(alpha = 0.5f))
                    }
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                when (page) {
                    0 -> WelcomeSlide(onNext = {
                        scope.launch { pagerState.animateScrollToPage(1) }
                    })
                    1 -> PermissionsSlide(onGrant = {
                        val perms = mutableListOf<String>()
                        if (Build.VERSION.SDK_INT >= 33) {
                            perms.add(Manifest.permission.READ_MEDIA_VIDEO)
                            perms.add(Manifest.permission.READ_MEDIA_AUDIO)
                            perms.add(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            perms.add(Manifest.permission.READ_EXTERNAL_STORAGE)
                        }
                        permissionLauncher.launch(perms.toTypedArray())
                    })
                    2 -> SetupSlide(
                        dynamicColor = dynamicColor,
                        onDynamicColorChange = { dynamicColor = it },
                        tmdbEnabled = tmdbEnabled,
                        onTmdbChange = { tmdbEnabled = it },
                        hwAccel = hwAccel,
                        onHwAccelChange = { hwAccel = it },
                        onDone = { viewModel.complete(dynamicColor, tmdbEnabled, hwAccel) }
                    )
                }
            }

            PageIndicator(
                pageCount = 3,
                currentPage = pagerState.currentPage,
                activeColor = slideColors[pagerState.currentPage],
                modifier = Modifier.padding(bottom = 40.dp)
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun OnboardingBlob(
    gradientStart: Color,
    gradientEnd: Color,
    centerIcon: ImageVector,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.size(270.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        gradientStart.copy(alpha = 0.4f),
                        gradientStart.copy(alpha = 0f)
                    ),
                    radius = size.minDimension
                ),
                radius = size.minDimension / 2
            )
        }
        Box(
            modifier = Modifier
                .size(180.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(gradientStart, gradientEnd)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = centerIcon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(72.dp)
            )
        }
    }
}

@Composable
private fun WelcomeSlide(onNext: () -> Unit) {
    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(240.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            LumenYellow.copy(alpha = 0.4f),
                            LumenYellow.copy(alpha = 0f)
                        )
                    ),
                    radius = size.minDimension / 1.5f
                )
            }
            Image(
                painter = painterResource(com.lumen.player.R.drawable.lumen_logo_rounded),
                contentDescription = "Lumen",
                modifier = Modifier.size(180.dp)
            )
        }
        Spacer(Modifier.height(48.dp))
        Text(
            "Welcome to Lumen",
            style = MaterialTheme.typography.displayLarge,
            textAlign = TextAlign.Center,
            color = Color.White
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "Your local media, beautifully.",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(48.dp))
        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .height(56.dp)
                .shadow(
                    elevation = 20.dp,
                    shape = RoundedCornerShape(percent = 50),
                    spotColor = LumenYellow.copy(alpha = 0.6f),
                    ambientColor = LumenYellow.copy(alpha = 0.3f)
                ),
            shape = RoundedCornerShape(percent = 50),
            colors = ButtonDefaults.buttonColors(
                containerColor = LumenYellow,
                contentColor = Color.Black
            )
        ) {
            Text("Get Started", style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun PermissionsSlide(onGrant: () -> Unit) {
    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        OnboardingBlob(
            gradientStart = LumenBlue,
            gradientEnd = LumenPurpleDeep,
            centerIcon = Icons.Rounded.Folder
        )
        Spacer(Modifier.height(48.dp))
        Text(
            "Grant Media Access",
            style = MaterialTheme.typography.displayLarge,
            textAlign = TextAlign.Center,
            color = Color.White
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "Lumen needs permission to read videos and audio on your device. We also use notifications for playback controls.",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
        Spacer(Modifier.height(48.dp))
        Button(
            onClick = onGrant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .height(56.dp)
                .shadow(
                    elevation = 20.dp,
                    shape = RoundedCornerShape(percent = 50),
                    spotColor = LumenBlue.copy(alpha = 0.6f),
                    ambientColor = LumenBlue.copy(alpha = 0.3f)
                ),
            shape = RoundedCornerShape(percent = 50),
            colors = ButtonDefaults.buttonColors(
                containerColor = LumenBlue,
                contentColor = Color.White
            )
        ) {
            Text("Grant Permissions", style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun SetupSlide(
    dynamicColor: Boolean,
    onDynamicColorChange: (Boolean) -> Unit,
    tmdbEnabled: Boolean,
    onTmdbChange: (Boolean) -> Unit,
    hwAccel: Boolean,
    onHwAccelChange: (Boolean) -> Unit,
    onDone: () -> Unit
) {
    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        OnboardingBlob(
            gradientStart = LumenGreen,
            gradientEnd = LumenCyanDeep,
            centerIcon = Icons.Rounded.Tune
        )
        Spacer(Modifier.height(48.dp))
        Text(
            "Customize Your Experience",
            style = MaterialTheme.typography.displayMedium,
            textAlign = TextAlign.Center,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(Modifier.height(40.dp))
        ToggleRow(
            title = "Dynamic Color",
            subtitle = "Match your wallpaper colors (Android 12+)",
            checked = dynamicColor,
            onToggle = onDynamicColorChange
        )
        ToggleRow(
            title = "Online Metadata",
            subtitle = "Fetch movie posters from TMDB",
            checked = tmdbEnabled,
            onToggle = onTmdbChange
        )
        ToggleRow(
            title = "Hardware Acceleration",
            subtitle = "Use hardware video decoding",
            checked = hwAccel,
            onToggle = onHwAccelChange
        )
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = onDone,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .height(56.dp)
                .shadow(
                    elevation = 20.dp,
                    shape = RoundedCornerShape(percent = 50),
                    spotColor = LumenGreen.copy(alpha = 0.6f),
                    ambientColor = LumenGreen.copy(alpha = 0.3f)
                ),
            shape = RoundedCornerShape(percent = 50),
            colors = ButtonDefaults.buttonColors(
                containerColor = LumenGreen,
                contentColor = Color.White
            )
        ) {
            Text("Done", style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun ToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = Color.White)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.5f)
            )
        }
        Spacer(Modifier.width(12.dp))
        Switch(checked = checked, onCheckedChange = onToggle)
    }
}

@Composable
private fun PageIndicator(
    pageCount: Int,
    currentPage: Int,
    activeColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { index ->
            val isActive = index == currentPage
            val color by animateColorAsState(
                targetValue = if (isActive) activeColor
                else Color.White.copy(alpha = 0.2f),
                label = "dot"
            )
            Box(
                Modifier
                    .size(if (isActive) 12.dp else 8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}
