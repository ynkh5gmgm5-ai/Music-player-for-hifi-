package com.yuandao.music.playback

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaybackQueueDrawerStateTest {
    @Test
    fun emptyQueueHasNoItems() {
        val state = PlaybackQueueDrawerProjector.project(
            queue = emptyList(),
            currentIndex = -1,
            shuffled = false,
            repeatMode = PlaybackRepeatMode.NONE,
            item = ::item,
        )

        assertTrue(state.isEmpty)
        assertNull(state.current)
        assertEquals(emptyList<PlaybackQueueDrawerItem>(), state.previous)
        assertEquals(emptyList<PlaybackQueueDrawerItem>(), state.upNext)
    }

    @Test
    fun projectsCurrentPreviousAndUpNext() {
        val state = PlaybackQueueDrawerProjector.project(
            queue = listOf("one", "two", "three", "four"),
            currentIndex = 2,
            shuffled = false,
            repeatMode = PlaybackRepeatMode.ALL,
            item = ::item,
        )

        assertEquals("three", state.current?.id)
        assertEquals("3", state.current?.positionLabel)
        assertEquals(listOf("one", "two"), state.previous.map { it.id })
        assertEquals(listOf("1", "2"), state.previous.map { it.positionLabel })
        assertEquals(listOf("four"), state.upNext.map { it.id })
        assertEquals(listOf("4"), state.upNext.map { it.positionLabel })
        assertEquals("顺序播放 / 列表循环", state.modeLabel)
    }

    @Test
    fun shuffledModeIsVisible() {
        val state = PlaybackQueueDrawerProjector.project(
            queue = listOf("one", "two"),
            currentIndex = 0,
            shuffled = true,
            repeatMode = PlaybackRepeatMode.ONE,
            item = ::item,
        )

        assertEquals("随机播放 / 单曲循环", state.modeLabel)
        assertEquals("1", state.current?.positionLabel)
        assertEquals(listOf("2"), state.upNext.map { it.positionLabel })
    }

    @Test
    fun outOfRangeIndexUsesFirstTrack() {
        val state = PlaybackQueueDrawerProjector.project(
            queue = listOf("one", "two"),
            currentIndex = -10,
            shuffled = false,
            repeatMode = PlaybackRepeatMode.NONE,
            item = ::item,
        )

        assertEquals("one", state.current?.id)
        assertEquals(listOf("two"), state.upNext.map { it.id })
    }

    private fun item(id: String, isCurrent: Boolean): PlaybackQueueDrawerItem =
        PlaybackQueueDrawerItem(
            id = id,
            title = id,
            artistName = "Artist $id",
            qualityLabel = "FLAC",
            isCurrent = isCurrent,
        )
}
