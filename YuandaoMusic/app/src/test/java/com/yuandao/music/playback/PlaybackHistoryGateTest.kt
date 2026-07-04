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
    fun recordsShortTrackAfterMostOfTrackHasPlayed() {
        val gate = PlaybackHistoryGate(minimumListenMs = 30_000)

        gate.onCurrentTrackChanged(trackId = "a", nowMs = 1_000)

        assertTrue(
            gate.shouldRecord(
                trackId = "a",
                positionMs = 14_000,
                durationMs = 20_000,
                nowMs = 10_000,
            )
        )
    }

    @Test
    fun recordsTrackOnlyOncePerContinuousVisit() {
        val gate = PlaybackHistoryGate(minimumListenMs = 30_000)
        gate.onCurrentTrackChanged(trackId = "a", nowMs = 1_000)

        assertTrue(gate.shouldRecord(trackId = "a", positionMs = 31_000, nowMs = 5_000))
        assertFalse(gate.shouldRecord(trackId = "a", positionMs = 35_000, nowMs = 9_000))
    }
}
