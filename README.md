# Lumen

A premium local-first media player for Android with TMDB integration and IPTV streaming.

## Features

### Local Media
- Beautiful library for videos and audio
- Automatic folder grouping with mosaic thumbnails
- Smart filename parsing (cleans `Movie.2024.1080p.x265.mkv` to `Movie (2024)`)
- Resume playback where you left off
- Playlists with drag-to-reorder
- Long-press multi-select with bulk actions
- Select all / deselect all inside folders and the library
- Hide videos from Home with undo

### Premium Visuals
- TMDB integration for real movie/show posters
- True cool dark mode (no warm tones)
- Customizable accent colors with HEX input
- Inter typography
- Glassmorphism navigation
- 60fps Material 3 design

### Network Streaming
- HTTP/HTTPS stream playback
- M3U playlist import (IPTV)
- In-player channel switcher (VLC-style)
- Favorites with category grouping
- Grid and list views with channel logos

### Player
- Hardware-accelerated video (Media3 ExoPlayer)
- Subtitle support (embedded, local files, OpenSubtitles)
- Subtitle delay adjustment
- Equalizer (bass boost, virtualizer, loudness)
- Gesture controls (brightness, volume, seek)
- Volume boost up to 200% (VLC-style): swipe up past the maximum volume
- Picture-in-Picture
- Mini-player with notification controls
- Sleep timer

## Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose + Material 3
- **Architecture:** MVVM + Clean Architecture
- **DI:** Hilt
- **Database:** Room (9 migrations, data preserving)
- **Player:** Media3 ExoPlayer
- **Image Loading:** Coil
- **Networking:** OkHttp
- **Background Work:** WorkManager
- **Persistence:** DataStore
- **Min SDK:** 26 (Android 8.0)
- **Target SDK:** 35 (Android 15)

## Setup

### Prerequisites
- Android Studio Hedgehog or newer
- JDK 17
- Android SDK 35

### TMDB API Key (Optional)
For movie poster integration:
1. Sign up at https://www.themoviedb.org/signup
2. Get your v3 API key from https://www.themoviedb.org/settings/api
3. Add to `local.properties`:

```
TMDB_API_KEY=your_key_here
```

App works without a TMDB key — posters simply won't load.

### Build
```bash
git clone https://github.com/YOUR_USERNAME/lumen-player.git
cd lumen-player
./gradlew assembleRelease
```

APK output: `app/build/outputs/apk/release/app-universal-release.apk`

## Permissions

- `READ_MEDIA_VIDEO` / `READ_MEDIA_AUDIO` — scan local media
- `POST_NOTIFICATIONS` — playback notifications
- `INTERNET` — TMDB metadata, network streams
- `FOREGROUND_SERVICE` — background playback

## Architecture

```
app/src/main/java/com/lumen/player/
├── core/          # DI modules, theme, utilities
├── data/          # Room database, repositories, MediaStore scanner
├── domain/        # Domain models, repository interfaces
├── player/        # Media3 player, controller, playback service
├── ui/            # Compose screens, ViewModels, components
└── LumenApplication.kt
```

## Compatibility

Universal APK runs on all Android architectures:

- arm64-v8a (modern phones, 2017+)
- armeabi-v7a (older phones, 2014-2017)
- x86_64 (emulators)

Works on Android 8.0 (API 26) through Android 15 (API 35).

## License

All rights reserved. Personal use only.

## Acknowledgments

- TMDB for movie metadata
- OpenSubtitles for subtitle downloads
- Inter Font by Rasmus Andersson
