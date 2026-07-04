package com.yuandao.music.playback

data class PlaybackQueueSummary(
    val positionLabel: String,
    val statusLabel: String,
    val modeLabel: String,
    val upcomingCount: Int,
)

object PlaybackQueueSummarizer {
    fun summarize(
        queueSize: Int,
        currentIndex: Int,
        isPlaying: Boolean,
        isBuffering: Boolean,
        shuffled: Boolean,
        repeatMode: PlaybackRepeatMode,
        errorMessage: String?,
    ): PlaybackQueueSummary {
        val normalizedQueueSize = queueSize.coerceAtLeast(0)
        val normalizedIndex = if (normalizedQueueSize == 0) {
            -1
        } else {
            currentIndex.coerceIn(0, normalizedQueueSize - 1)
        }
        val positionLabel = if (normalizedQueueSize == 0) {
            "无队列"
        } else {
            "${normalizedIndex + 1} / $normalizedQueueSize"
        }
        val statusLabel = when {
            !errorMessage.isNullOrBlank() -> errorMessage
            isBuffering -> "缓冲中"
            isPlaying -> "播放中"
            normalizedQueueSize > 0 -> "已暂停"
            else -> "就绪"
        }
        val modeLabel = listOfNotNull(
            "随机播放".takeIf { shuffled },
            repeatMode.label.takeIf { repeatMode != PlaybackRepeatMode.NONE },
        ).joinToString(" / ").ifBlank { "顺序播放" }

        return PlaybackQueueSummary(
            positionLabel = positionLabel,
            statusLabel = statusLabel,
            modeLabel = modeLabel,
            upcomingCount = if (normalizedIndex < 0) {
                0
            } else {
                (normalizedQueueSize - normalizedIndex - 1).coerceAtLeast(0)
            },
        )
    }
}

private val PlaybackRepeatMode.label: String
    get() = when (this) {
        PlaybackRepeatMode.NONE -> "顺序播放"
        PlaybackRepeatMode.ONE -> "单曲循环"
        PlaybackRepeatMode.ALL -> "列表循环"
    }
