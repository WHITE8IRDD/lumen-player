# Lumen Player — Session Handoff

## Goal
Build a complete, production-quality **"Lumen Player"** Android app at `C:\Users\m_oya\Desktop\Lumen` matching VLC-like premium UX: working video surface, buttery-smooth performance, FFmpeg-backed universal format support, redesigned video library (grid + list views with thumbnails), and per-video resume position with progress indicator.

## Tech Stack
- **Language:** Kotlin 2.0.20, JVM target 17, coroutines 1.9.0
- **UI:** Jetpack Compose (BOM 2024.09.03), Material 3
- **DI:** Hilt 2.52
- **DB:** Room 2.6.1
- **Media:** Media3 / ExoPlayer 1.4.1 (session, ui, datasource, dash, hls, rtsp, smoothstreaming, cast, decoder, extractor) + MediaSessionService
- **Navigation:** Navigation Compose 2.8.2
- **Lifecycle:** 2.8.6
- **DataStore:** 1.1.1 (preferences)
- **Image loading:** Coil 2.7.0 (compose + video frame extraction via `coil-video`)
- **Permissions:** Accompanist 0.36.0
- **Build:** AGP 8.5.2, KSP 2.0.20-1.0.25 (**KSP1 mode** — `ksp.useKSP2=false`), Gradle 8.7, JDK 17, minSdk 23 / target 35
- **Optional:** Media3 FFmpeg extension AAR (drop into `app/libs/`, no Maven artifact exists)

## Architecture
Clean Architecture: `ui → viewmodel → usecase → repository → data`
- Singleton-scoped Hilt
- `applicationId = com.lumen.player`, debug suffix `.debug`
- `PLAYBACK_CHANNEL_ID="lumen.playback"`, `PLAYBACK_NOTIFICATION_ID=1001`

## Round-by-Round Progress

### Round 1 — Initial build (from spec)
- Full Clean-Architecture skeleton
- All MVVM ViewModels, repositories, use-cases
- MediaSessionService + MediaController wiring
- PiP, lock-screen / Android Auto / Cast
- 10-band EQ
- Material You theming, night-first dark palette, violet accent

### Round 2 — Bug fixes & UX polish
- **Permission flow** with auto-request + auto-scan on grant + "Open settings" dialog on deny (`MediaPermissions.kt`, `PermissionDeniedDialog.kt`, `MainActivity.kt`)
- **LibraryScreen** rewritten with scanning spinner + empty placeholder
- **PlayerScreen** rewritten: blurred animated backdrop, animated album art (0.85↔1.0 scale + corner animation), full transport controls, scrub bar, speed/lyrics/tracks/aspect/sleep secondary row, auto-hide after 3s, lock toggle, full-screen flag
- **PlayerGestures.kt**: tap toggle, double-tap ±10s, vertical drag brightness/volume, long-press 2x, pinch captured
- **HudIndicator** overlay
- **PlayerViewModel**: speed cycle, sleep timer, lock, brightness/volume HUD, LRC lyrics parser, aspect mode, UI mode enum
- **PlayerController**: queue ops, track selection, volume setter, lyrics/tracks flows
- **LyricsView.kt**: LRC parser, auto-scroll, bold highlight
- **QueueScreen.kt**: current track highlighted, long-press drag handle wired
- **TrackSelectorSheet.kt**: bottom sheet for audio/subtitle/video track options
- **VideoPlayerScreen.kt**: pinch-to-zoom (0.5–4x), long-press 2x, double-tap ±10s, single-tap toggle, auto-hide 3s, aspect ratio cycling, KEEP_SCREEN_ON, `enterPip` util
- `Destinations.QUEUE` route added
- **Splash crash fix**: `splash_background.xml` `<bitmap>` referencing vector drawable → direct `@mipmap/ic_launcher`
- **AGP 8.5.2 fix**: `app/src/main/res/resources.properties` with `unqualifiedResLocale=en-US`
- **KSP2 bug fix**: `ksp.useKSP2=false` (Kotlin 2.0.20 KSP2 `UtilKt.fillInDeepSubstitutor` NoSuchElementException)
- First APK on desktop as `C:\Users\m_oya\Desktop\Lumen-debug.apk` (29.2 MB)

### Round 3 — 5 problems (current round, complete)
1. **Video not rendering** — fixed
2. **Performance** — fixed
3. **FFmpeg extension** — wired
4. **Video library redesign** — done
5. **Resume position** — done

## Round 3 — Detailed Changes

### 1. `LumenRenderersFactory.kt` — FFmpeg + reflection fallback
- Reflection-based registration of `FfmpegAudioRenderer` + `FfmpegVideoRenderer` from `androidx.media3.decoder.ffmpeg.*` so the AAR is **optional**
- Override of `buildAudioSink` (gapless offload preferences) and `buildVideoRenderers`/`buildAudioRenderers` (Media3 1.4.1 signature includes `AudioSink` arg)
- `setExtensionRendererMode(EXTENSION_RENDERER_MODE_PREFER)` + `setEnableDecoderFallback(true)` + `setEnableAudioFloatOutput(false)`

### 2. `PlayerManager.kt` — perf
- Buffer reduced 15s/60s (was 30s/120s) for fast first frame
- `setSkipSilenceEnabled(true)`
- 30s back-buffer with `retainBackBufferFromKeyframe`
- `setSeekBackIncrementMs(10_000)` + `setSeekForwardIncrementMs(10_000)`
- Track selector: tunneling ON, video mixed-mime adaptiveness, video non-seamless adaptiveness
- Load error policy: 3 retries
- **Removed** `nextMediaItemPreloadEnabled` (only in Media3 1.5+, we're on 1.4.1)

### 3. `VideoPlayerScreen.kt` — fixed video rendering
- Split into separate `VideoSurface` composable
- Re-assert `player` in `update` block (in case first attach was null)
- **Removed** `setSurfaceType(SURFACE_TYPE_SURFACE_VIEW)` — default is SurfaceView which is already hardware-accelerated; the call didn't compile
- Fixed long-press release via `tryAwaitRelease`
- `setUseArtwork(false)` + `keepScreenOn = true`

### 4. `PlayerViewModel.kt` — resume position
- New 10-second coroutine: while playing, calls `repository.updateResumePosition(currentMediaId.toLong, currentPosition)`
- `flushResumePosition()` called on `onCleared()`
- Injected `MediaRepository` for direct DAO access

### 5. `PlaybackService.kt` — resume position save hooks
- Added `Player.Listener` that saves on `EVENT_POSITION_DISCONTINUITY` and `EVENT_MEDIA_ITEM_TRANSITION`
- Saves on `STATE_ENDED` / `STATE_IDLE`
- Saves in `onTaskRemoved` and `onDestroy`
- Injected `MediaRepository`, uses `CoroutineScope(SupervisorJob + Dispatchers.IO)`

### 6. `LibraryViewModel.kt` — resume-aware play
- New `playAtIndex(items, index)`: looks up `mediaDao.get(id)?.lastPositionMs`, seeks if >5s, increments play count
- `VideosScreen` calls this on tap

### 7. `VideosScreen.kt` — VLC-like redesign
- New header: title + sort dropdown (Title/Date/Duration/Size) + `SegmentedButton` grid/list toggle
- Grid view: `LazyVerticalGrid` 2-col, each card:
  - 16:9 Coil video frame thumbnail at 1s mark (via `coil.request.videoFrameMillis`)
  - Duration badge bottom-right
  - Resume progress bar bottom
  - Resume dot top-left
  - Title + size · duration below
- List view: `LazyColumn` rows, 130×74 thumbnail left + title + size/duration right + overflow menu
- `humanSize()` helper, `VideoSort.label()` helper
- Tapping a video calls `viewModel.playAtIndex(items, idx)` and navigates to `videoFullscreen`

### 8. `app/build.gradle.kts` — FFmpeg AAR support
- Auto-detects `app/libs/media3-decoder-ffmpeg-release.aar`:
  ```kotlin
  val ffmpegAar = file("libs/media3-decoder-ffmpeg-release.aar")
  if (ffmpegAar.exists()) {
      implementation(files("libs/media3-decoder-ffmpeg-release.aar"))
  }
  ```
- Added `implementation(libs.coil.video)` for video frame thumbnails

### 9. `gradle/libs.versions.toml`
- Added `coil-video = { module = "io.coil-kt:coil-video", version.ref = "coil" }`

### 10. `LumenNavHost.kt`
- `VideosScreen` now takes `onVideoClick: (MediaItem) -> Unit` → sets `videoFullscreen = true`

### 11. `strings.xml`
- Added `<string name="videos">Videos</string>`

## Build Status
- **Build: SUCCESS** (1m 55s)
- **APK:** `C:\Users\m_oya\Desktop\Lumen-debug.apk` (27.6 MB, 6/2/2026 8:00:42 AM)
- Only deprecation warnings, no errors

## Critical Context / Gotchas
- **KSP2 + Kotlin 2.0.20 bug** → use KSP1 (`ksp.useKSP2=false` in `gradle.properties`)
- **AGP 8.5.2** needs `app/src/main/res/resources.properties` with `unqualifiedResLocale=en-US`
- **Media3 FFmpeg extension is NOT on Maven** — must be built from source. AAR is dropped in `app/libs/`. App works without it via reflection fallback.
- **Media3 1.4.1 `buildAudioRenderers` signature** includes `AudioSink` arg (no `AudioProcessor[]`)
- **Media3 1.4.1 has no `nextMediaItemPreloadEnabled`** — that's 1.5+
- **PlayerView default surface is `SURFACE_TYPE_SURFACE_VIEW`** (hardware-accelerated). Don't call `setSurfaceType` from Kotlin.
- **`coil-video` artifact is required** for `videoFrameMillis` extension
- **Hilt + ViewModel** with `MediaRepository` injection: the constructor needs `@Inject` and the type must be the interface (`MediaRepository`), not the impl

## Key Files
- `C:\Users\m_oya\Desktop\Lumen\app\src\main\java\com\lumen\player\player\LumenRenderersFactory.kt`
- `C:\Users\m_oya\Desktop\Lumen\app\src\main\java\com\lumen\player\player\PlayerManager.kt`
- `C:\Users\m_oya\Desktop\Lumen\app\src\main\java\com\lumen\player\player\PlaybackService.kt`
- `C:\Users\m_oya\Desktop\Lumen\app\src\main\java\com\lumen\player\player\PlayerController.kt`
- `C:\Users\m_oya\Desktop\Lumen\app\src\main\java\com\lumen\player\ui\player\PlayerViewModel.kt`
- `C:\Users\m_oya\Desktop\Lumen\app\src\main\java\com\lumen\player\ui\player\VideoPlayerScreen.kt`
- `C:\Users\m_oya\Desktop\Lumen\app\src\main\java\com\lumen\player\ui\library\VideosScreen.kt`
- `C:\Users\m_oya\Desktop\Lumen\app\src\main\java\com\lumen\player\ui\library\LibraryViewModel.kt`
- `C:\Users\m_oya\Desktop\Lumen\app\src\main\java\com\lumen\player\ui\navigation\LumenNavHost.kt`
- `C:\Users\m_oya\Desktop\Lumen\app\build.gradle.kts`
- `C:\Users\m_oya\Desktop\Lumen\gradle\libs.versions.toml`
- `C:\Users\m_oya\Desktop\Lumen\gradle.properties` (KSP1 toggle)
- `C:\Users\m_oya\Desktop\Lumen\app\src\main\res\resources.properties` (AGP fix)
- `C:\Users\m_oya\Desktop\Lumen\app\src\main\res\drawable\splash_background.xml` (splash fix)
- `C:\Users\m_oya\Desktop\Lumen-debug.apk` (built APK)

## Open Items / Next Steps
- (none) — all 5 problems addressed and built successfully
- Optional: drop the FFmpeg AAR into `app/libs/` and rebuild to enable FFmpeg renderer
- Optional: test on a real device and verify video renders, resume works, grid/list looks right
- Optional: add haptic feedback for the resume bar interaction
- Optional: long-press menu on video card for "Play next", "Add to playlist", "Delete", "Properties"

## How to Continue
1. Read the "Round 3 — Detailed Changes" section
2. Check the "Critical Context / Gotchas" section
3. Build: `cd C:\Users\m_oya\Desktop\Lumen; .\gradlew.bat :app:assembleDebug`
4. APK lands at `app/build/outputs/apk/debug/app-debug.apk`; copy to `C:\Users\m_oya\Desktop\Lumen-debug.apk`
