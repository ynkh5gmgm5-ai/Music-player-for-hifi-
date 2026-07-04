package com.yuandao.music.data.model

enum class AudioSourceType {
    LOCAL,
    CLOUD,
    STREAMING,
}

data class AudioSource(
    val type: AudioSourceType,
    val id: String,
    val label: String,
)

