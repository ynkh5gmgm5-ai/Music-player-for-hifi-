package com.yuandao.music.playback

import com.yuandao.music.data.model.Track
import kotlinx.coroutines.flow.StateFlow

interface PlaybackGateway {
    val state: StateFlow<PlaybackUiState>

    suspend fun restoreIfPossible(availableTracks: List<Track>)
    fun playQueue(tracks: List<Track>, startIndex: Int = 0)
    fun playQueueTrack(trackId: String)
    fun removeQueueTrack(trackId: String)
    fun clearQueue()
    fun togglePlayPause()
    fun next()
    fun previous()
    fun seekTo(positionMs: Long)
    fun cycleRepeatMode()
    fun toggleShuffle()
    fun stopPlayback()
}
