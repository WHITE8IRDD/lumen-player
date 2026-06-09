# Lumen Player

A premium, dark-first media player for Android built with **Jetpack Compose**, **Media3 / ExoPlayer**, **Hilt**, **Room**, and **Material You**.

## Highlights

- **Audio + Video** playback with a unified queue and a disjoint-ID MediaStore scanner
- **Media3**-backed `MediaSessionService` for lock-screen, Android Auto (in-app), Cast and `MediaController` UI
- **PiP**, gestures (volume / brightness / double-tap seek), and a fullscreen video surface
- **10-band equalizer**, bass boost, virtualizer, and loudness gain — persisted in DataStore
- **Material You** dynamic theming, with a custom violet/cyan night palette fallback
- **Clean Architecture**: `ui → viewmodel → usecase → repository → data` with Hilt
- **Room** for media metadata, playlists, and folder aggregates
- **DataStore** preferences for theme, resume, audio focus, gapless, skip-silence, EQ

## Tech Stack

| Layer       | Tools                                                              |
|-------------|--------------------------------------------------------------------|
| UI          | Jetpack Compose, Material 3, Adaptive layouts, Coil                |
| Player      | Media3 1.4.1 (ExoPlayer + UI + Datasource-okhttp + SmoothStreaming)|
| DI          | Hilt + KSP2                                                        |
| DB          | Room 2.6.x                                                         |
| Prefs       | DataStore Preferences                                              |
| Async       | Kotlin Coroutines + Flow                                           |
| Build       | Gradle 8.7, AGP 8.5.2, Kotlin 2.0.20, JDK 17, version catalog      |

## Build & Run

1. Open the project root in **Android Studio Ladybug (2024.2)** or newer.
2. From the project root run once:
   ```bash
   gradle wrapper --gradle-version 8.7
   ```
   This generates the `gradle-wrapper.jar` (the binary is intentionally not committed).
3. Sync Gradle, then `Run` ▶ `app` on a device or emulator with API 23+.

## Optional: FFmpeg extension renderer

The default `LumenRenderersFactory` enables decoder fallback and prefers the
official Media3 extensions. To enable FFmpeg-backed formats (FLAC, ALAC, etc.
in custom containers):

1. Build the Media3 FFmpeg AAR:
   ```bash
   git clone https://github.com/androidx/media.git
   cd media && ./gradlew :libraries:extension-ffmpeg:assembleRelease
   ```
2. Copy the produced AAR into `app/libs/media3-extension-ffmpeg-release.aar`.
3. In `app/build.gradle.kts`, uncomment the `files("libs/media3-extension-ffmpeg-release.aar")` line.
4. In `LumenRenderersFactory`, load it:
   ```kotlin
   val ffmpegRenderer = FfmpegAudioRenderer(
       FfmpegAudioProcessor.Builder().build(),
       /* ... */
   )
   setExtensionRendererMode(EXTENSION_RENDERER_MODE_PREFER)
   ```
5. Rebuild.

## Module Map

```
app/
└── src/main/java/com/lumen/player/
    ├── LumenApplication.kt          Hilt entry, notification channel
    ├── MainActivity.kt              Edge-to-edge host, permission check
    ├── core/
    │   ├── di/                      Hilt modules (App, Database, Player, Repository)
    │   ├── theme/                   Material You + violet/cyan palette
    │   └── util/                    Constants, time formatting, extensions
    ├── domain/                      Pure-Kotlin models, repository contracts, use-cases
    ├── data/                        Room, DataStore, MediaStore scanner, repository impls
    ├── player/                      ExoPlayer wiring, equalizer, Cast, notification, session
    └── ui/                          Compose screens, view-models, navigation
        ├── home/                    Scaffold + tabs
        ├── library/                 Music library + video grid
        ├── folders/                 Folder browser + detail
        ├── settings/                Theme, playback, library, audio
        ├── equalizer/               10-band EQ + bass/virt/loudness
        └── player/                  Full-screen audio player + video surface
```

## Permissions

| Permission | Why |
|------------|-----|
| `READ_MEDIA_AUDIO/VIDEO` (API 33+) | Library scan on modern Android |
| `READ_EXTERNAL_STORAGE` (API ≤ 32) | Legacy library scan |
| `POST_NOTIFICATIONS` (API 33+) | Media notification on Android 13+ |
| `FOREGROUND_SERVICE` / `FOREGROUND_SERVICE_MEDIA_PLAYBACK` | Background playback |
| `WAKE_LOCK` | Keep screen on for video |

## Theming

- `dynamicColor = true` follows Material You on Android 12+
- Falls back to a custom night-first palette: violet primary (`#9F7BFF`), cyan secondary (`#5EE7DF`)
- Light variant uses a soft cream background with the same accent

## License

For personal use. Wire up your own license before distribution.
