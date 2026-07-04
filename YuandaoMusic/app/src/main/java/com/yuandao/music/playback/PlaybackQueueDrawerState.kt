package com.yuandao.music.playback

data class PlaybackQueueDrawerItem(
    val id: String,
    val title: String,
    val artistName: String,
    val qualityLabel: String,
    val isCurrent: Boolean,
    val positionLabel: String = "",
)

data class PlaybackQueueDrawerState(
    val current: PlaybackQueueDrawerItem?,
    val previous: List<PlaybackQueueDrawerItem>,
    val upNext: List<PlaybackQueueDrawerItem>,
    val modeLabel: String,
) {
    val isEmpty: Boolean = current == null && previous.isEmpty() && upNext.isEmpty()
}

object PlaybackQueueDrawerProjector {
    fun <T> project(
        queue: List<T>,
        currentIndex: Int,
        shuffled: Boolean,
        repeatMode: PlaybackRepeatMode,
        item: (track: T, isCurrent: Boolean) -> PlaybackQueueDrawerItem,
    ): PlaybackQueueDrawerState {
        if (queue.isEmpty()) {
            return PlaybackQueueDrawerState(
                current = null,
                previous = emptyList(),
                upNext = emptyList(),
                modeLabel = modeLabel(shuffled, repeatMode),
            )
        }

        val safeIndex = currentIndex.coerceIn(0, queue.lastIndex)
        return PlaybackQueueDrawerState(
            current = itemAt(queue, safeIndex, safeIndex, item),
            previous = queue.indices
                .take(safeIndex)
                .map { index -> itemAt(queue, index, safeIndex, item) },
            upNext = queue.indices
                .drop(safeIndex + 1)
                .map { index -> itemAt(queue, index, safeIndex, item) },
            modeLabel = modeLabel(shuffled, repeatMode),
        )
    }

    private fun <T> itemAt(
        queue: List<T>,
        index: Int,
        currentIndex: Int,
        item: (track: T, isCurrent: Boolean) -> PlaybackQueueDrawerItem,
    ): PlaybackQueueDrawerItem =
        item(queue[index], index == currentIndex).copy(positionLabel = "${index + 1}")

    private fun modeLabel(shuffled: Boolean, repeatMode: PlaybackRepeatMode): String =
        listOfNotNull(
            if (shuffled) "随机播放" else "顺序播放",
            repeatMode.label.takeIf { repeatMode != PlaybackRepeatMode.NONE },
        ).joinToString(" / ")
}

private val PlaybackRepeatMode.label: String
    get() = when (this) {
        PlaybackRepeatMode.NONE -> "顺序播放"
        PlaybackRepeatMode.ONE -> "单曲循环"
        PlaybackRepeatMode.ALL -> "列表循环"
    }
