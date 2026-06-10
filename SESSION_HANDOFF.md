# Lumen Player — Session Handoff (June 10, 2026)

## Current State

### What's Done
1. **Critical playback bug fixed** — `MediaItem.equals()`/`hashCode()` override by `id` only; 5 call-sites replaced `indexOf(item)` with `indexOfFirst { it.id == item.id }`. Verified on device.
2. **Regression checklist** — all 10 items passed
3. **Step 2 Folder Mosaics** — complete and verified on device
   - `FolderThumbnailType.kt` — sealed class (TmdbPoster, Mosaic, Single, Placeholder)
   - `FolderThumbnailResolver.kt` — resolves thumbnail type per folder with priority: TmdbPoster > Mosaic (4+) > Single (1-3) > Placeholder
   - `MosaicComposer.kt` — 2x2 Canvas composition
   - `MosaicFetcher.kt` — Coil Fetcher with SHA-256 disk cache (cache HIT confirmed for Black Doves, Blue Mountain State, Yuusha Party)
   - `LumenImageLoaderFactory.kt` — registered all 3 fetchers (VideoFrame, FolderThumbnail, Mosaic)
   - `VideoViews.kt` — FolderCard rewritten; FolderGrid renders multi-video folders as FolderCard, single-video as VideoGridCard
   - `LibraryScreen.kt` — videosByFolder computed via `items.groupBy { it.folderPath }`

### Device Verification Results
- **Mosaic thumbnails**: Cached HITs logged for all 3 multi-video folders
- **FolderCard layout**: Badge (video count) + clean title + "N videos · duration" — all rendering correctly
- **TmdbPoster layer**: Not yet triggered — requires user to enable "Fetch online metadata" in Settings (triggers `TmdbEnrichmentWorker`)

### Current Log Output (from 09:30 on device)
```
MosaicFetcher: Cache HIT for /storage/.../Black.Doves.S01...
MosaicFetcher: Cache HIT for /storage/.../Blue Mountain State...
MosaicFetcher: Cache HIT for /storage/.../Yuusha Party...
RealImageLoader: 💾 Successful (DISK) - MosaicFetcherInput(...)
```

## Blockers / Open Issues
- **TmdbEnrichmentWorker** only runs when user toggles "Fetch online metadata" ON in `SettingsViewModel.setFetchOnlineMetadata()` (line 38). Not run automatically after scan. To test TmdbPoster thumbnails: navigate to Settings → toggle "Fetch online metadata" ON.
- **HomeScreen `HomeFolderCard`** does NOT use `FolderThumbnailResolver` — only shows `folder.firstVideoUri` via `FolderThumbnailFetcher`. Extending would require passing video data per folder to the Home screen.

## How to Continue
1. Build: `.\gradlew.bat :app:assembleDebug`
2. Install: `adb install -r app\build\outputs\apk\debug\app-debug.apk` (adb at `C:\Users\m_oya\AppData\Local\Android\Sdk\platform-tools\adb.exe`)
3. Device: `9H4XS4T4WKPFAYKN`, package: `com.lumen.player.debug`, main activity: `com.lumen.player.MainActivity`
4. Launch: `adb shell am start -n com.lumen.player.debug/com.lumen.player.MainActivity`
5. Navigate: Library → Videos tab (Grid mode, group-by-folder ON by default). Scroll past single-video items to see FolderCards.
6. UI dump: `adb shell uiautomator dump /sdcard/ui.xml && adb shell cat /sdcard/ui.xml`
7. Log filter: `adb logcat -d --pid=$(adb shell pidof com.lumen.player.debug) | Select-String -Pattern "(Mosaic|FolderCard|FolderThumbnail|ThumbnailResolver|MosaicFetcher|MosaicComposer)"`
8. Screenshot: `adb exec-out screencap -p > file.png`

## Key Files
- `app/src/main/java/com/lumen/player/ui/library/VideoViews.kt` — FolderCard (line 534), FolderGrid (line 649)
- `app/src/main/java/com/lumen/player/ui/library/LibraryScreen.kt` — videosByFolder (line 229), FolderGrid call (line 416)
- `app/src/main/java/com/lumen/player/ui/util/FolderThumbnailResolver.kt` — resolution logic
- `app/src/main/java/com/lumen/player/ui/util/FolderThumbnailType.kt` — sealed class
- `app/src/main/java/com/lumen/player/ui/util/MosaicFetcher.kt` — Coil fetcher + cache
- `app/src/main/java/com/lumen/player/ui/util/MosaicComposer.kt` — 2x2 composition
- `app/src/main/java/com/lumen/player/ui/util/LumenImageLoaderFactory.kt` — fetcher registration
- `app/src/main/java/com/lumen/player/data/worker/TmdbEnrichmentWorker.kt` — TMDB enrichment
- `app/src/main/java/com/lumen/player/ui/settings/SettingsViewModel.kt` — enrichment trigger
