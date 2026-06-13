package com.lumen.player.ui.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lumen.player.core.theme.LumenAmber
import com.lumen.player.core.theme.LumenLavender
import com.lumen.player.ui.components.LumenBlob
import com.lumen.player.ui.components.Mood
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch

// Onboarding-specific legacy palette (vibrant experience, not UI chrome)
private val OnboardingYellow = Color(0xFFFFD23F)
private val OnboardingBlue = Color(0xFF60A5FA)
private val OnboardingBlueDeep = Color(0xFF3B82F6)
private val OnboardingGreen = Color(0xFF34D399)
private val OnboardingGreenDeep = Color(0xFF10B981)
private val OnboardingPurpleDeep = Color(0xFF7C3AED)
private val OnboardingOrangeDeep = Color(0xFFEA580C)
private val OnboardingCyanDeep = Color(0xFF0891B2)

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

    val slideColors = listOf(OnboardingYellow, OnboardingBlue, OnboardingGreen)

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
                        Text("skip", color = Color.White.copy(alpha = 0.5f))
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
private fun WelcomeSlide(onNext: () -> Unit) {
    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        LumenBlob(modifier = Modifier.size(200.dp), color = LumenAmber, mood = Mood.IDLE)
        Spacer(Modifier.height(48.dp))
        Text(
            "welcome to Lumen",
            style = MaterialTheme.typography.displayLarge,
            textAlign = TextAlign.Center,
            color = Color.White
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "your local media, beautifully.",
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
                    spotColor = LumenAmber.copy(alpha = 0.6f),
                    ambientColor = LumenAmber.copy(alpha = 0.3f)
                ),
            shape = RoundedCornerShape(percent = 50),
            colors = ButtonDefaults.buttonColors(
                containerColor = LumenAmber,
                contentColor = Color.Black
            )
        ) {
            Text("get started", style = MaterialTheme.typography.labelLarge)
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
        LumenBlob(modifier = Modifier.size(200.dp), color = LumenLavender, mood = Mood.CURIOUS)
        Spacer(Modifier.height(48.dp))
        Text(
            "grant media access",
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
                    spotColor = LumenAmber.copy(alpha = 0.6f),
                    ambientColor = LumenAmber.copy(alpha = 0.3f)
                ),
            shape = RoundedCornerShape(percent = 50),
            colors = ButtonDefaults.buttonColors(
                containerColor = LumenAmber,
                contentColor = Color.Black
            )
        ) {
            Text("grant permissions", style = MaterialTheme.typography.labelLarge)
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
        LumenBlob(modifier = Modifier.size(200.dp), color = LumenAmber, mood = Mood.HAPPY)
        Spacer(Modifier.height(48.dp))
        Text(
            "customize your experience",
            style = MaterialTheme.typography.displayMedium,
            textAlign = TextAlign.Center,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(Modifier.height(40.dp))
        ToggleRow(
            title = "dynamic color",
            subtitle = "match your wallpaper colors (android 12+)",
            checked = dynamicColor,
            onToggle = onDynamicColorChange
        )
        ToggleRow(
            title = "online metadata",
            subtitle = "fetch movie posters from TMDB",
            checked = tmdbEnabled,
            onToggle = onTmdbChange
        )
        ToggleRow(
            title = "hardware acceleration",
            subtitle = "use hardware video decoding",
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
                    spotColor = LumenAmber.copy(alpha = 0.6f),
                    ambientColor = LumenAmber.copy(alpha = 0.3f)
                ),
            shape = RoundedCornerShape(percent = 50),
            colors = ButtonDefaults.buttonColors(
                containerColor = LumenAmber,
                contentColor = Color.Black
            )
        ) {
            Text("done", style = MaterialTheme.typography.labelLarge)
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
