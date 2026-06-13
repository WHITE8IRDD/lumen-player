package com.lumen.player.ui.components

import android.graphics.BlurMaskFilter
import android.graphics.Paint as AndroidPaint
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lumen.player.core.theme.LumenAmber
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

enum class Mood {
    IDLE,
    HAPPY,
    SLEEPING,
    CURIOUS,
    EXCITED
}

@Composable
fun LumenBlob(
    modifier: Modifier = Modifier,
    size: Dp = 200.dp,
    animated: Boolean = true,
    glow: Boolean = true,
    mood: Mood = Mood.IDLE,
    color: Color = LumenAmber
) {
    val seed = remember { Random.nextInt() }
    val transition = rememberInfiniteTransition(label = "blob")

    val morphProgress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (mood) {
                    Mood.SLEEPING -> 4000
                    Mood.EXCITED -> 1200
                    else -> 2400
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "morph"
    )

    Canvas(modifier = modifier.defaultMinSize(160.dp).size(size)) {
            val canvasSize = this.size
            val centerX = canvasSize.width / 2f
            val centerY = canvasSize.height / 2f
            val baseRadius = canvasSize.minDimension * 0.35f
            val center = Offset(centerX, centerY)
            val t = if (animated) morphProgress else 0f

            if (glow) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            color.copy(alpha = 0.30f),
                            color.copy(alpha = 0.12f),
                            color.copy(alpha = 0f)
                        ),
                        center = center,
                        radius = baseRadius * 2.4f
                    ),
                    radius = baseRadius * 2.4f,
                    center = center
                )

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            color.copy(alpha = 0.50f),
                            color.copy(alpha = 0.20f),
                            color.copy(alpha = 0f)
                        ),
                        center = center,
                        radius = baseRadius * 1.6f
                    ),
                    radius = baseRadius * 1.6f,
                    center = center
                )
            }

            val bodyPath = buildOrganicPath(center, baseRadius, mood, t, seed)

            drawIntoCanvas { canvas ->
                val paint = AndroidPaint().apply {
                    isAntiAlias = true
                    this.color = color.copy(alpha = 0.92f).toArgb()
                    maskFilter = BlurMaskFilter(
                        baseRadius * 0.18f,
                        BlurMaskFilter.Blur.NORMAL
                    )
                }
                canvas.nativeCanvas.drawPath(bodyPath.asAndroidPath(), paint)
            }

            val corePath = buildOrganicPath(
                center = center,
                baseRadius = baseRadius * 0.85f,
                mood = mood,
                morphProgress = morphProgress,
                seed = seed
            )
            drawPath(
                path = corePath,
                color = color.copy(alpha = 0.55f)
            )
        }
    }

private fun moodControlPoints(mood: Mood): List<Float> {
    return when (mood) {
        Mood.IDLE -> listOf(1.0f, 0.98f, 1.02f, 0.96f, 1.0f, 1.04f, 0.98f, 1.02f)
        Mood.HAPPY -> listOf(1.08f, 1.04f, 1.0f, 0.92f, 1.1f, 0.96f, 1.02f, 1.06f)
        Mood.SLEEPING -> listOf(0.85f, 0.92f, 0.88f, 0.95f, 0.82f, 0.9f, 0.86f, 0.93f)
        Mood.CURIOUS -> listOf(0.9f, 1.06f, 1.2f, 0.88f, 0.85f, 1.02f, 0.88f, 1.1f)
        Mood.EXCITED -> listOf(1.15f, 0.88f, 1.2f, 0.8f, 1.25f, 0.85f, 1.1f, 1.12f)
    }
}

private fun buildOrganicPath(
    center: Offset,
    baseRadius: Float,
    mood: Mood,
    morphProgress: Float,
    seed: Int
): Path {
    val n = 8
    val angles = List(n) { i -> i * 2.0 * PI / n - PI / 2.0 }
    val moodRadii = moodControlPoints(mood)
    val random = Random(seed)

    val seededRadii = moodRadii.map { r ->
        r * (1f + 0.04f * (random.nextFloat() - 0.5f))
    }

    val r = seededRadii.map { it * baseRadius }.toMutableList()
    val t = morphProgress

    val smoothed = List(n) { i ->
        val prev = r[(i - 1 + n) % n]
        val curr = r[i]
        val next = r[(i + 1) % n]
        curr + 0.2f * (prev + next - 2f * curr)
    }

    val points = List(n) { i ->
        val a = angles[i] + 0.08 * sin(t * 2.0 * PI + i * 0.5)
        val rad = smoothed[i] * (1f + 0.04f * sin((t * 2.0 * PI + i * 0.7).toFloat()))
        Offset(
            center.x + rad * cos(a).toFloat(),
            center.y + rad * sin(a).toFloat()
        )
    }

    return Path().apply {
        moveTo(points[0].x, points[0].y)
        for (i in 0 until n) {
            val curr = points[i]
            val next = points[(i + 1) % n]
            val cp1 = Offset(
                curr.x + (next.x - points[(i - 1 + n) % n].x) * 0.12f,
                curr.y + (next.y - points[(i - 1 + n) % n].y) * 0.12f
            )
            val cp2 = Offset(
                next.x - (points[(i + 2) % n].x - curr.x) * 0.12f,
                next.y - (points[(i + 2) % n].y - curr.y) * 0.12f
            )
            cubicTo(cp1.x, cp1.y, cp2.x, cp2.y, next.x, next.y)
        }
        close()
    }
}

@Composable
fun LumenBlobPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000000)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("IDLE", style = MaterialTheme.typography.labelLarge, color = Color.White)
            Spacer(Modifier.height(8.dp))
            LumenBlob(size = 120.dp, mood = Mood.IDLE)
            Spacer(Modifier.height(24.dp))
            Text("HAPPY", style = MaterialTheme.typography.labelLarge, color = Color.White)
            Spacer(Modifier.height(8.dp))
            LumenBlob(size = 120.dp, mood = Mood.HAPPY)
            Spacer(Modifier.height(24.dp))
            Text("SLEEPING", style = MaterialTheme.typography.labelLarge, color = Color.White)
            Spacer(Modifier.height(8.dp))
            LumenBlob(size = 120.dp, mood = Mood.SLEEPING)
            Spacer(Modifier.height(24.dp))
            Text("CURIOUS", style = MaterialTheme.typography.labelLarge, color = Color.White)
            Spacer(Modifier.height(8.dp))
            LumenBlob(size = 120.dp, mood = Mood.CURIOUS)
            Spacer(Modifier.height(24.dp))
            Text("EXCITED", style = MaterialTheme.typography.labelLarge, color = Color.White)
            Spacer(Modifier.height(8.dp))
            LumenBlob(size = 120.dp, mood = Mood.EXCITED)
        }
    }
}
