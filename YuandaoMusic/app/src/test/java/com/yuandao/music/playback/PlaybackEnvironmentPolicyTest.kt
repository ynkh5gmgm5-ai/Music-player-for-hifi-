package com.yuandao.music.playback

import androidx.media3.common.C
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaybackEnvironmentPolicyTest {
    @Test
    fun defaultConfigUsesManualAudioFocusAndNoisyOutputPause() {
        val config = PlaybackEnvironmentPolicy.defaultConfig

        assertEquals(false, config.handleAudioFocus)
        assertTrue(config.pauseWhenAudioBecomesNoisy)
        assertEquals(C.WAKE_MODE_LOCAL, config.wakeMode)
    }
}
