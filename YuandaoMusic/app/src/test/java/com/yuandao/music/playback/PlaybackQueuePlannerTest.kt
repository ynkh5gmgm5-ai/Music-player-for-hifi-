package com.yuandao.music.playback

import com.yuandao.music.data.model.AudioFormat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class PlaybackQueuePlannerTest {
    @Test
    fun planFiltersDeferredFormatsAndKeepsRequestedTrack() {
        val tracks = listOf(
            FixtureTrack("mp3", AudioFormat.MP3),
            FixtureTrack("dsd", AudioFormat.DSD),
            FixtureTrack("flac", AudioFormat.FLAC),
        )

        val plan = PlaybackQueuePlanner.plan(
            tracks = tracks,
            requestedIndex = 2,
            trackId = { it.id },
            format = { it.format },
            title = { it.title },
        )

        assertTrue(plan is PlaybackQueuePlan.Ready)
        val ready = plan as PlaybackQueuePlan.Ready
        assertEquals(listOf("mp3", "flac"), ready.queue.tracks.map { it.id })
        assertEquals(1, ready.queue.startIndex)
    }

    @Test
    fun planRejectsWhenRequestedTrackIsDeferred() {
        val tracks = listOf(
            FixtureTrack("mp3", AudioFormat.MP3),
            FixtureTrack("ape", AudioFormat.APE),
        )

        val plan = PlaybackQueuePlanner.plan(
            tracks = tracks,
            requestedIndex = 1,
            trackId = { it.id },
            format = { it.format },
            title = { it.title },
        )

        assertTrue(plan is PlaybackQueuePlan.Rejected)
    }

    @Test
    fun restorePrefersSavedCurrentTrackIdAfterFiltering() {
        val restored = PlaybackQueuePlanner.restore(
            savedQueue = listOf(
                FixtureTrack("mp3", AudioFormat.MP3),
                FixtureTrack("dsd", AudioFormat.DSD),
                FixtureTrack("flac", AudioFormat.FLAC),
            ),
            currentTrackId = "flac",
            currentIndex = 1,
            trackId = { it.id },
            format = { it.format },
        )

        requireNotNull(restored)
        assertEquals(listOf("mp3", "flac"), restored.tracks.map { it.id })
        assertEquals(1, restored.startIndex)
    }

    @Test
    fun restoreFallsBackToCoercedIndex() {
        val restored = PlaybackQueuePlanner.restore(
            savedQueue = listOf(
                FixtureTrack("ape", AudioFormat.APE),
                FixtureTrack("flac", AudioFormat.FLAC),
            ),
            currentTrackId = "missing",
            currentIndex = 10,
            trackId = { it.id },
            format = { it.format },
        )

        requireNotNull(restored)
        assertEquals(listOf("flac"), restored.tracks.map { it.id })
        assertEquals(0, restored.startIndex)
    }

    @Test
    fun randomizeKeepsCurrentTrackFirstWithoutRepeats() {
        val tracks = listOf("one", "two", "three", "four")

        val randomized = PlaybackQueuePlanner.randomizeKeepingCurrent(
            tracks = tracks,
            currentIndex = 2,
            random = Random(42),
        )

        requireNotNull(randomized)
        assertEquals("three", randomized.tracks.first())
        assertEquals(tracks.sorted(), randomized.tracks.sorted())
        assertEquals(randomized.tracks.size, randomized.tracks.distinct().size)
        assertEquals(0, randomized.startIndex)
    }

    @Test
    fun randomizeForNextCycleStartsAfterCurrentTrack() {
        val tracks = listOf("one", "two", "three", "four")

        val randomized = PlaybackQueuePlanner.randomizeForNextCycle(
            tracks = tracks,
            currentIndex = 2,
            random = Random(42),
        )

        requireNotNull(randomized)
        assertEquals("three", randomized.tracks.first())
        assertEquals(tracks.sorted(), randomized.tracks.sorted())
        assertEquals(randomized.tracks.size, randomized.tracks.distinct().size)
        assertEquals(1, randomized.startIndex)
    }

    @Test
    fun restoreLinearOrderUsesAvailableLibraryOrderForSavedQueue() {
        val availableTracks = listOf(
            FixtureTrack("one", AudioFormat.FLAC),
            FixtureTrack("two", AudioFormat.FLAC),
            FixtureTrack("three", AudioFormat.FLAC),
            FixtureTrack("four", AudioFormat.FLAC),
        )
        val savedRandomQueue = listOf(
            FixtureTrack("three", AudioFormat.FLAC),
            FixtureTrack("one", AudioFormat.FLAC),
            FixtureTrack("two", AudioFormat.FLAC),
        )

        val linearQueue = PlaybackQueuePlanner.restoreLinearOrder(
            availableTracks = availableTracks,
            savedQueue = savedRandomQueue,
            trackId = { it.id },
            format = { it.format },
        )

        assertEquals(listOf("one", "two", "three"), linearQueue.map { it.id })
    }

    @Test
    fun restoreLinearOrderFallsBackToSavedQueueWhenLibraryOrderCannotMatch() {
        val savedRandomQueue = listOf(
            FixtureTrack("three", AudioFormat.FLAC),
            FixtureTrack("one", AudioFormat.FLAC),
            FixtureTrack("two", AudioFormat.FLAC),
        )

        val linearQueue = PlaybackQueuePlanner.restoreLinearOrder(
            availableTracks = emptyList(),
            savedQueue = savedRandomQueue,
            trackId = { it.id },
            format = { it.format },
        )

        assertEquals(listOf("three", "one", "two"), linearQueue.map { it.id })
    }

    private data class FixtureTrack(
        val id: String,
        val format: AudioFormat,
        val title: String = id,
    )
}
