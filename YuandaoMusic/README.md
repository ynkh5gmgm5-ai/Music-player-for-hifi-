# Yuandao Music

Yuandao Music is an Android-first HiFi music player prototype for local libraries today and
private cloud or streaming sources later.

The first milestone focuses on a clean local playback foundation:

- Kotlin, Jetpack Compose, Media3, and Room.
- MediaStore scanning with SAF scanner hooks.
- Local tracks, albums, artists, audio format metadata, and cover indexing.
- Playback queue, repeat/shuffle, seek, and queue persistence.
- Media3 MediaSession service for background playback, notification surfaces, lock screen controls,
  and headset/media-button routing.
- Runtime playback quality display from Media3 audio format callbacks.
- Recently played history and no-repeat randomized queue order for shuffle playback.
- A first-pass now-playing sheet with artwork, metadata, progress, lyrics, and playback controls.
- Queue drawer ordering that exposes the real playback order, including shuffled queues.
- `.lrc` lyric parsing and synchronized lyric display hooks.
- Output-device discovery, with USB/exclusive output reserved for a later milestone.
- Source models for `LOCAL`, `CLOUD`, and `STREAMING`, even though only local playback is active.

Auxio is used only as an architectural reference. No GPL code is copied into this project.

## Roadmap

1. Local Android player: scan, index, play, persist, show lyrics, and verify WAV/FLAC runtime playback.
2. Local library polish: search, queue actions, file/folder management, richer local metadata, and visual refinement.
3. Advanced output: USB DAC, exclusive output, DSD/DoP research, and device-specific tuning.
4. Multi-source expansion: private cloud first, licensed streaming later.

## Project Docs

- [Architecture](docs/ARCHITECTURE.md)
- [Roadmap](docs/ROADMAP.md)
- [Project status](docs/PROJECT_STATUS.md)
- [Android Studio runbook](docs/ANDROID_STUDIO_RUNBOOK.md)
- [WAV/FLAC verification checklist](docs/manual-verification/wav-flac-local-playback.md)
- [Runtime issue template](docs/manual-verification/runtime-issue-template.md)

## License

Apache-2.0.

## Build

Open the `YuandaoMusic/` folder with Android Studio or run:

```powershell
.\gradlew.bat testDebugUnitTest assembleDebug lintDebug --stacktrace
```

Requirements:

- JDK 17
- Android SDK 35
- Network access for the first Gradle dependency sync
