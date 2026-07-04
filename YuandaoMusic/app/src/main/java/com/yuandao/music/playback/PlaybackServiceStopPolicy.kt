package com.yuandao.music.playback

import androidx.media3.common.Player

object PlaybackServiceStopPolicy {
    fun shouldStopWhenTaskRemoved(
        playWhenReady: Boolean,
        mediaItemCount: Int,
        playbackState: Int,
    ): Boolean =
        !playWhenReady ||
            mediaItemCount == 0 ||
            playbackState == Player.STATE_ENDED
}
