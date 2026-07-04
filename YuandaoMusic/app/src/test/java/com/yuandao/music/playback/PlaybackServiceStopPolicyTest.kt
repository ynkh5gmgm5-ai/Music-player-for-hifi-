package com.yuandao.music.playback

import androidx.media3.common.Player
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaybackServiceStopPolicyTest {
    @Test
    fun keepsServiceWhenActivelyPlayingQueuedMedia() {
        assertFalse(
            PlaybackServiceStopPolicy.shouldStopWhenTaskRemoved(
                playWhenReady = true,
                mediaItemCount = 3,
                playbackState = Player.STATE_READY,
            )
        )
    }

    @Test
    fun stopsServiceWhenPlaybackIsPaused() {
        assertTrue(
            PlaybackServiceStopPolicy.shouldStopWhenTaskRemoved(
                playWhenReady = false,
                mediaItemCount = 3,
                playbackState = Player.STATE_READY,
            )
        )
    }

    @Test
    fun stopsServiceWhenQueueIsEmpty() {
        assertTrue(
            PlaybackServiceStopPolicy.shouldStopWhenTaskRemoved(
                playWhenReady = true,
                mediaItemCount = 0,
                playbackState = Player.STATE_READY,
            )
        )
    }

    @Test
    fun stopsServiceWhenPlaybackEnded() {
        assertTrue(
            PlaybackServiceStopPolicy.shouldStopWhenTaskRemoved(
                playWhenReady = true,
                mediaItemCount = 3,
                playbackState = Player.STATE_ENDED,
            )
        )
    }
}
