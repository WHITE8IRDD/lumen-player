package com.lumen.player.ui.player

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import com.lumen.player.core.util.Constants
import com.lumen.player.data.repository.MediaRepositoryImpl
import com.lumen.player.domain.repository.MediaRepository
import com.lumen.player.player.LyricsLine
import com.lumen.player.player.PlaybackState
import com.lumen.player.player.PlayerController
import com.lumen.player.ui.subtitles.LocalSubtitle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@UnstableApi
@HiltViewModel
class PlayerViewModel @Inject constructor(
    application: Application,
    private val controller: PlayerController,
    private val repository: MediaRepository
) : AndroidViewModel(application) {

    val state = controller.state
    val tracks = controller.tracks
    val lyrics = controller.lyrics

    private val _ui = MutableStateFlow(PlayerUiState())
    val ui = _ui.asStateFlow()

    val speeds = Constants.AVAILABLE_PLAYBACK_SPEEDS

    val exoPlayer get() = controller.getPlayer()

    init {
        // Tick position every 200ms for smooth scrub bar.
        viewModelScope.launch {
            while (true) {
                delay(Constants.PLAYER_POSITION_TICK_MS)
                controller.refreshPosition()
            }
        }
        // Save resume position every 10s while playing.
        viewModelScope.launch {
            while (true) {
                delay(10_000)
                val c = controller.getPlayer() ?: continue
                if (!c.isPlaying) continue
                val id = c.currentMediaItem?.mediaId?.toLongOrNull() ?: continue
                val pos = c.currentPosition
                if (pos <= 0) continue
                runCatching { repository.updateResumePosition(id, pos) }
            }
        }
    }

    /** Called when the user navigates away from the player; persist last position. */
    fun flushResumePosition() {
        viewModelScope.launch {
            val c = controller.getPlayer() ?: return@launch
            val id = c.currentMediaItem?.mediaId?.toLongOrNull() ?: return@launch
            val pos = c.currentPosition
            if (pos > 0) {
                runCatching { repository.updateResumePosition(id, pos) }
            }
        }
    }

    fun playPause()              = controller.playPause()
    fun next()                   = controller.next()
    fun previous()                = controller.previous()
    fun seekTo(ms: Long)         = controller.seekTo(ms)
    fun seekBy(ms: Long)         = controller.seekBy(ms)
    fun toggleShuffle()          = controller.toggleShuffle()
    fun setSpeed(speed: Float)   = controller.setSpeed(speed)

    /**
     * Sets playback speed with pitch correction. Media3's [PlaybackParameters]
     * keeps pitch = 1f by default so the audio stays natural at any speed
     * — no chipmunk effect, no bass drop. Instant, no buffering delay.
     */
    fun setSpeedPitched(speed: Float) {
        controller.setSpeedPitched(speed)
    }

    fun cycleRepeat() {
        val next = when (state.value.repeatMode) {
            androidx.media3.common.Player.REPEAT_MODE_OFF -> androidx.media3.common.Player.REPEAT_MODE_ALL
            androidx.media3.common.Player.REPEAT_MODE_ALL -> androidx.media3.common.Player.REPEAT_MODE_ONE
            else -> androidx.media3.common.Player.REPEAT_MODE_OFF
        }
        controller.setRepeatMode(next)
    }

    fun cyclePlaybackSpeed() {
        val current = state.value.playbackSpeed
        val idx = speeds.indexOfFirst { it == current }.takeIf { it >= 0 } ?: 3
        setSpeedPitched(speeds[(idx + 1) % speeds.size])
    }

    fun setRepeatMode(mode: Int) = controller.setRepeatMode(mode)
    fun setShuffle(on: Boolean) {
        if (controller.state.value.shuffle != on) controller.toggleShuffle()
    }

    fun showBrightnessHud(value: Float) {
        _ui.value = _ui.value.copy(brightness = value)
        scheduleHudClear()
    }
    fun setBrightness(value: Float) {
        _ui.value = _ui.value.copy(brightness = value.coerceIn(0f, 1f))
        scheduleHudClear()
    }
    fun adjustVolume(delta: Float) {
        val current = _ui.value.volume ?: 0.5f
        val updated = (current + delta).coerceIn(0f, 1f)
        _ui.value = _ui.value.copy(volume = updated)
        controller.setVolume(updated)
        scheduleHudClear()
    }
    fun setVolume(value: Float) {
        _ui.value = _ui.value.copy(volume = value.coerceIn(0f, 1f))
        controller.setVolume(value)
        scheduleHudClear()
    }
    fun currentVolume(): Float = _ui.value.volume ?: 0.5f

    private fun scheduleHudClear() {
        viewModelScope.launch {
            delay(1_200)
            _ui.value = _ui.value.copy(brightness = null, volume = null)
        }
    }

    fun toggleControlsOverlay() {
        _ui.value = _ui.value.copy(controlsVisible = !_ui.value.controlsVisible)
    }
    fun setControlsVisible(visible: Boolean) {
        _ui.value = _ui.value.copy(controlsVisible = visible)
    }

    fun setSleepTimer(minutes: Int) {
        if (minutes <= 0) {
            _ui.value = _ui.value.copy(sleepTimerMs = null)
            return
        }
        val target = System.currentTimeMillis() + minutes * 60_000L
        _ui.value = _ui.value.copy(sleepTimerMs = target)
        viewModelScope.launch {
            delay(minutes * 60_000L)
            controller.pause()
            _ui.value = _ui.value.copy(sleepTimerMs = null)
        }
    }

    fun toggleLock() {
        val newLocked = !_ui.value.locked
        _ui.value = _ui.value.copy(
            locked = newLocked,
            controlsVisible = if (newLocked) false else true
        )
    }

    fun loadLyrics(uri: Uri, mime: String?) {
        viewModelScope.launch {
            try {
                val ctx = getApplication<Application>()
                val lines = ctx.contentResolver.openInputStream(uri)?.use { input ->
                    val raw = input.bufferedReader().readText()
                    parseLrc(raw) ?: emptyList()
                } ?: emptyList()
                controller.setLyrics(lines)
            } catch (_: Throwable) {
                controller.setLyrics(emptyList())
            }
        }
    }

    fun setVolumeKey(delta: Float) = adjustVolume(delta)

    fun moveInQueue(from: Int, to: Int) = controller.moveQueueItem(from, to)
    fun removeFromQueue(index: Int) = controller.removeQueueItem(index)
    fun jumpTo(index: Int) = controller.jumpToQueueItem(index)
    fun queueItemAt(i: Int): MediaItem? = controller.queueItemAt(i)
    fun queueSize(): Int = controller.queueSize()
    fun currentQueueIndex(): Int = controller.currentQueueIndex()

    fun setAspectRatio(mode: AspectRatioMode) {
        _ui.value = _ui.value.copy(aspectRatio = mode)
    }

    fun setUiMode(mode: UiMode) {
        _ui.value = _ui.value.copy(uiMode = mode)
    }

    fun selectTrack(groupId: String?, trackIndex: Int) {
        controller.selectTrack(groupId, trackIndex)
    }

    /**
     * Adds a user-imported subtitle file to the now-playing item. Caller is
     * responsible for the URI permission (use ActivityResultContracts.OpenDocument
     * with persistable URI permissions).
     */
    fun addSubtitle(uri: Uri, mimeType: String) {
        controller.addSubtitle(uri, mimeType)
    }

    fun setSubtitleOffset(delayUs: Long) {
        controller.setSubtitleOffset(delayUs)
    }

    /**
     * Scan the directory containing the currently playing video for local
     * subtitle files matching the video name or a `ShowName.S01E01` style
     * pattern. Returns a list of [LocalSubtitle] sorted by language / format.
     */
    suspend fun scanLocalSubtitles(videoUri: Uri?): List<LocalSubtitle> =
        withContext(Dispatchers.IO) {
            if (videoUri == null) return@withContext emptyList()
            val path: String = if (videoUri.scheme == "file") videoUri.path!!
                else resolveContentPath(videoUri) ?: return@withContext emptyList()
            val videoFile = File(path)
            val parent = videoFile.parentFile ?: return@withContext emptyList()
            val base = stripExtension(videoFile.name)
            val siblingSeasonEp = seasonEpisodeTokens(base)
            val files = parent.listFiles() ?: return@withContext emptyList()
            files.asSequence()
                .filter { it.isFile }
                .filter { hasSubtitleExt(it.name) }
                .filter { f ->
                    val other = stripExtension(f.name)
                    if (other.equals(base, ignoreCase = true)) return@filter true
                    siblingSeasonEp?.let { (show, s, e) ->
                        val otherTokens = seasonEpisodeTokens(other)
                        if (otherTokens != null) {
                            val (oShow, oS, oE) = otherTokens
                            return@filter oShow.equals(show, ignoreCase = true) &&
                                oS == s && oE == e
                        }
                    }
                    false
                }
                .mapNotNull { f ->
                    val lang = languageFromFileName(f.nameWithoutExtension)
                    LocalSubtitle(
                        uri = Uri.fromFile(f),
                        filePath = f.absolutePath,
                        mimeType = mimeForExtension(f.extension),
                        language = lang,
                        fileName = f.name
                    )
                }
                .sortedWith(compareBy({ it.language }, { it.fileName }))
                .toList()
        }

    private fun resolveContentPath(uri: Uri): String? {
        return try {
            val proj = arrayOf(android.provider.MediaStore.Video.VideoColumns.DATA)
            getApplication<Application>().contentResolver
                .query(uri, proj, null, null, null)?.use { c ->
                    if (c.moveToFirst()) c.getString(0) else null
                }
        } catch (_: Exception) { null }
    }

    private fun stripExtension(name: String): String {
        val dot = name.lastIndexOf('.')
        return if (dot <= 0) name else name.substring(0, dot)
    }

    private fun hasSubtitleExt(name: String): Boolean {
        val ext = name.substringAfterLast('.', "").lowercase()
        return ext in setOf("srt", "ass", "ssa", "vtt", "sub")
    }

    /** Extract `(showName, season, episode)` from tokens like `Show.S01E01` */
    private fun seasonEpisodeTokens(name: String): Triple<String, Int, Int>? {
        val match = Regex("""^(.*?)\.S(\d{1,2})E(\d{1,2})$""", RegexOption.IGNORE_CASE)
            .find(name) ?: return null
        val (show, s, e) = match.destructured
        return Triple(show.trim(), s.toIntOrNull() ?: return null, e.toIntOrNull() ?: return null)
    }

    private fun languageFromFileName(name: String): String {
        // Common pattern: <name>.<lang>.<ext>
        val parts = name.split('.')
        if (parts.size >= 2) {
            val candidate = parts[parts.size - 2]
            if (candidate.length in 2..8 && candidate.all { it.isLetter() || it == '_' || it == '-' }) {
                return candidate.uppercase()
            }
        }
        return "UND"
    }

    private fun mimeForExtension(ext: String): String = when (ext.lowercase()) {
        "srt" -> "application/x-subrip"
        "vtt" -> "text/vtt"
        "ass", "ssa" -> "text/x-ssa"
        "sub" -> "application/x-subrip"
        else -> "application/x-subrip"
    }

    private fun parseLrc(text: String): List<LyricsLine>? {
        val pattern = Regex("""\[(\d+):(\d+(?:\.\d+)?)]""")
        val out = mutableListOf<LyricsLine>()
        text.lines().forEach { line ->
            val matches = pattern.findAll(line).toList()
            if (matches.isEmpty()) return@forEach
            val lyric = pattern.replace(line, "").trim()
            if (lyric.isEmpty()) return@forEach
            matches.forEach { m ->
                val mins = m.groupValues[1].toLong()
                val secs = m.groupValues[2].toDouble()
                out += LyricsLine(
                    timeMs = mins * 60_000L + (secs * 1000).toLong(),
                    text = lyric
                )
            }
        }
        return out.sortedBy { it.timeMs }.ifEmpty { null }
    }

    override fun onCleared() {
        super.onCleared()
        flushResumePosition()
    }
}

enum class AspectRatioMode { FIT, FILL, STRETCH, CROP }
enum class UiMode { MAIN, LYRICS, QUEUE, TRACKS, SPEED, ASPECT }

data class PlayerUiState(
    val brightness: Float? = null,
    val volume: Float? = null,
    val controlsVisible: Boolean = true,
    val sleepTimerMs: Long? = null,
    val locked: Boolean = false,
    val aspectRatio: AspectRatioMode = AspectRatioMode.FIT,
    val uiMode: UiMode = UiMode.MAIN
)
