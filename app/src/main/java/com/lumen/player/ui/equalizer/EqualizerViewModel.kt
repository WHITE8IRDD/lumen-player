package com.lumen.player.ui.equalizer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumen.player.domain.model.AudioEffectsState
import com.lumen.player.domain.repository.SettingsRepository
import com.lumen.player.player.equalizer.EqualizerController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EqUiState(
    val enabled: Boolean = false,
    val bandCount: Int = 0,
    val bandFreqs: List<Int> = emptyList(),
    val bandLevels: List<Short> = emptyList(),
    val minMb: Short = -1500,
    val maxMb: Short = 1500,
    val presets: List<String> = emptyList(),
    val selectedPreset: Int = 0,
    val bassBoost: Int = 0,
    val virtualizer: Int = 0,
    val loudnessMb: Int = 0
)

@HiltViewModel
class EqualizerViewModel @Inject constructor(
    private val controller: EqualizerController,
    private val settings: SettingsRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(EqUiState())
    val ui = _ui.asStateFlow()

    init { reload() }

    fun reload() {
        val bands = controller.bandCount
        val range = controller.bandLevelRange
        _ui.value = EqUiState(
            enabled = false,
            bandCount = bands,
            bandFreqs = (0 until bands).map { controller.centerFreqHz(it) },
            bandLevels = (0 until bands).map { controller.getBandLevel(it) },
            minMb = range.first,
            maxMb = range.second,
            presets = controller.presets()
        )
        viewModelScope.launch {
            val persisted = settings.settings.first().audioEffects
            if (persisted.enabled) {
                controller.applyState(persisted)
                _ui.value = EqUiState(
                    enabled = persisted.enabled,
                    bandCount = bands,
                    bandFreqs = (0 until bands).map { controller.centerFreqHz(it) },
                    bandLevels = (0 until bands).map { controller.getBandLevel(it) },
                    minMb = range.first,
                    maxMb = range.second,
                    presets = controller.presets(),
                    selectedPreset = persisted.preset,
                    bassBoost = persisted.bassBoost,
                    virtualizer = persisted.virtualizer,
                    loudnessMb = persisted.loudnessGainMb
                )
            }
        }
    }

    fun setEnabled(v: Boolean) {
        controller.setEnabled(v)
        _ui.value = _ui.value.copy(enabled = v)
        persist()
    }

    fun setBand(band: Int, levelMb: Short) {
        controller.setBandLevel(band, levelMb)
        _ui.value = _ui.value.copy(
            bandLevels = _ui.value.bandLevels.toMutableList().also { it[band] = levelMb }
        )
        persist()
    }

    fun usePreset(index: Int) {
        controller.usePreset(index)
        _ui.value = _ui.value.copy(
            selectedPreset = index,
            bandLevels = (0 until _ui.value.bandCount).map { controller.getBandLevel(it) }
        )
        persist()
    }

    fun setBassBoost(v: Int)   { controller.setBassBoost(v);   _ui.value = _ui.value.copy(bassBoost = v); persist() }
    fun setVirtualizer(v: Int) { controller.setVirtualizer(v); _ui.value = _ui.value.copy(virtualizer = v); persist() }
    fun setLoudness(mB: Int)   { controller.setLoudnessGainMb(mB); _ui.value = _ui.value.copy(loudnessMb = mB); persist() }

    private fun persist() {
        val s = _ui.value
        viewModelScope.launch {
            settings.setAudioEffects(
                AudioEffectsState(
                    enabled = s.enabled,
                    preset = s.selectedPreset,
                    bandLevels = s.bandLevels,
                    bassBoost = s.bassBoost,
                    virtualizer = s.virtualizer,
                    loudnessGainMb = s.loudnessMb
                )
            )
        }
    }
}
