package com.yuandao.music.playback

import androidx.media3.common.Player
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ShuffleCyclePolicyTest {
    @Test
    fun startsNextCycleAfterAutomaticRepeatAllWrap() {
        assertTrue(
            ShuffleCyclePolicy.shouldStartNextCycle(
                shuffleEnabled = true,
                repeatMode = Player.REPEAT_MODE_ALL,
                transitionReason = Player.MEDIA_ITEM_TRANSITION_REASON_AUTO,
                previousIndex = 3,
                currentIndex = 0,
                queueSize = 4,
            )
        )
    }

    @Test
    fun ignoresManualSeekToFirstTrack() {
        assertFalse(
            ShuffleCyclePolicy.shouldStartNextCycle(
                shuffleEnabled = true,
                repeatMode = Player.REPEAT_MODE_ALL,
                transitionReason = Player.MEDIA_ITEM_TRANSITION_REASON_SEEK,
                previousIndex = 3,
                currentIndex = 0,
                queueSize = 4,
            )
        )
    }

    @Test
    fun ignoresNonWrappingAutomaticTransition() {
        assertFalse(
            ShuffleCyclePolicy.shouldStartNextCycle(
                shuffleEnabled = true,
                repeatMode = Player.REPEAT_MODE_ALL,
                transitionReason = Player.MEDIA_ITEM_TRANSITION_REASON_AUTO,
                previousIndex = 1,
                currentIndex = 2,
                queueSize = 4,
            )
        )
    }

    @Test
    fun ignoresSingleTrackQueue() {
        assertFalse(
            ShuffleCyclePolicy.shouldStartNextCycle(
                shuffleEnabled = true,
                repeatMode = Player.REPEAT_MODE_ALL,
                transitionReason = Player.MEDIA_ITEM_TRANSITION_REASON_AUTO,
                previousIndex = 0,
                currentIndex = 0,
                queueSize = 1,
            )
        )
    }
}
