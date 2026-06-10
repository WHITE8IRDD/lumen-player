package com.lumen.player.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.lumen.player.core.util.Constants
import com.lumen.player.domain.model.AudioEffectsState
import com.lumen.player.domain.repository.ThemeMode
import com.lumen.player.domain.repository.UserSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

private val Context.prefsDataStore by preferencesDataStore(name = Constants.PREFS_NAME)
private val settingsScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val THEME_MODE        = stringPreferencesKey("theme_mode")
        val DYNAMIC_COLOR     = booleanPreferencesKey("dynamic_color")
        val RESUME_PLAYBACK   = booleanPreferencesKey("resume_playback")
        val AUDIO_FOCUS       = booleanPreferencesKey("respect_audio_focus")
        val SKIP_SILENCE      = booleanPreferencesKey("skip_silence")
        val GAPLESS           = booleanPreferencesKey("gapless_playback")
        val SHOW_VIDEOS       = booleanPreferencesKey("show_videos")
        val HARDWARE_ACCEL    = booleanPreferencesKey("hardware_acceleration")
        val SUBTITLE_DELAY_US = longPreferencesKey("subtitle_delay_us")
        val SUBTITLE_TEXT_SIZE = floatPreferencesKey("subtitle_text_size")

        val FETCH_ONLINE_METADATA = booleanPreferencesKey("fetch_online_metadata")
        val METADATA_WIFI_ONLY    = booleanPreferencesKey("metadata_wifi_only")

        val EQ_ENABLED        = booleanPreferencesKey("eq_enabled")
        val EQ_PRESET         = intPreferencesKey("eq_preset")
        val EQ_BAND_LEVELS    = stringPreferencesKey("eq_band_levels")
        val EQ_BASS           = intPreferencesKey("eq_bass")
        val EQ_VIRT           = intPreferencesKey("eq_virt")
        val EQ_LOUDNESS       = intPreferencesKey("eq_loudness")
    }

    val flow: Flow<UserSettings> = context.prefsDataStore.data.map { p ->
        UserSettings(
            themeMode = runCatching { ThemeMode.valueOf(p[Keys.THEME_MODE] ?: ThemeMode.SYSTEM.name) }
                .getOrDefault(ThemeMode.SYSTEM),
            dynamicColor = p[Keys.DYNAMIC_COLOR] ?: true,
            resumePlayback = p[Keys.RESUME_PLAYBACK] ?: true,
            respectAudioFocus = p[Keys.AUDIO_FOCUS] ?: true,
            skipSilence = p[Keys.SKIP_SILENCE] ?: false,
            gaplessPlayback = p[Keys.GAPLESS] ?: true,
            showVideos = p[Keys.SHOW_VIDEOS] ?: true,
            hardwareAcceleration = p[Keys.HARDWARE_ACCEL] ?: true,
            fetchOnlineMetadata = p[Keys.FETCH_ONLINE_METADATA] ?: false,
            metadataWifiOnly = p[Keys.METADATA_WIFI_ONLY] ?: true,
            subtitleDelayUs = p[Keys.SUBTITLE_DELAY_US] ?: 0L,
            subtitleTextSize = p[Keys.SUBTITLE_TEXT_SIZE] ?: 16f,
            audioEffects = AudioEffectsState(
                enabled = p[Keys.EQ_ENABLED] ?: false,
                preset = p[Keys.EQ_PRESET] ?: 0,
                bandLevels = p[Keys.EQ_BAND_LEVELS]
                    ?.split(',')
                    ?.mapNotNull { it.toShortOrNull() }
                    ?: emptyList(),
                bassBoost = p[Keys.EQ_BASS] ?: 0,
                virtualizer = p[Keys.EQ_VIRT] ?: 0,
                loudnessGainMb = p[Keys.EQ_LOUDNESS] ?: 0
            )
        )
    }

    val sharedFlow: StateFlow<UserSettings> = flow.stateIn(
        scope = settingsScope,
        started = SharingStarted.Eagerly,
        initialValue = UserSettings()
    )

    suspend fun setThemeMode(mode: ThemeMode) {
        context.prefsDataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun setDynamicColor(v: Boolean) {
        context.prefsDataStore.edit { it[Keys.DYNAMIC_COLOR] = v }
    }

    suspend fun setResumePlayback(v: Boolean) {
        context.prefsDataStore.edit { it[Keys.RESUME_PLAYBACK] = v }
    }

    suspend fun setRespectAudioFocus(v: Boolean) {
        context.prefsDataStore.edit { it[Keys.AUDIO_FOCUS] = v }
    }

    suspend fun setSkipSilence(v: Boolean) {
        context.prefsDataStore.edit { it[Keys.SKIP_SILENCE] = v }
    }

    suspend fun setGapless(v: Boolean) {
        context.prefsDataStore.edit { it[Keys.GAPLESS] = v }
    }

    suspend fun setShowVideos(v: Boolean) {
        context.prefsDataStore.edit { it[Keys.SHOW_VIDEOS] = v }
    }

    suspend fun setAudioEffects(state: AudioEffectsState) {
        context.prefsDataStore.edit { p ->
            p[Keys.EQ_ENABLED] = state.enabled
            p[Keys.EQ_PRESET]  = state.preset
            p[Keys.EQ_BAND_LEVELS] = state.bandLevels.joinToString(",")
            p[Keys.EQ_BASS]    = state.bassBoost
            p[Keys.EQ_VIRT]    = state.virtualizer
            p[Keys.EQ_LOUDNESS] = state.loudnessGainMb
        }
    }

    suspend fun setHardwareAcceleration(enabled: Boolean) {
        context.prefsDataStore.edit { it[Keys.HARDWARE_ACCEL] = enabled }
    }

    suspend fun setSubtitleDelayUs(delayUs: Long) {
        context.prefsDataStore.edit { it[Keys.SUBTITLE_DELAY_US] = delayUs }
    }

    suspend fun setSubtitleTextSize(size: Float) {
        context.prefsDataStore.edit { it[Keys.SUBTITLE_TEXT_SIZE] = size }
    }

    suspend fun setFetchOnlineMetadata(v: Boolean) {
        context.prefsDataStore.edit { it[Keys.FETCH_ONLINE_METADATA] = v }
    }

    suspend fun setMetadataWifiOnly(v: Boolean) {
        context.prefsDataStore.edit { it[Keys.METADATA_WIFI_ONLY] = v }
    }
}
