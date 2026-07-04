package com.yuandao.music.playback

import android.media.AudioManager
import org.junit.Assert.assertEquals
import org.junit.Test

class AudioFocusPolicyTest {
    @Test
    fun permanentLossPausesWithoutResume() {
        val decision = AudioFocusPolicy.decide(
            focusChange = AudioManager.AUDIOFOCUS_LOSS,
            wasPlayingBeforeLoss = true,
            resumeOnFocusGain = false,
        )

        assertEquals(AudioFocusAction.Pause, decision.action)
        assertEquals(false, decision.resumeOnFocusGain)
    }

    @Test
    fun transientLossPausesAndRemembersResumeWhenPlaying() {
        val decision = AudioFocusPolicy.decide(
            focusChange = AudioManager.AUDIOFOCUS_LOSS_TRANSIENT,
            wasPlayingBeforeLoss = true,
            resumeOnFocusGain = false,
        )

        assertEquals(AudioFocusAction.Pause, decision.action)
        assertEquals(true, decision.resumeOnFocusGain)
    }

    @Test
    fun duckLossReducesVolumeWithoutChangingResumeFlag() {
        val decision = AudioFocusPolicy.decide(
            focusChange = AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK,
            wasPlayingBeforeLoss = true,
            resumeOnFocusGain = true,
        )

        assertEquals(AudioFocusAction.Duck, decision.action)
        assertEquals(true, decision.resumeOnFocusGain)
    }

    @Test
    fun gainAfterTransientLossResumesAndClearsResumeFlag() {
        val decision = AudioFocusPolicy.decide(
            focusChange = AudioManager.AUDIOFOCUS_GAIN,
            wasPlayingBeforeLoss = false,
            resumeOnFocusGain = true,
        )

        assertEquals(AudioFocusAction.Resume, decision.action)
        assertEquals(false, decision.resumeOnFocusGain)
    }

    @Test
    fun gainAfterManualPauseRestoresVolumeOnly() {
        val decision = AudioFocusPolicy.decide(
            focusChange = AudioManager.AUDIOFOCUS_GAIN,
            wasPlayingBeforeLoss = false,
            resumeOnFocusGain = false,
        )

        assertEquals(AudioFocusAction.RestoreVolume, decision.action)
        assertEquals(false, decision.resumeOnFocusGain)
    }
}
