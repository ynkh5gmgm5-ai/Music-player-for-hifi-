package com.yuandao.music.playback

import org.junit.Assert.assertEquals
import org.junit.Test

class PlaybackQueueSummaryTest {
    @Test
    fun emptyQueueShowsIdleStatus() {
        val summary = PlaybackQueueSummarizer.summarize(
            queueSize = 0,
            currentIndex = -1,
            isPlaying = false,
            isBuffering = false,
            shuffled = false,
            repeatMode = PlaybackRepeatMode.NONE,
            errorMessage = null,
        )

        assertEquals("无队列", summary.positionLabel)
        assertEquals("就绪", summary.statusLabel)
        assertEquals("顺序播放", summary.modeLabel)
        assertEquals(0, summary.upcomingCount)
    }

    @Test
    fun playingQueueShowsPositionAndUpcomingCount() {
        val summary = PlaybackQueueSummarizer.summarize(
            queueSize = 5,
            currentIndex = 1,
            isPlaying = true,
            isBuffering = false,
            shuffled = false,
            repeatMode = PlaybackRepeatMode.NONE,
            errorMessage = null,
        )

        assertEquals("2 / 5", summary.positionLabel)
        assertEquals("播放中", summary.statusLabel)
        assertEquals("顺序播放", summary.modeLabel)
        assertEquals(3, summary.upcomingCount)
    }

    @Test
    fun bufferingStatusTakesPriorityOverPlaying() {
        val summary = PlaybackQueueSummarizer.summarize(
            queueSize = 3,
            currentIndex = 0,
            isPlaying = true,
            isBuffering = true,
            shuffled = false,
            repeatMode = PlaybackRepeatMode.NONE,
            errorMessage = null,
        )

        assertEquals("缓冲中", summary.statusLabel)
    }

    @Test
    fun errorStatusTakesHighestPriority() {
        val summary = PlaybackQueueSummarizer.summarize(
            queueSize = 3,
            currentIndex = 0,
            isPlaying = true,
            isBuffering = true,
            shuffled = false,
            repeatMode = PlaybackRepeatMode.NONE,
            errorMessage = "Cannot decode",
        )

        assertEquals("Cannot decode", summary.statusLabel)
    }

    @Test
    fun shuffledRepeatAllModeIsVisible() {
        val summary = PlaybackQueueSummarizer.summarize(
            queueSize = 12,
            currentIndex = 4,
            isPlaying = false,
            isBuffering = false,
            shuffled = true,
            repeatMode = PlaybackRepeatMode.ALL,
            errorMessage = null,
        )

        assertEquals("随机播放 / 列表循环", summary.modeLabel)
    }

    @Test
    fun currentIndexIsCoercedIntoQueueBounds() {
        val summary = PlaybackQueueSummarizer.summarize(
            queueSize = 4,
            currentIndex = 99,
            isPlaying = false,
            isBuffering = false,
            shuffled = false,
            repeatMode = PlaybackRepeatMode.ONE,
            errorMessage = null,
        )

        assertEquals("4 / 4", summary.positionLabel)
        assertEquals(0, summary.upcomingCount)
        assertEquals("单曲循环", summary.modeLabel)
    }

    @Test
    fun negativeCurrentIndexInNonEmptyQueueStartsAtFirstTrack() {
        val summary = PlaybackQueueSummarizer.summarize(
            queueSize = 4,
            currentIndex = -1,
            isPlaying = false,
            isBuffering = false,
            shuffled = false,
            repeatMode = PlaybackRepeatMode.NONE,
            errorMessage = null,
        )

        assertEquals("1 / 4", summary.positionLabel)
        assertEquals(3, summary.upcomingCount)
    }
}
