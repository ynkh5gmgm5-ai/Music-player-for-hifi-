package com.yuandao.music.playback

object PlaybackServiceStartupPolicy {
    const val BOOTSTRAP_NOTIFICATION_ID = 1001
    const val BOOTSTRAP_CHANNEL_ID = "playback"

    fun requiresImmediateForegroundBootstrap(sdkInt: Int): Boolean = sdkInt >= 26
}
