# Now Playing And Local Playback Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a dedicated Android now-playing surface, make queue order visible, and document the WAV/FLAC local playback verification path.

**Architecture:** Keep playback state in `PlaybackUiState`. Add pure Kotlin/Compose-adjacent projection models under `ui/screens` for the now-playing sheet, and extend the existing queue drawer model in `playback` with order labels. UI changes stay inside `HomeScreen.kt` so the app does not need a navigation graph yet.

**Tech Stack:** Kotlin, Jetpack Compose, Media3 state already exposed through `PlaybackUiState`, JUnit unit tests.

---

### Task 1: Now Playing Screen State

**Files:**
- Create: `app/src/main/java/com/yuandao/music/ui/screens/NowPlayingScreenState.kt`
- Test: `app/src/test/java/com/yuandao/music/ui/screens/NowPlayingScreenStateTest.kt`

- [ ] Write failing tests for active track projection, empty state, lyric line selection, and progress clamping.
- [ ] Add `NowPlayingScreenState` and `NowPlayingScreenStateProjector.project(playbackState, lyrics)`.
- [ ] Keep formatting helpers local to the projector so UI composables receive ready-to-render strings.
- [ ] Run `./gradlew.bat testDebugUnitTest --tests com.yuandao.music.ui.screens.NowPlayingScreenStateTest --stacktrace`.

### Task 2: Queue Order Labels

**Files:**
- Modify: `app/src/main/java/com/yuandao/music/playback/PlaybackQueueDrawerState.kt`
- Modify: `app/src/main/java/com/yuandao/music/ui/screens/HomeScreen.kt`
- Test: `app/src/test/java/com/yuandao/music/playback/PlaybackQueueDrawerStateTest.kt`

- [ ] Write a failing test proving current, previous, and up-next items expose stable `positionLabel` values.
- [ ] Add `positionLabel` to `PlaybackQueueDrawerItem` with a default value for compatibility.
- [ ] Set position labels in `PlaybackQueueDrawerProjector` from the active queue order.
- [ ] Render the position label in `QueueDrawerItemRow`.
- [ ] Run `./gradlew.bat testDebugUnitTest --tests com.yuandao.music.playback.PlaybackQueueDrawerStateTest --stacktrace`.

### Task 3: Now Playing Sheet UI

**Files:**
- Modify: `app/src/main/java/com/yuandao/music/ui/screens/HomeScreen.kt`

- [ ] Add `showNowPlaying` local state to `HomeScreen`.
- [ ] Make the mini player open the now-playing sheet.
- [ ] Add `NowPlayingSheet` with artwork, title, artist, album, source/runtime quality, progress, lyric line, error message, and playback controls.
- [ ] Reuse existing callbacks: play/pause, previous, next, seek, shuffle, repeat, stop, and queue.
- [ ] Keep styling in the current dark glass language; leave 1:1 visual recreation for the later reference-image phase.

### Task 4: WAV/FLAC Verification Notes

**Files:**
- Create: `docs/manual-verification/wav-flac-local-playback.md`
- Test: `app/src/test/java/com/yuandao/music/data/model/AudioFormatTest.kt`

- [ ] Ensure the existing format-boundary test still proves WAV and FLAC are first-pass playable while DSD/APE/CUE are deferred.
- [ ] Document manual verification steps for scanning a folder, playing WAV/FLAC, checking queue controls, checking random playback, and observing error behavior.
- [ ] Run `./gradlew.bat testDebugUnitTest --tests com.yuandao.music.data.model.AudioFormatTest --stacktrace`.

### Task 5: Verification And Review

**Files:**
- Review changed playback, UI, and docs files.

- [ ] Run focused tests for now-playing, queue drawer, audio format, and playback controls.
- [ ] Run `./gradlew.bat testDebugUnitTest assembleDebug lintDebug --stacktrace`.
- [ ] Do a local review for UI state drift, queue order correctness, and user-visible text.
- [ ] Report completed content, remaining risks, and the next-stage plan.

