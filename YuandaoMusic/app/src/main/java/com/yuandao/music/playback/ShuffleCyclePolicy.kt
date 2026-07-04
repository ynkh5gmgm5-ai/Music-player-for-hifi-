package com.yuandao.music.playback

import androidx.media3.common.Player

internal object ShuffleCyclePolicy {
    fun shouldStartNextCycle(
        shuffleEnabled: Boolean,
        repeatMode: Int,
        transitionReason: Int,
        previousIndex: Int,
        currentIndex: Int,
        queueSize: Int,
    ): Boolean =
        shuffleEnabled &&
            repeatMode == Player.REPEAT_MODE_ALL &&
            transitionReason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO &&
            queueSize > 1 &&
            previousIndex == queueSize - 1 &&
            currentIndex == 0
}
