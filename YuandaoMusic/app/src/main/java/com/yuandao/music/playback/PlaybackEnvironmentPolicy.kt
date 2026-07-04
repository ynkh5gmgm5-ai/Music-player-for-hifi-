package com.yuandao.music.playback

import androidx.media3.common.C

data class PlaybackEnvironmentConfig(
    val handleAudioFocus: Boolean,
    val pauseWhenAudioBecomesNoisy: Boolean,
    val wakeMode: Int,
)

object PlaybackEnvironmentPolicy {
    val defaultConfig: PlaybackEnvironmentConfig =
        PlaybackEnvironmentConfig(
            handleAudioFocus = false,
            pauseWhenAudioBecomesNoisy = true,
            wakeMode = C.WAKE_MODE_LOCAL,
        )
}
