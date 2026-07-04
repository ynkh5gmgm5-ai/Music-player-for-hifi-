# Local Player Hardening 2 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Strengthen the local HiFi playback foundation after the first playback polish pass.

**Architecture:** Keep source metadata, runtime decoder state, persistence, UI rendering, and device discovery as separate small units. Prefer pure Kotlin policy/model tests where Android framework APIs are not needed, and Android lint/build checks for Compose and service integration.

**Tech Stack:** Kotlin, Jetpack Compose, Media3, Room, Android AudioManager, Gradle unit tests, Android lint.

---

### Task 1: Split Source Audio Info From Runtime Decoder Info

**Files:**
- Modify: `app/src/main/java/com/yuandao/music/playback/PlaybackModels.kt`
- Modify: `app/src/main/java/com/yuandao/music/playback/PlaybackController.kt`
- Modify: `app/src/main/java/com/yuandao/music/ui/screens/HomeScreen.kt`
- Test: `app/src/test/java/com/yuandao/music/playback/PlaybackAudioInfoTest.kt`

- [ ] Add tests proving source labels use scanned file metadata and runtime labels use decoder data separately.
- [ ] Replace the single mixed `PlaybackAudioInfo` fields with nested `source` and `runtime` info objects.
- [ ] Update `PlaybackController` mapping so source file bit depth/sample rate never gets overwritten by decoder/output fields.
- [ ] Update UI labels to prefer source quality for HiFi badges and show decoder details only as runtime context.
- [ ] Run focused tests, then full Gradle verification.

### Task 2: Room Schema Export And Migration Verification

**Files:**
- Modify: `app/build.gradle.kts`
- Modify: `app/src/main/java/com/yuandao/music/core/AppContainer.kt`
- Modify: `app/src/main/java/com/yuandao/music/data/db/AppDatabase.kt`
- Create: `app/src/androidTest/java/com/yuandao/music/data/db/AppDatabaseMigrationTest.kt`
- Create/update: `app/schemas/com.yuandao.music.data.db.AppDatabase/`

- [ ] Enable Room schema export through KSP arguments.
- [ ] Move migrations to a reusable `AppMigrations` object.
- [ ] Add Room testing dependency and Android instrumentation runner if needed.
- [ ] Add a 1-to-2 migration test that creates a v1 database, migrates, and validates schema.
- [ ] Run unit/build/lint; Android instrumentation test may be build-only unless a device is available.

### Task 3: Display Cached Local Cover Art

**Files:**
- Modify: `app/build.gradle.kts`
- Modify: `gradle/libs.versions.toml`
- Modify: `app/src/main/java/com/yuandao/music/ui/screens/HomeScreen.kt`

- [ ] Add a Compose-friendly image loader dependency.
- [ ] Replace placeholder-only cover rendering with a reusable cover composable that loads `Track.coverUri`.
- [ ] Keep the current gradient/music-note placeholder as the failure and empty fallback.
- [ ] Run Compose build and lint.

### Task 4: Output Device Auto Refresh

**Files:**
- Modify: `app/src/main/java/com/yuandao/music/playback/OutputDeviceManager.kt`
- Test: `app/src/test/java/com/yuandao/music/playback/OutputDeviceManagerTest.kt` or a focused policy test if Android callbacks cannot run on JVM.

- [ ] Introduce a small adapter around AudioManager device reads and callback registration.
- [ ] Register `AudioDeviceCallback` so device list updates on add/remove events.
- [ ] Provide a release hook to unregister callbacks when the manager is no longer needed.
- [ ] Keep manual refresh as a user-visible fallback.
- [ ] Run focused tests and full Gradle verification.

### Final Verification

- [ ] Run `./gradlew.bat testDebugUnitTest assembleDebug lintDebug --stacktrace`.
- [ ] Report remaining limitations, especially Android instrumentation tests that require a connected device.
