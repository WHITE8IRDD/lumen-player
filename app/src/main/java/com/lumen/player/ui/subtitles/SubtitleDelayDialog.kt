package com.lumen.player.ui.subtitles

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun SubtitleDelayDialog(
    visible: Boolean,
    delayMs: Int,
    onDelayChange: (Int) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!visible) return

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .width(340.dp)
                .border(BorderStroke(1.dp, Color(0xFFFFAB40).copy(alpha = 0.35f)), RoundedCornerShape(16.dp)),
            color = Color(0xFF1C1C1C),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(Modifier.width(24.dp))
                    Text("Subtitle delay", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFFFAB40), modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Rounded.Close, "Close", tint = Color.White.copy(0.6f), modifier = Modifier.size(16.dp))
                    }
                }
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().background(Color.White.copy(0.05f), RoundedCornerShape(12.dp)).padding(vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { onDelayChange(delayMs - 50) }) {
                        Icon(Icons.Rounded.ChevronLeft, "Decrease", tint = Color.White, modifier = Modifier.size(28.dp))
                    }
                    Text("${if (delayMs >= 0) "+" else ""}$delayMs ms", fontSize = 18.sp, fontWeight = FontWeight.Medium, color = Color.White)
                    IconButton(onClick = { onDelayChange(delayMs + 50) }) {
                        Icon(Icons.Rounded.ChevronRight, "Increase", tint = Color.White, modifier = Modifier.size(28.dp))
                    }
                }
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { onReset(); onDismiss() }) { Text("Reset", color = Color(0xFFFFAB40), fontSize = 13.sp) }
                    Spacer(Modifier.width(8.dp))
                    TextButton(onClick = onDismiss) { Text("Done", color = Color.White, fontSize = 13.sp) }
                }
            }
        }
    }
}
