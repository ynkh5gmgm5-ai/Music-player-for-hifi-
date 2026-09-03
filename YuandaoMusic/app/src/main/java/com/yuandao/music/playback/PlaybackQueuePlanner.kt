package com.yuandao.music.playback

import com.yuandao.music.data.model.AudioFormat
import com.yuandao.music.data.model.Track
import kotlin.random.Random

data class PlannedPlaybackQueue<T>(
    val tracks: List<T>,
    val startIndex: Int,
)

sealed class PlaybackQueuePlan<out T> {
    data class Ready<T>(val queue: PlannedPlaybackQueue<T>) : PlaybackQueuePlan<T>()
    data class Rejected(val reason: String) : PlaybackQueuePlan<Nothing>()
}

object PlaybackQueuePlanner {
    fun planTracks(tracks: List<Track>, requestedIndex: Int): PlaybackQueuePlan<Track> =
        plan(
            tracks = tracks,
            requestedIndex = requestedIndex,
            trackId = { it.id },
            format = { it.format },
            title = { it.title },
        )

    fun restoreTracks(
        savedQueue: List<Track>,
        currentTrackId: String?,
        currentIndex: Int,
    ): PlannedPlaybackQueue<Track>? =
        restore(
            savedQueue = savedQueue,
            currentTrackId = currentTrackId,
            currentIndex = currentIndex,
            trackId = { it.id },
            format = { it.format },
        )

    fun restoreLinearTrackOrder(
        availableTracks: List<Track>,
        savedQueue: List<Track>,
    ): List<Track> =
        restoreLinearOrder(
            availableTracks = availableTracks,
            savedQueue = savedQueue,
            trackId = { it.id },
            format = { it.format },
        )

    fun <T> randomizeKeepingCurrent(
        tracks: List<T>,
        currentIndex: Int,
        random: Random = Random.Default,
    ): PlannedPlaybackQueue<T>? {
        if (tracks.isEmpty()) return null
        val index = currentIndex.coerceIn(0, tracks.lastIndex)
        val current = tracks[index]
        val rest = tracks
            .filterIndexed { candidateIndex, _ -> candidateIndex != index }
            .shuffled(random)
        return PlannedPlaybackQueue(listOf(current) + rest, startIndex = 0)
    }

    fun <T> randomizeForNextCycle(
        tracks: List<T>,
        currentIndex: Int,
        random: Random = Random.Default,
    ): PlannedPlaybackQueue<T>? {
        if (tracks.isEmpty()) return null
        val index = currentIndex.coerceIn(0, tracks.lastIndex)
        val current = tracks[index]
        val rest = tracks
            .filterIndexed { candidateIndex, _ -> candidateIndex != index }
            .shuffled(random)
        val randomized = listOf(current) + rest
        return PlannedPlaybackQueue(randomized, startIndex = if (randomized.size > 1) 1 else 0)
    }

    fun <T> plan(
        tracks: List<T>,
        requestedIndex: Int,
        trackId: (T) -> String,
        format: (T) -> AudioFormat,
        title: (T) -> String,
    ): PlaybackQueuePlan<T> {
        if (tracks.isEmpty()) {
            return PlaybackQueuePlan.Rejected(PlaybackErrorMessage.noLocalTracks)
        }

        val requestedTrack = tracks.getOrNull(requestedIndex.coerceAtLeast(0))
        if (requestedTrack != null && !format(requestedTrack).isFirstPassPlayable) {
            return PlaybackQueuePlan.Rejected(
                PlaybackErrorMessage.unsupportedTrack(title(requestedTrack), format(requestedTrack))
            )
        }

        val playableTracks = tracks.filter { format(it).isFirstPassPlayable }
        if (playableTracks.isEmpty()) {
            return PlaybackQueuePlan.Rejected(PlaybackErrorMessage.noPlayableTracks)
        }

        val requestedId = requestedTrack?.let(trackId)
        val startIndex = requestedId
            ?.let { id -> playableTracks.indexOfFirst { trackId(it) == id } }
            ?.takeIf { it >= 0 }
            ?: 0

        return PlaybackQueuePlan.Ready(PlannedPlaybackQueue(playableTracks, startIndex))
    }

    fun <T> restore(
        savedQueue: List<T>,
        currentTrackId: String?,
        currentIndex: Int,
        trackId: (T) -> String,
        format: (T) -> AudioFormat,
    ): PlannedPlaybackQueue<T>? {
        val playableTracks = savedQueue.filter { format(it).isFirstPassPlayable }
        if (playableTracks.isEmpty()) return null

        val indexFromTrackId = currentTrackId
            ?.let { id -> playableTracks.indexOfFirst { trackId(it) == id } }
            ?.takeIf { it >= 0 }

        val startIndex = indexFromTrackId ?: currentIndex.coerceIn(0, playableTracks.lastIndex)
        return PlannedPlaybackQueue(playableTracks, startIndex)
    }

    fun <T> restoreLinearOrder(
        availableTracks: List<T>,
        savedQueue: List<T>,
        trackId: (T) -> String,
        format: (T) -> AudioFormat,
    ): List<T> {
        val savedIds = savedQueue.map(trackId).toSet()
        val restoredFromLibrary = availableTracks
            .filter { trackId(it) in savedIds && format(it).isFirstPassPlayable }
        return restoredFromLibrary.ifEmpty {
            savedQueue.filter { format(it).isFirstPassPlayable }
        }
    }

}
