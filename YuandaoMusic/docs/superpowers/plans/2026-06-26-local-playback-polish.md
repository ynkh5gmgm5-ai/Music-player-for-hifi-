# Local Playback Polish Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make the Android local playback experience reliable, smooth, and information-rich before adding cloud or streaming sources.

**Architecture:** Keep `PlaybackController` as the concrete Media3 owner and expose it through `PlaybackGateway` so UI does not depend on ExoPlayer directly. Move testable playback decisions into small pure Kotlin policies, then let the controller wire those policies to Media3, Room, and Compose state.

**Tech Stack:** Kotlin, Jetpack Compose, Media3 ExoPlayer/MediaSessionService, Room, JUnit4.

---

## File Structure

- `app/src/main/java/com/yuandao/music/playback/PlaybackHistoryGate.kt`: Pure policy that decides when a track should count as "played".
- `app/src/test/java/com/yuandao/music/playback/PlaybackHistoryGateTest.kt`: Unit tests for playback-history threshold behavior.
- `app/src/main/java/com/yuandao/music/playback/PlaybackQueuePlanner.kt`: Extend shuffle planning so a completed shuffle cycle can be reshuffled without repeats inside a cycle.
- `app/src/test/java/com/yuandao/music/playback/PlaybackQueuePlannerTest.kt`: Add tests for shuffle cycle behavior and restore semantics.
- `app/src/main/java/com/yuandao/music/playback/PlaybackController.kt`: Wire playback-history gate, shuffle cycle policy, service lifecycle, and Media3 state updates.
- `app/src/main/java/com/yuandao/music/playback/YuandaoPlaybackService.kt`: Tighten service stop behavior and media-session lifecycle.
- `app/src/main/java/com/yuandao/music/data/db/MusicDao.kt`: Add source reconciliation helpers for stale local tracks.
- `app/src/main/java/com/yuandao/music/data/repository/MusicRepository.kt`: Rebuild aggregates even when a scan returns zero tracks for a selected source.
- `app/src/main/java/com/yuandao/music/ui/screens/HomeScreen.kt`: Normalize visible Chinese text and split mini-player/track-row pieces if edits become risky.
- `docs/ARCHITECTURE.md`: Document finalized local playback behavior and deferred items.

---

### Task 1: Make Playback History Semantically Correct

**Files:**
- Create: `app/src/main/java/com/yuandao/music/playback/PlaybackHistoryGate.kt`
- Create: `app/src/test/java/com/yuandao/music/playback/PlaybackHistoryGateTest.kt`
- Modify: `app/src/main/java/com/yuandao/music/playback/PlaybackController.kt`

- [ ] **Step 1: Write the failing test**

Create `PlaybackHistoryGateTest.kt`:

```kotlin
package com.yuandao.music.playback

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaybackHistoryGateTest {
    @Test
    fun doesNotRecordImmediatelyOnTrackChange() {
        val gate = PlaybackHistoryGate(minimumListenMs = 30_000)

        gate.onCurrentTrackChanged(trackId = "a", nowMs = 1_000)

        assertFalse(gate.shouldRecord(trackId = "a", positionMs = 1_000, nowMs = 2_000))
    }

    @Test
    fun recordsAfterEnoughPlaybackPosition() {
        val gate = PlaybackHistoryGate(minimumListenMs = 30_000)

        gate.onCurrentTrackChanged(trackId = "a", nowMs = 1_000)

        assertTrue(gate.shouldRecord(trackId = "a", positionMs = 31_000, nowMs = 5_000))
    }

    @Test
    fun recordsAfterEnoughWallClockPlayback() {
        val gate = PlaybackHistoryGate(minimumListenMs = 30_000)

        gate.onCurrentTrackChanged(trackId = "a", nowMs = 1_000)

        assertTrue(gate.shouldRecord(trackId = "a", positionMs = 4_000, nowMs = 31_500))
    }

    @Test
    fun recordsTrackOnlyOncePerContinuousVisit() {
        val gate = PlaybackHistoryGate(minimumListenMs = 30_000)
        gate.onCurrentTrackChanged(trackId = "a", nowMs = 1_000)

        assertTrue(gate.shouldRecord(trackId = "a", positionMs = 31_000, nowMs = 5_000))
        assertFalse(gate.shouldRecord(trackId = "a", positionMs = 35_000, nowMs = 9_000))
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run:

```powershell
$env:JAVA_HOME='C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat testDebugUnitTest --tests com.yuandao.music.playback.PlaybackHistoryGateTest
```

Expected: FAIL because `PlaybackHistoryGate` does not exist.

- [ ] **Step 3: Implement minimal policy**

Create `PlaybackHistoryGate.kt`:

```kotlin
package com.yuandao.music.playback

class PlaybackHistoryGate(
    private val minimumListenMs: Long = 30_000L,
) {
    private var currentTrackId: String? = null
    private var currentTrackStartedAtMs: Long = 0L
    private var recordedTrackId: String? = null

    fun onCurrentTrackChanged(trackId: String?, nowMs: Long) {
        if (trackId == currentTrackId) return
        currentTrackId = trackId
        currentTrackStartedAtMs = nowMs
        recordedTrackId = null
    }

    fun shouldRecord(trackId: String?, positionMs: Long, nowMs: Long): Boolean {
        if (trackId == null || trackId != currentTrackId || trackId == recordedTrackId) return false
        val listenedLongEnough = positionMs >= minimumListenMs ||
            nowMs - currentTrackStartedAtMs >= minimumListenMs
        if (!listenedLongEnough) return false
        recordedTrackId = trackId
        return true
    }
}
```

- [ ] **Step 4: Wire policy into controller**

In `PlaybackController`, replace immediate `recordCurrentPlayback()` calls on `playQueue()` and media-item transition with:

```kotlin
private val playbackHistoryGate = PlaybackHistoryGate()

private fun updateHistoryCandidate() {
    playbackHistoryGate.onCurrentTrackChanged(
        trackId = _state.value.currentTrack?.id,
        nowMs = System.currentTimeMillis(),
    )
}

private fun recordCurrentPlaybackIfEligible() {
    val snapshot = _state.value
    val trackId = snapshot.currentTrack?.id ?: return
    if (!playbackHistoryGate.shouldRecord(trackId, snapshot.positionMs, System.currentTimeMillis())) return
    scope.launch(Dispatchers.IO) {
        dao.recordPlayback(trackId, System.currentTimeMillis())
    }
}
```

Call `updateHistoryCandidate()` after `publishState()` when the current item changes, and call `recordCurrentPlaybackIfEligible()` from the 500 ms loop after `publishState()`.

- [ ] **Step 5: Run tests**

Run:

```powershell
.\gradlew.bat testDebugUnitTest
```

Expected: PASS.

---

### Task 2: Tighten Shuffle Semantics

**Files:**
- Modify: `app/src/main/java/com/yuandao/music/playback/PlaybackQueuePlanner.kt`
- Modify: `app/src/test/java/com/yuandao/music/playback/PlaybackQueuePlannerTest.kt`
- Modify: `app/src/main/java/com/yuandao/music/playback/PlaybackController.kt`

- [ ] **Step 1: Add tests for reshuffle cycle**

Add to `PlaybackQueuePlannerTest.kt`:

```kotlin
@Test
fun randomizeReturnsAllTracksExactlyOnceWithCurrentFirst() {
    val tracks = listOf("a", "b", "c", "d", "e")

    val queue = PlaybackQueuePlanner.randomizeKeepingCurrent(tracks, currentIndex = 1, random = Random(7))

    requireNotNull(queue)
    assertEquals("b", queue.tracks.first())
    assertEquals(tracks.sorted(), queue.tracks.sorted())
    assertEquals(queue.tracks.size, queue.tracks.distinct().size)
}
```

- [ ] **Step 2: Run test**

Run:

```powershell
.\gradlew.bat testDebugUnitTest --tests com.yuandao.music.playback.PlaybackQueuePlannerTest
```

Expected: PASS for existing behavior.

- [ ] **Step 3: Controller cycle behavior**

In `PlaybackController.next()`, when shuffle is enabled and the player is at the final media item, generate a new randomized queue from `linearQueue`, keeping the current final track first, then advance to the next track after preparing the new queue:

```kotlin
private fun reshuffleCycleKeepingCurrent(positionMs: Long = 0L) {
    val source = linearQueue.takeIf { it.isNotEmpty() } ?: queue
    val currentTrackId = _state.value.currentTrack?.id
    val currentIndex = source.indexOfFirst { it.id == currentTrackId }.takeIf { it >= 0 } ?: 0
    val nextPlan = PlaybackQueuePlanner.randomizeKeepingCurrent(source, currentIndex) ?: return
    queue = nextPlan.tracks
    player.setMediaItems(queue.map { it.toMediaItem() }, nextPlan.startIndex, positionMs)
    player.prepare()
}
```

Use this helper only at shuffle cycle boundaries. Do not reshuffle during ordinary `next()`.

- [ ] **Step 4: Run playback tests and build**

Run:

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
```

Expected: both PASS.

---

### Task 3: Reconcile Local Library Scans

**Files:**
- Modify: `app/src/main/java/com/yuandao/music/data/db/MusicDao.kt`
- Modify: `app/src/main/java/com/yuandao/music/data/repository/MusicRepository.kt`

- [ ] **Step 1: Add DAO helpers**

Add to `MusicDao`:

```kotlin
@Query("DELETE FROM tracks WHERE sourceType = :sourceType AND id NOT IN (:trackIds)")
abstract suspend fun deleteTracksMissingFromSource(sourceType: AudioSourceType, trackIds: List<String>)

@Query("DELETE FROM tracks WHERE sourceType = :sourceType")
abstract suspend fun deleteAllTracksForSource(sourceType: AudioSourceType)

@Query("DELETE FROM albums")
abstract suspend fun clearAlbums()

@Query("DELETE FROM artists")
abstract suspend fun clearArtists()
```

Import `AudioSourceType`.

- [ ] **Step 2: Rebuild aggregates even on empty scan**

In `MusicRepository.persistScan`, replace early return with source-aware reconciliation:

```kotlin
private suspend fun persistScan(source: AudioSourceType, tracks: List<TrackEntity>) {
    if (tracks.isEmpty()) {
        dao.deleteAllTracksForSource(source)
    } else {
        dao.upsertTracks(tracks)
        dao.deleteTracksMissingFromSource(source, tracks.map { it.id })
    }
    rebuildLibraryAggregates(dao.getAllTracks())
}
```

Update callers to pass `AudioSourceType.LOCAL`.

- [ ] **Step 3: Clear aggregate tables before rebuild**

At the top of `rebuildLibraryAggregates`:

```kotlin
dao.clearArtists()
dao.clearAlbums()
```

Then upsert non-empty aggregate lists.

- [ ] **Step 4: Run build**

Run:

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat lintDebug
```

Expected: both PASS.

---

### Task 4: Stabilize MediaSession Service Lifecycle

**Files:**
- Modify: `app/src/main/java/com/yuandao/music/playback/YuandaoPlaybackService.kt`
- Modify: `app/src/main/AndroidManifest.xml`

- [ ] **Step 1: Make service non-exported unless external controllers require it**

Change manifest service:

```xml
<service
    android:name=".playback.YuandaoPlaybackService"
    android:exported="false"
    android:foregroundServiceType="mediaPlayback">
    <intent-filter>
        <action android:name="androidx.media3.session.MediaSessionService" />
    </intent-filter>
</service>
```

- [ ] **Step 2: Stop service when playback is idle**

In `YuandaoPlaybackService`, keep existing `onTaskRemoved`, and add a listener on the player that calls `stopSelf()` when state is idle/ended and `playWhenReady` is false.

- [ ] **Step 3: Run lint**

Run:

```powershell
.\gradlew.bat lintDebug
```

Expected: PASS.

---

### Task 5: Normalize Current UI Text Before Visual Replication

**Files:**
- Modify: `app/src/main/java/com/yuandao/music/ui/screens/HomeScreen.kt`

- [ ] **Step 1: Replace garbled visible Chinese strings**

Replace the tab/header/section strings with clean UTF-8 literals:

```kotlin
SegmentLabel("私有云", selected = false)
SegmentLabel("流媒体", selected = false)
SegmentLabel("本地曲库", selected = true)
Text(text = "音乐")
Text(text = "本机存储 · Hi-Res ready")
SectionHeader("资料库概览", "第一阶段")
```

- [ ] **Step 2: Re-run Compose compile**

Run:

```powershell
.\gradlew.bat assembleDebug
```

Expected: PASS.

---

### Task 6: Verification Gate

**Files:**
- Modify: `docs/ARCHITECTURE.md`

- [ ] **Step 1: Run full verification**

Run:

```powershell
$env:JAVA_HOME='C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
.\gradlew.bat lintDebug
```

Expected:
- Unit tests pass.
- Debug APK builds.
- Lint passes.

- [ ] **Step 2: Document remaining device-only checks**

Add to `docs/ARCHITECTURE.md`:

```markdown
## Device Verification Backlog

- Install on Android device or emulator.
- Grant media/audio permissions.
- Scan MediaStore and SAF folders.
- Play MP3/FLAC/WAV/ALAC tracks.
- Verify lock-screen controls and notification actions.
- Verify headphone unplug pauses playback.
- Verify shuffle queue does not repeat within a cycle.
- Verify runtime sample rate/bit depth updates during playback.
```

