package com.yuandao.music.playback

import android.media.AudioManager

enum class AudioFocusAction {
    Pause,
    Duck,
    RestoreVolume,
    Resume,
    NoOp,
}

data class AudioFocusDecision(
    val action: AudioFocusAction,
    val resumeOnFocusGain: Boolean,
)

object AudioFocusPolicy {
    fun decide(
        focusChange: Int,
        wasPlayingBeforeLoss: Boolean,
        resumeOnFocusGain: Boolean,
    ): AudioFocusDecision =
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN ->
                AudioFocusDecision(
                    action = if (resumeOnFocusGain) AudioFocusAction.Resume else AudioFocusAction.RestoreVolume,
                    resumeOnFocusGain = false,
                )

            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT ->
                AudioFocusDecision(
                    action = AudioFocusAction.Pause,
                    resumeOnFocusGain = wasPlayingBeforeLoss,
                )

            AudioManager.AUDIOFOCUS_LOSS ->
                AudioFocusDecision(
                    action = AudioFocusAction.Pause,
                    resumeOnFocusGain = false,
                )

            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK ->
                AudioFocusDecision(
                    action = AudioFocusAction.Duck,
                    resumeOnFocusGain = resumeOnFocusGain,
                )

            else ->
                AudioFocusDecision(
                    action = AudioFocusAction.NoOp,
                    resumeOnFocusGain = resumeOnFocusGain,
                )
        }
}
