# Project Status

Last updated: 2026-06-28

## Current Stage

Yuandao Music is in the Android local-player foundation stage. The project is not yet a polished
consumer release. The current goal is to make local WAV/FLAC playback reliable before expanding into
visual 1:1 recreation, USB exclusive output, Linux, cloud, or streaming work.

## Verified Build Gate

Use this command from `YuandaoMusic/` before claiming a stage is complete:

```powershell
.\gradlew.bat testDebugUnitTest assembleDebug lintDebug --stacktrace
```

Latest known result in this thread: `BUILD SUCCESSFUL`.

## Feature Matrix

| Area | Status | Notes |
| --- | --- | --- |
| Android project scaffold | Complete | Kotlin, Compose, Media3, Room, KSP, Gradle wrapper. |
| License route | Complete | Apache-2.0 project. Auxio is reference only; no GPL code is copied. |
| Local source model | Complete | `LOCAL`, `CLOUD`, and `STREAMING` source types exist; only `LOCAL` is active. |
| MediaStore scan | Implemented, needs device verification | Indexes local media metadata into Room. |
| SAF folder scan | Implemented, needs device verification | Used for selected folders and external storage style testing. |
| Track/album/artist database | Implemented | Room stores library and aggregate views. |
| Cover extraction/cache | Implemented, needs broader file testing | Embedded cover handling exists; more formats need runtime samples. |
| WAV/FLAC playback | Implemented path, needs real playback verification | Code path exists through Media3; runtime confirmation is next. |
| MP3/AAC/ALAC/OGG/OPUS playback | First-pass playable | Kept available because Media3 can generally handle them. WAV/FLAC remain the priority. |
| DSD/APE/CUE playback | Deferred | Recognized as HiFi formats but not first-pass playable. |
| Queue playback | Implemented | Queue order, previous/next, repeat, shuffle, and persisted queue are present. |
| Shuffle behavior | Implemented | Actual queue is randomized while keeping the current track; queue drawer shows order labels. |
| Now-playing UI | Implemented first pass | Modal sheet with artwork, metadata, progress, lyrics, controls, queue access. |
| Mini player | Implemented first pass | Persistent bottom surface driven by `PlaybackUiState`. |
| Lyrics | Partial | Sidecar `.lrc` parsing and current-line display exist. Embedded lyrics are deferred. |
| Background playback service | Implemented, needs runtime verification | Media3 `MediaSessionService` is present. Notification behavior needs emulator/device checks. |
| Audio focus | Implemented | App owns manual focus policy: pause, duck, restore, optional resume. |
| Noisy output handling | Implemented | Headphone unplug/noisy output policy is configured. |
| Output device discovery | Partial | System output discovery exists; USB exclusive output is deferred. |
| Cloud/private library | Reserved | Data-source positions exist, but no cloud sync or cloud playback is active. |
| Streaming services | Reserved | Deferred due to licensing, business, and provider constraints. |
| Linux client | Deferred | Should wait until Android local playback is stable. |
| 1:1 visual recreation | Deferred until playback loop is verified | Current UI follows the dark glass direction but is not final visual fidelity. |

## Known Risks

- Runtime access to external folders can differ between emulator, real phone, Android version, and USB media paths.
- Media3 decoding behavior may differ across devices for high sample rates and uncommon WAV variants.
- Runtime bit depth may not always be exposed accurately by Android decoder/output APIs.
- Background notification and lock-screen behavior must be checked on a real runtime target.
- The workspace currently shows `YuandaoMusic/` as untracked from the parent repository view; long-term version control needs a deliberate cleanup step.

## When User Help Is Needed

The user does not need to write code. The most useful help is:

- Run the app in Android Studio when a runtime device/emulator is required.
- Provide screenshots or screen recordings of visual/runtime issues.
- Provide Logcat output for crashes, playback failures, scanner failures, and permission issues.
- Provide sample WAV/FLAC files when a decoder, tag, cover, or lyric issue is file-specific.
- Confirm product choices when two technically valid behaviors conflict.

## Next Gate

The next stage is Android runtime verification:

1. Open the project in Android Studio.
2. Run the debug app on an emulator or Android device.
3. Use SAF to select a folder containing WAV and FLAC files.
4. Verify scan, playback, queue, shuffle, lyrics, background behavior, and close-service behavior.
5. File any runtime problems with `docs/manual-verification/runtime-issue-template.md`.

