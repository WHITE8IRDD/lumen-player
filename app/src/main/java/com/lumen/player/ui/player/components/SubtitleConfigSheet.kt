package com.lumen.player.ui.player.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lumen.player.R

data class SubtitleConfig(
    val trackId: Int? = null,
    val size: Float = 1f,        // 0.5..1.5
    val textColor: Int = 0xFFFFFFFF.toInt(),
    val bottomOffset: Float = 0.0f
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubtitleConfigSheet(
    availableTracks: List<String>,
    initial: SubtitleConfig = SubtitleConfig(),
    onDismiss: () -> Unit,
    onApply: (SubtitleConfig) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var config by remember { mutableStateOf(initial) }
    var trackMenuOpen by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Subtitles", style = MaterialTheme.typography.titleLarge)

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Track", modifier = Modifier.weight(1f))
                Text(
                    availableTracks.getOrNull(config.trackId ?: -1) ?: "Off",
                    modifier = Modifier
                        .padding(start = 12.dp)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Size", modifier = Modifier.weight(1f))
                Slider(
                    value = config.size,
                    onValueChange = { config = config.copy(size = it) },
                    valueRange = 0.5f..1.5f
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Position", modifier = Modifier.weight(1f))
                Slider(
                    value = config.bottomOffset,
                    onValueChange = { config = config.copy(bottomOffset = it) },
                    valueRange = 0f..1f
                )
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    "Apply",
                    modifier = Modifier
                        .padding(8.dp)
                )
            }
        }
    }
}
