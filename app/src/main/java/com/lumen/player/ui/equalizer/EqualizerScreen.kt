package com.lumen.player.ui.equalizer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lumen.player.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualizerScreen(
    onBack: () -> Unit,
    viewModel: EqualizerViewModel = hiltViewModel()
) {
    val state by viewModel.ui.collectAsStateWithLifecycle()

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
            }
            Spacer(Modifier.width(8.dp))
            Text(
                stringResource(R.string.equalizer),
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(Modifier.weight(1f))
            Switch(checked = state.enabled, onCheckedChange = viewModel::setEnabled)
        }

        if (state.bandCount == 0) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "Equalizer unavailable — start playback first.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            return@Column
        }

        // ---- Body of the equalizer: greys out + becomes non-interactive
        //      when the master switch is off, so the user can clearly see
        //      that the entire effect chain is bypassed. ----
        Column(
            Modifier
                .fillMaxWidth()
                .alpha(if (state.enabled) 1f else 0.4f)
        ) {
            EqBandSliders(enabled = state.enabled, state = state, onChange = viewModel::setBand)

            Spacer(Modifier.height(8.dp))

            if (state.presets.isNotEmpty()) {
                Text(
                    stringResource(R.string.preset),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp)
                )
                Row(
                    Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    state.presets.forEachIndexed { idx, name ->
                        FilterChip(
                            selected = idx == state.selectedPreset,
                            onClick = { if (state.enabled) viewModel.usePreset(idx) },
                            enabled = state.enabled,
                            label = { Text(name) },
                            colors = FilterChipDefaults.filterChipColors(
                                disabledSelectedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }
                }
            }

            HorizontalDivider(Modifier.padding(vertical = 8.dp))

            SliderRow(
                label = stringResource(R.string.bass_boost),
                value = state.bassBoost,
                range = 0..1000,
                onChange = viewModel::setBassBoost,
                enabled = state.enabled
            )
            SliderRow(
                label = stringResource(R.string.virtualizer),
                value = state.virtualizer,
                range = 0..1000,
                onChange = viewModel::setVirtualizer,
                enabled = state.enabled
            )
            SliderRow(
                label = stringResource(R.string.loudness),
                value = state.loudnessMb,
                range = 0..2000,
                onChange = viewModel::setLoudness,
                enabled = state.enabled
            )
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun EqBandSliders(
    enabled: Boolean,
    state: EqUiState,
    onChange: (Int, Short) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .padding(horizontal = 8.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        for (band in 0 until state.bandCount) {
            val current = state.bandLevels.getOrNull(band)?.toFloat() ?: 0f
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxHeight().width(48.dp)
            ) {
                Box(
                    Modifier
                        .weight(1f)
                        .graphicsLayer(rotationZ = -90f)
                ) {
                    Slider(
                        value = current,
                        onValueChange = { onChange(band, it.toInt().toShort()) },
                        valueRange = state.minMb.toFloat()..state.maxMb.toFloat(),
                        enabled = enabled,
                        modifier = Modifier.width(220.dp)
                    )
                }
                Text(
                    formatHz(state.bandFreqs.getOrNull(band) ?: 0),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SliderRow(
    label: String,
    value: Int,
    range: IntRange,
    onChange: (Int) -> Unit,
    enabled: Boolean
) {
    Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
            Text("${(value * 100 / range.last)}%", style = MaterialTheme.typography.labelMedium)
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onChange(it.toInt()) },
            valueRange = range.first.toFloat()..range.last.toFloat(),
            enabled = enabled
        )
    }
}

private fun formatHz(hz: Int): String =
    if (hz >= 1000) "${hz / 1000}k" else "$hz"
