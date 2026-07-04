package com.yuandao.music.playback

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaybackServiceStartupPolicyTest {
    @Test
    fun androidOAndNewerRequireImmediateForegroundBootstrap() {
        assertTrue(PlaybackServiceStartupPolicy.requiresImmediateForegroundBootstrap(sdkInt = 26))
    }

    @Test
    fun preAndroidODoesNotRequireImmediateForegroundBootstrap() {
        assertFalse(PlaybackServiceStartupPolicy.requiresImmediateForegroundBootstrap(sdkInt = 25))
    }
}
