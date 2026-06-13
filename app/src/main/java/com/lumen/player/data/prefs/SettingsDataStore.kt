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
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

private val Context.prefsDataStore by preferencesDataStore(name = Constants.PREFS_NAME)
private val settingsScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    init {
        // One-time migration: old teal default (#00E5C8) → new amber default (#FFC857)
        settingsScope.launch {
            context.prefsDataStore.edit { prefs ->
                val current = prefs[Keys.ACCENT_COLOR_HEX]
                if (current != null && current == "#00E5C8") {
                    prefs[Keys.ACCENT_COLOR_HEX] = "#FFC857"
                }
            }
        }
    }
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

        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")

        val FETCH_ONLINE_METADATA = booleanPreferencesKey("fetch_online_metadata")
        val METADATA_WIFI_ONLY    = booleanPreferencesKey("metadata_wifi_only")

        val FONT_SIZE        = stringPreferencesKey("font_size")
        val ASPECT_RATIO     = stringPreferencesKey("aspect_ratio")
        val BRIGHTNESS_MODE  = stringPreferencesKey("brightness_mode")
        val SUBTITLE_COLOR   = stringPreferencesKey("subtitle_color")
        val SUBTITLE_LANG    = stringPreferencesKey("subtitle_language")
        val OPENSUBTITLES_KEY = stringPreferencesKey("opensubtitles_api_key")
        val AUTO_SCAN        = booleanPreferencesKey("auto_scan_on_startup")
        val SHOW_AUDIO       = booleanPreferencesKey("show_audio")
        val REPLAY_GAIN      = booleanPreferencesKey("replay_gain")

        val ACCENT_COLOR_HEX = stringPreferencesKey("accent_color_hex")
        val ACCENT_MIGRATED = booleanPreferencesKey("accent_migrated_v3")

        val SEC_APPEARANCE  = booleanPreferencesKey("section_appearance")
        val SEC_PLAYBACK    = booleanPreferencesKey("section_playback")
        val SEC_VIDEO       = booleanPreferencesKey("section_video")
        val SEC_SUBTITLES   = booleanPreferencesKey("section_subtitles")
        val SEC_LIBRARY     = booleanPreferencesKey("section_library")
        val SEC_AUDIO       = booleanPreferencesKey("section_audio")
        val SEC_NETWORK     = booleanPreferencesKey("section_network")
        val SEC_ADVANCED    = booleanPreferencesKey("section_advanced")
        val SEC_ABOUT       = booleanPreferencesKey("section_about")

        val EQ_ENABLED        = booleanPreferencesKey("eq_enabled")
        val EQ_PRESET         = intPreferencesKey("eq_preset")
        val EQ_BAND_LEVELS    = stringPreferencesKey("eq_band_levels")
        val EQ_BASS           = intPreferencesKey("eq_bass")
        val EQ_VIRT           = intPreferencesKey("eq_virt")
        val EQ_LOUDNESS       = intPreferencesKey("eq_loudness")

        val TYPOGRAPHY_FONT_FAMILY = stringPreferencesKey("typography_font_family")
        val TYPOGRAPHY_CASE       = stringPreferencesKey("typography_case")
        val TYPOGRAPHY_WEIGHT     = intPreferencesKey("typography_weight_offset")
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
            onboardingCompleted = p[Keys.ONBOARDING_COMPLETED] ?: false,
            accentColorHex = p[Keys.ACCENT_COLOR_HEX] ?: "#FFC857",
            fontSize = p[Keys.FONT_SIZE] ?: "DEFAULT",
            aspectRatio = p[Keys.ASPECT_RATIO] ?: "AUTO",
            brightnessMode = p[Keys.BRIGHTNESS_MODE] ?: "SYSTEM",
            subtitleColor = p[Keys.SUBTITLE_COLOR] ?: "WHITE",
            subtitleLanguage = p[Keys.SUBTITLE_LANG] ?: "English",
            opensubtitlesApiKey = p[Keys.OPENSUBTITLES_KEY] ?: "",
            autoScanOnStartup = p[Keys.AUTO_SCAN] ?: true,
            showAudio = p[Keys.SHOW_AUDIO] ?: true,
            replayGain = p[Keys.REPLAY_GAIN] ?: false,
            sectionAppearance = p[Keys.SEC_APPEARANCE] ?: true,
            sectionPlayback = p[Keys.SEC_PLAYBACK] ?: true,
            sectionVideo = p[Keys.SEC_VIDEO] ?: false,
            sectionSubtitles = p[Keys.SEC_SUBTITLES] ?: false,
            sectionLibrary = p[Keys.SEC_LIBRARY] ?: false,
            sectionAudio = p[Keys.SEC_AUDIO] ?: false,
            sectionNetwork = p[Keys.SEC_NETWORK] ?: false,
            sectionAdvanced = p[Keys.SEC_ADVANCED] ?: false,
            sectionAbout = p[Keys.SEC_ABOUT] ?: false,
            fontFamilyChoice = p[Keys.TYPOGRAPHY_FONT_FAMILY] ?: "SERIF_DISPLAY",
            textCaseChoice = p[Keys.TYPOGRAPHY_CASE] ?: "LOWERCASE",
            weightOffset = p[Keys.TYPOGRAPHY_WEIGHT] ?: 0,
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

    suspend fun setOnboardingCompleted(v: Boolean) {
        context.prefsDataStore.edit { it[Keys.ONBOARDING_COMPLETED] = v }
    }

    suspend fun setAccentColorHex(v: String) {
        context.prefsDataStore.edit { it[Keys.ACCENT_COLOR_HEX] = v }
    }

    suspend fun setFontSize(v: String) {
        context.prefsDataStore.edit { it[Keys.FONT_SIZE] = v }
    }

    suspend fun setAspectRatio(v: String) {
        context.prefsDataStore.edit { it[Keys.ASPECT_RATIO] = v }
    }

    suspend fun setBrightnessMode(v: String) {
        context.prefsDataStore.edit { it[Keys.BRIGHTNESS_MODE] = v }
    }

    suspend fun setSubtitleColor(v: String) {
        context.prefsDataStore.edit { it[Keys.SUBTITLE_COLOR] = v }
    }

    suspend fun setSubtitleLanguage(v: String) {
        context.prefsDataStore.edit { it[Keys.SUBTITLE_LANG] = v }
    }

    suspend fun setOpensubtitlesApiKey(v: String) {
        context.prefsDataStore.edit { it[Keys.OPENSUBTITLES_KEY] = v }
    }

    suspend fun setAutoScanOnStartup(v: Boolean) {
        context.prefsDataStore.edit { it[Keys.AUTO_SCAN] = v }
    }

    suspend fun setShowAudio(v: Boolean) {
        context.prefsDataStore.edit { it[Keys.SHOW_AUDIO] = v }
    }

    suspend fun setReplayGain(v: Boolean) {
        context.prefsDataStore.edit { it[Keys.REPLAY_GAIN] = v }
    }

    suspend fun setFontFamilyChoice(v: String) {
        context.prefsDataStore.edit { it[Keys.TYPOGRAPHY_FONT_FAMILY] = v }
    }

    suspend fun setTextCaseChoice(v: String) {
        context.prefsDataStore.edit { it[Keys.TYPOGRAPHY_CASE] = v }
    }

    suspend fun setWeightOffset(v: Int) {
        context.prefsDataStore.edit { it[Keys.TYPOGRAPHY_WEIGHT] = v }
    }

    suspend fun setSectionExpanded(section: String, expanded: Boolean) {
        val key = when (section) {
            "appearance" -> Keys.SEC_APPEARANCE
            "playback" -> Keys.SEC_PLAYBACK
            "video" -> Keys.SEC_VIDEO
            "subtitles" -> Keys.SEC_SUBTITLES
            "library" -> Keys.SEC_LIBRARY
            "audio" -> Keys.SEC_AUDIO
            "network" -> Keys.SEC_NETWORK
            "advanced" -> Keys.SEC_ADVANCED
            "about" -> Keys.SEC_ABOUT
            else -> return
        }
        context.prefsDataStore.edit { it[key] = expanded }
    }
}
