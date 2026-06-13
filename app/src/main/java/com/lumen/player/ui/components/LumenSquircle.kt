package com.lumen.player.ui.components

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import kotlin.math.min

class LumenSquircle(
    radius: Dp,
    private val smoothing: Float = 0.6f
) : CornerBasedShape(CornerSize(radius), CornerSize(radius), CornerSize(radius), CornerSize(radius)) {
    private val cornerRadius: Dp = radius

    override fun createOutline(
        size: Size,
        topLeft: Float,
        topRight: Float,
        bottomRight: Float,
        bottomLeft: Float,
        layoutDirection: LayoutDirection
    ): Outline {
        val r = min(topLeft, min(topRight, min(bottomRight, bottomLeft)))
            .coerceAtMost(min(size.width, size.height) / 2f)

        val s = r * (1f + smoothing)
        val sx = s.coerceAtMost(size.width / 2f)
        val sy = s.coerceAtMost(size.height / 2f)

        val path = Path().apply {
            moveTo(sx, 0f)
            lineTo(size.width - sx, 0f)
            cubicTo(
                size.width - sx * (1 - smoothing), 0f,
                size.width, sy * (1 - smoothing),
                size.width, sy
            )
            lineTo(size.width, size.height - sy)
            cubicTo(
                size.width, size.height - sy * (1 - smoothing),
                size.width - sx * (1 - smoothing), size.height,
                size.width - sx, size.height
            )
            lineTo(sx, size.height)
            cubicTo(
                sx * (1 - smoothing), size.height,
                0f, size.height - sy * (1 - smoothing),
                0f, size.height - sy
            )
            lineTo(0f, sy)
            cubicTo(
                0f, sy * (1 - smoothing),
                sx * (1 - smoothing), 0f,
                sx, 0f
            )
            close()
        }
        return Outline.Generic(path)
    }

    override fun copy(
        topLeft: CornerSize,
        topRight: CornerSize,
        bottomRight: CornerSize,
        bottomLeft: CornerSize
    ): LumenSquircle {
        return LumenSquircle(radius = cornerRadius, smoothing = smoothing)
    }

    override fun toString(): String {
        return "LumenSquircle(radius = $cornerRadius, smoothing = $smoothing)"
    }
}
