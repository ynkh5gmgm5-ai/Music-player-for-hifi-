# SAF Root Management Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Remember user-selected SAF music folders and provide a reliable manual rescan path.

**Architecture:** Store SAF roots in Room as first-class library roots. Keep scanning in `MusicRepository`, expose folder state through `LibraryViewModel`, and add small Home UI controls without building a full folder management screen yet.

**Tech Stack:** Kotlin, Room, Jetpack Compose, SAF `Uri`, Gradle unit tests.

---

### Task 1: Persist SAF Roots

**Files:**
- Modify: `app/src/main/java/com/yuandao/music/data/db/Entities.kt`
- Modify: `app/src/main/java/com/yuandao/music/data/db/AppDatabase.kt`
- Modify: `app/src/main/java/com/yuandao/music/data/db/AppMigrations.kt`
- Modify: `app/src/main/java/com/yuandao/music/data/db/MusicDao.kt`
- Test: `app/src/test/java/com/yuandao/music/data/repository/AppMigrationsTest.kt`

- [ ] Add `LibraryRootEntity` with URI, display name, type, enabled flag, timestamps.
- [ ] Bump Room database version from 2 to 3 and add migration creating `library_roots`.
- [ ] Add DAO methods to observe roots, upsert roots, update scan timestamp, and delete roots.
- [ ] Extend migration test to verify the v2-to-v3 SQL.

### Task 2: Repository Root Management

**Files:**
- Modify: `app/src/main/java/com/yuandao/music/data/repository/MusicRepository.kt`
- Test: `app/src/test/java/com/yuandao/music/data/repository/MusicRepositoryTest.kt`

- [ ] Expose `safRoots` flow from repository.
- [ ] Add `addSafRoot(uri, displayName)` to store root metadata.
- [ ] Add `rescanSafRoots()` to scan all enabled roots and update last scan time.
- [ ] Keep `scanSafRoots(roots)` for direct newly selected folder scans.

### Task 3: ViewModel And Activity Integration

**Files:**
- Modify: `app/src/main/java/com/yuandao/music/MainActivity.kt`
- Modify: `app/src/main/java/com/yuandao/music/ui/LibraryViewModel.kt`
- Modify: `app/src/main/java/com/yuandao/music/ui/YuandaoMusicApp.kt`

- [ ] On SAF folder selection, persist URI permission, store the root, then scan it.
- [ ] Expose `safRoots` in `LibraryViewModel`.
- [ ] Add `rescanSafRoots()` action for manual folder rescans.
- [ ] Release output device manager from `onCleared()`.

### Task 4: Home UI Scan Controls

**Files:**
- Modify: `app/src/main/java/com/yuandao/music/ui/screens/HomeScreen.kt`

- [ ] Show added folder count near the scan controls.
- [ ] Add a compact "rescan folders" action when roots exist.
- [ ] Keep existing MediaStore scan and add-folder actions.

### Final Verification

- [ ] Run focused tests for migrations and repository.
- [ ] Run `./gradlew.bat testDebugUnitTest assembleDebug lintDebug --stacktrace`.
- [ ] Report completed items and next-stage planning.
