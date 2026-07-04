# Playback Experience Loop Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make local playback feel coherent by exposing richer queue state, making recent playback more reliable, and documenting the background playback environment in tested code.

**Architecture:** Keep Android-heavy playback in `PlaybackController`, but extract small pure Kotlin policies for queue status, listening-history eligibility, and playback environment defaults. UI consumes derived state from `PlaybackUiState` instead of rebuilding playback rules in Compose.

**Tech Stack:** Kotlin, Media3, Jetpack Compose, Room, JUnit unit tests.

---

### Task 1: Queue Status Summary

**Files:**
- Create: `app/src/main/java/com/yuandao/music/playback/PlaybackQueueSummary.kt`
- Modify: `app/src/main/java/com/yuandao/music/playback/PlaybackModels.kt`
- Modify: `app/src/main/java/com/yuandao/music/ui/screens/HomeScreen.kt`
- Test: `app/src/test/java/com/yuandao/music/playback/PlaybackQueueSummaryTest.kt`

- [ ] Write failing tests for empty queue, playing queue, buffering queue, error state, shuffled repeat state, and out-of-range index handling.
- [ ] Add `PlaybackQueueSummary` and `PlaybackQueueSummarizer.summarize(...)`.
- [ ] Add `val queueSummary` to `PlaybackUiState`.
- [ ] Show queue position and playback status in the continue card and mini player.
- [ ] Run focused playback tests.

### Task 2: Recent Playback Eligibility For Short Tracks

**Files:**
- Modify: `app/src/main/java/com/yuandao/music/playback/PlaybackHistoryGate.kt`
- Modify: `app/src/main/java/com/yuandao/music/playback/PlaybackController.kt`
- Test: `app/src/test/java/com/yuandao/music/playback/PlaybackHistoryGateTest.kt`

- [ ] Write a failing test proving a short track can be recorded after 70% playback.
- [ ] Extend `PlaybackHistoryGate.shouldRecord(...)` with optional `durationMs`.
- [ ] Pass `snapshot.durationMs` from `PlaybackController.recordCurrentPlaybackIfEligible()`.
- [ ] Preserve existing 30-second position and wall-clock rules.
- [ ] Run focused history tests.

### Task 3: Playback Environment Policy

**Files:**
- Create: `app/src/main/java/com/yuandao/music/playback/PlaybackEnvironmentPolicy.kt`
- Modify: `app/src/main/java/com/yuandao/music/playback/PlaybackController.kt`
- Test: `app/src/test/java/com/yuandao/music/playback/PlaybackEnvironmentPolicyTest.kt`

- [ ] Write failing tests for default policy values: handle audio focus, pause on noisy output changes, and keep local wake mode.
- [ ] Add `PlaybackEnvironmentConfig` and `PlaybackEnvironmentPolicy.defaultConfig`.
- [ ] Use that config in `ExoPlayer.Builder`: audio focus handling, wake mode, and audio-becoming-noisy handling.
- [ ] Run focused playback tests.

### Task 4: Stage Review And Verification

**Files:**
- Review: playback files touched above.

- [ ] Run `./gradlew.bat testDebugUnitTest assembleDebug lintDebug --stacktrace`.
- [ ] Request a code review subagent if available.
- [ ] Fix any critical or important review findings.
- [ ] Report completed content, risks, and the next-stage plan.
