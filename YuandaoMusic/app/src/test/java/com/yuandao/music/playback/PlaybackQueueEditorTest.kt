package com.yuandao.music.playback

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaybackQueueEditorTest {
    @Test
    fun removingUpcomingTrackKeepsCurrentTrackAndReindexesIt() {
        val result = PlaybackQueueEditor.remove(
            tracks = listOf("one", "two", "three", "four"),
            currentIndex = 2,
            removedTrackId = "two",
            trackId = { it },
        )

        requireNotNull(result)
        assertEquals(listOf("one", "three", "four"), result.tracks)
        assertEquals(1, result.currentIndex)
        assertFalse(result.removedCurrent)
    }

    @Test
    fun removingCurrentTrackSelectsNextTrackOrPreviousAtQueueEnd() {
        val next = PlaybackQueueEditor.remove(
            tracks = listOf("one", "two", "three"),
            currentIndex = 1,
            removedTrackId = "two",
            trackId = { it },
        )
        val previous = PlaybackQueueEditor.remove(
            tracks = listOf("one", "two", "three"),
            currentIndex = 2,
            removedTrackId = "three",
            trackId = { it },
        )

        requireNotNull(next)
        requireNotNull(previous)
        assertEquals(listOf("one", "three"), next.tracks)
        assertEquals(1, next.currentIndex)
        assertTrue(next.removedCurrent)
        assertEquals(listOf("one", "two"), previous.tracks)
        assertEquals(1, previous.currentIndex)
        assertTrue(previous.removedCurrent)
    }

    @Test
    fun removingOnlyTrackProducesEmptyQueue() {
        val result = PlaybackQueueEditor.remove(
            tracks = listOf("one"),
            currentIndex = 0,
            removedTrackId = "one",
            trackId = { it },
        )

        requireNotNull(result)
        assertTrue(result.tracks.isEmpty())
        assertEquals(-1, result.currentIndex)
        assertTrue(result.removedCurrent)
    }

    @Test
    fun missingTrackDoesNotProduceAnEdit() {
        val result = PlaybackQueueEditor.remove(
            tracks = listOf("one", "two"),
            currentIndex = 0,
            removedTrackId = "missing",
            trackId = { it },
        )

        assertNull(result)
    }
}
