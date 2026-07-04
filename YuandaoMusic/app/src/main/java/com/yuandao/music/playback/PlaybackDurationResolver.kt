package com.yuandao.music.playback

object PlaybackDurationResolver {
    fun resolve(trackDurationMs: Long?, playerDurationMs: Long): Long =
        trackDurationMs?.takeIf { it > 0 }
            ?: playerDurationMs.takeIf { it > 0 }
            ?: 0L
}
