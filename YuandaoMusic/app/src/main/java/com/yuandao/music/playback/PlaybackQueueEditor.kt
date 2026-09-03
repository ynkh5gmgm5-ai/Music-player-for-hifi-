package com.yuandao.music.playback

data class PlaybackQueueEditResult<T>(
    val tracks: List<T>,
    val currentIndex: Int,
    val removedCurrent: Boolean,
)

object PlaybackQueueEditor {
    fun <T> remove(
        tracks: List<T>,
        currentIndex: Int,
        removedTrackId: String,
        trackId: (T) -> String,
    ): PlaybackQueueEditResult<T>? {
        val removedIndex = tracks.indexOfFirst { trackId(it) == removedTrackId }
        if (removedIndex < 0) return null

        val hasCurrent = currentIndex in tracks.indices
        val safeCurrentIndex = currentIndex.coerceIn(0, tracks.lastIndex)
        val removedCurrent = hasCurrent && removedIndex == safeCurrentIndex
        val remainingTracks = tracks.filterIndexed { index, _ -> index != removedIndex }

        if (remainingTracks.isEmpty()) {
            return PlaybackQueueEditResult(
                tracks = emptyList(),
                currentIndex = -1,
                removedCurrent = removedCurrent,
            )
        }

        if (!hasCurrent) {
            return PlaybackQueueEditResult(
                tracks = remainingTracks,
                currentIndex = -1,
                removedCurrent = false,
            )
        }

        val nextCurrentIndex = if (removedCurrent) {
            safeCurrentIndex.coerceAtMost(remainingTracks.lastIndex)
        } else {
            val currentTrackId = trackId(tracks[safeCurrentIndex])
            remainingTracks.indexOfFirst { trackId(it) == currentTrackId }
                .takeIf { it >= 0 }
                ?: 0
        }

        return PlaybackQueueEditResult(
            tracks = remainingTracks,
            currentIndex = nextCurrentIndex,
            removedCurrent = removedCurrent,
        )
    }
}
