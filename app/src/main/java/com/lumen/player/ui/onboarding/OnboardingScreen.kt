package com.lumen.player.ui.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch

// Stable amber for page indicator (immune to theme changes)
private val IndicatorAmber = Color(0xFFFFC857)

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()
    val interactionSource = remember { MutableInteractionSource() }

    var tmdbEnabled by remember { mutableStateOf(false) }
    var hwAccel by remember { mutableStateOf(true) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val allGranted = results.values.all { it }
        if (allGranted) {
            scope.launch { pagerState.animateScrollToPage(2) }
        } else {
            scope.launch { pagerState.animateScrollToPage(2) }
        }
    }

    val orbColors = listOf(Color(0xFFFFC857), Color(0xFFB4A7FF), Color(0xFFFFC857))
    val currentPage = pagerState.currentPage
    val offset = pagerState.currentPageOffsetFraction
    val fromColor = orbColors[currentPage.coerceIn(0, orbColors.lastIndex)]
    val toColor = orbColors[(currentPage + 1).coerceIn(0, orbColors.lastIndex)]
    val lerpedColor = lerp(fromColor, toColor, offset.coerceIn(0f, 1f))

    val orbOffsetY by animateFloatAsState(
        targetValue = when (pagerState.currentPage) {
            0 -> 0f
            1 -> -20f
            2 -> 10f
            else -> 0f
        },
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "orbY"
    )

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

            Spacer(Modifier.height(32.dp))
            GlowOrb(
                color = lerpedColor,
                modifier = Modifier.offset(y = orbOffsetY.dp)
            )
            Spacer(Modifier.height(16.dp))

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
                        tmdbEnabled = tmdbEnabled,
                        onTmdbChange = { tmdbEnabled = it },
                        hwAccel = hwAccel,
                        onHwAccelChange = { hwAccel = it },
                        onDone = { viewModel.complete(tmdbEnabled, hwAccel) }
                    )
                }
            }

            PageIndicator(
                pageCount = 3,
                currentPage = pagerState.currentPage,
                modifier = Modifier.padding(bottom = 40.dp)
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun GlowOrb(color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.size(220.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(220.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(color.copy(alpha = 0.18f), Color.Transparent)
                    ),
                    shape = CircleShape
                )
        )
        Box(
            modifier = Modifier
                .size(130.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            color.copy(alpha = 0.9f),
                            color.copy(alpha = 0.45f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )
    }
}

@Composable
private fun WelcomeSlide(onNext: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
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
        Box(
            modifier = Modifier
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onNext
                )
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 16.dp)
                .height(52.dp)
                .clip(RoundedCornerShape(26.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFFFD880), Color(0xFFFFC857))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text("Get Started", fontWeight = FontWeight.SemiBold, color = Color(0xFF1C1400), fontSize = 16.sp)
        }
    }
}

@Composable
private fun PermissionsSlide(onGrant: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(Modifier.height(48.dp))
        Text(
            "Grant media access",
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
        Box(
            modifier = Modifier
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onGrant
                )
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 16.dp)
                .height(52.dp)
                .clip(RoundedCornerShape(26.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFFFD880), Color(0xFFFFC857))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text("Grant Permissions", fontWeight = FontWeight.SemiBold, color = Color(0xFF1C1400), fontSize = 16.sp)
        }
    }
}

@Composable
private fun SetupSlide(
    tmdbEnabled: Boolean,
    onTmdbChange: (Boolean) -> Unit,
    hwAccel: Boolean,
    onHwAccelChange: (Boolean) -> Unit,
    onDone: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(48.dp))
        Text(
            "Customize your experience",
            style = MaterialTheme.typography.displayMedium,
            textAlign = TextAlign.Center,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(Modifier.height(40.dp))
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
        ) {
            Column(Modifier.padding(vertical = 8.dp)) {
                ToggleRow("Online Metadata", "Fetch movie posters from TMDB", tmdbEnabled, onTmdbChange)
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f), modifier = Modifier.padding(horizontal = 16.dp))
                ToggleRow("Hardware Acceleration", "Use hardware video decoding", hwAccel, onHwAccelChange)
            }
        }
        Spacer(Modifier.height(32.dp))
        Box(
            modifier = Modifier
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onDone
                )
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 16.dp)
                .height(52.dp)
                .clip(RoundedCornerShape(26.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFFFD880), Color(0xFFFFC857))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text("Done", fontWeight = FontWeight.SemiBold, color = Color(0xFF1C1400), fontSize = 16.sp)
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
        Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, color = Color.White)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
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
                targetValue = if (isActive) IndicatorAmber
                else Color.White.copy(alpha = 0.3f),
                label = "dotColor"
            )
            val width by animateDpAsState(targetValue = if (isActive) 20.dp else 6.dp, label = "dotWidth")
            Box(
                Modifier
                    .width(width)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(color)
            )
        }
    }
}
