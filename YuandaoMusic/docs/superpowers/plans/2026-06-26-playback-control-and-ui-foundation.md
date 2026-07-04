# Playback Control And UI Foundation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Improve Android local WAV/FLAC playback usability with explicit service controls, audio focus behavior, queue drawer data, and clean UI text for later 1:1 visual work.

**Architecture:** Keep Media3 as the playback engine and use standard `MediaSession` behavior for notification transport controls. Add small pure Kotlin policies/models for stop behavior, audio focus decisions, and queue drawer projection, then connect those through `PlaybackGateway`, `LibraryViewModel`, and `HomeScreen`.

**Tech Stack:** Kotlin, Media3, Jetpack Compose, Room, JUnit unit tests.

---

### Task 1: Explicit Stop Playback Path

**Files:**
- Modify: `app/src/main/java/com/yuandao/music/playback/PlaybackGateway.kt`
- Modify: `app/src/main/java/com/yuandao/music/playback/PlaybackController.kt`
- Modify: `app/src/main/java/com/yuandao/music/playback/YuandaoPlaybackService.kt`
- Modify: `app/src/main/java/com/yuandao/music/ui/LibraryViewModel.kt`
- Modify: `app/src/main/java/com/yuandao/music/ui/YuandaoMusicApp.kt`
- Modify: `app/src/main/java/com/yuandao/music/ui/screens/HomeScreen.kt`
- Test: `app/src/test/java/com/yuandao/music/playback/PlaybackServiceStopPolicyTest.kt`

- [ ] Add a failing unit test for service stop policy: stop when queue empty, stopped, or ended; keep service when actively playing.
- [ ] Extract `PlaybackServiceStopPolicy.shouldStopWhenTaskRemoved(...)`.
- [ ] Add `stopPlayback()` to `PlaybackGateway` and implement it in `PlaybackController` by pausing, stopping service playback, publishing state, and persisting state.
- [ ] Expose `stopPlayback()` through `LibraryViewModel` and wire it to a compact close button in `MiniPlayerBar`.
- [ ] Keep previous/next/play-pause controls unchanged and rely on MediaSession for notification actions.

### Task 2: Audio Focus Event Policy

**Files:**
- Create: `app/src/main/java/com/yuandao/music/playback/AudioFocusPolicy.kt`
- Modify: `app/src/main/java/com/yuandao/music/playback/PlaybackModels.kt`
- Modify: `app/src/main/java/com/yuandao/music/playback/PlaybackController.kt`
- Test: `app/src/test/java/com/yuandao/music/playback/AudioFocusPolicyTest.kt`

- [ ] Add failing tests for transient loss, permanent loss, duck, gain-after-transient-loss, and gain-after-manual-pause.
- [ ] Create `AudioFocusPolicy` with actions: `Pause`, `Duck`, `RestoreVolume`, `Resume`, `NoOp`.
- [ ] Track focus-derived pause intent in `PlaybackController`.
- [ ] Override `onAudioFocusChanged` in `PlaybackController`: pause on loss/loss transient, duck on duck, restore and optionally resume on gain.
- [ ] Add a focus status label to `PlaybackUiState` only if needed for debugging; keep UI minimal.

### Task 3: Queue Drawer State

**Files:**
- Create: `app/src/main/java/com/yuandao/music/playback/PlaybackQueueDrawerState.kt`
- Modify: `app/src/main/java/com/yuandao/music/playback/PlaybackModels.kt`
- Modify: `app/src/main/java/com/yuandao/music/ui/screens/HomeScreen.kt`
- Test: `app/src/test/java/com/yuandao/music/playback/PlaybackQueueDrawerStateTest.kt`

- [ ] Add failing tests for current track, up-next list, previous list, shuffled flag, and empty queue.
- [ ] Add `PlaybackQueueDrawerState` and a pure projector from `PlaybackUiState` fields.
- [ ] Expose it as a derived property on `PlaybackUiState`.
- [ ] Add a bottom-sheet style queue drawer in `HomeScreen` with current, up next, previous, shuffle/repeat labels, and a close control.
- [ ] Keep queue item actions read-only for this phase; reordering and remove-from-queue are later work.

### Task 4: UI Text Encoding Cleanup

**Files:**
- Modify: `app/src/main/java/com/yuandao/music/ui/screens/HomeScreen.kt`

- [ ] Replace garbled Chinese text with clean Chinese or neutral English text in visible first-screen components.
- [ ] Keep the layout structure unchanged except where Task 1 and Task 3 add controls.
- [ ] Prefer concise UI strings: `私有云`, `流媒体`, `本地曲库`, `音乐`, `继续播放`, `最近播放`, `最近添加`, `常听歌手`, `资料库概览`, `本机输出`.
- [ ] Leave full 1:1 visual styling for the later Figma/reference-image phase.

### Task 5: Verification And Review

**Files:**
- Review all changed playback and UI files.

- [ ] Run focused playback tests.
- [ ] Run `./gradlew.bat testDebugUnitTest assembleDebug lintDebug --stacktrace`.
- [ ] Request a code review subagent for playback behavior and UI state wiring.
- [ ] Fix critical and important findings.
- [ ] Report completed content, remaining risks, and next-stage plan.
