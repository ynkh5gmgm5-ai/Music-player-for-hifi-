package com.yuandao.music.playback

class PlaybackHistoryGate(
    private val minimumListenMs: Long = 30_000L,
) {
    private var currentTrackId: String? = null
    private var currentTrackStartedAtMs: Long = 0L
    private var recordedTrackId: String? = null

    fun onCurrentTrackChanged(trackId: String?, nowMs: Long) {
        if (trackId == currentTrackId) return
        currentTrackId = trackId
        currentTrackStartedAtMs = nowMs
        recordedTrackId = null
    }

    fun shouldRecord(
        trackId: String?,
        positionMs: Long,
        nowMs: Long,
        durationMs: Long = 0L,
    ): Boolean {
        if (trackId == null || trackId != currentTrackId || trackId == recordedTrackId) return false
        val playedMostOfShortTrack = durationMs in 1 until minimumListenMs &&
            positionMs >= (durationMs * SHORT_TRACK_COMPLETION_RATIO).toLong()
        val listenedLongEnough = positionMs >= minimumListenMs ||
            playedMostOfShortTrack ||
            nowMs - currentTrackStartedAtMs >= minimumListenMs
        if (!listenedLongEnough) return false
        recordedTrackId = trackId
        return true
    }

    private companion object {
        const val SHORT_TRACK_COMPLETION_RATIO = 0.7
    }
}
