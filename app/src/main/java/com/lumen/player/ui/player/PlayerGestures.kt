package com.lumen.player.ui.player

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import kotlin.math.abs

@Composable
fun Modifier.playerGestures(
    locked: Boolean,
    onTapToggle: () -> Unit,
    onDoubleTapForward: () -> Unit,
    onDoubleTapBack: () -> Unit,
    onLongPressStart: () -> Unit,
    onLongPressEnd: () -> Unit,
    onVerticalLeft: (delta: Float) -> Unit,
    onVerticalRight: (delta: Float) -> Unit,
    onPinchZoom: (scale: Float) -> Unit
): Modifier {
    val w = LocalConfiguration.current.screenWidthDp.toFloat()
    val split = remember(w) { w / 2f }
    var longPressing by remember { mutableStateOf(false) }

    return this
        .pointerInput(locked, split) {
            if (locked) return@pointerInput
            detectTransformGestures { _, pan, zoom, _ ->
                if (zoom != 1f) {
                    onPinchZoom(zoom)
                } else if (abs(pan.y) > abs(pan.x) && abs(pan.y) > 0.5f) {
                    val fraction = -pan.y / 400f
                    if (pan.x > split) onVerticalRight(fraction) else onVerticalLeft(fraction)
                }
            }
        }
        .pointerInput(locked, split) {
            if (locked) return@pointerInput
            detectTapGestures(
                onTap = { onTapToggle() },
                onDoubleTap = { offset ->
                    if (offset.x > split) onDoubleTapForward() else onDoubleTapBack()
                },
                onLongPress = {
                    longPressing = true
                    onLongPressStart()
                },
                onPress = {
                    tryAwaitRelease()
                    if (longPressing) {
                        longPressing = false
                        onLongPressEnd()
                    }
                }
            )
        }
}
