package com.yuandao.music.ui.screens

import com.yuandao.music.data.model.Track
import com.yuandao.music.lyrics.TimedLyrics
import com.yuandao.music.playback.PlaybackUiState

data class NowPlayingScreenState(
    val hasTrack: Boolean,
    val track: Track?,
    val title: String,
    val artistName: String,
    val albumTitle: String,
    val sourceQualityLabel: String,
    val runtimeQualityLabel: String,
    val queueLabel: String,
    val lyricLine: String,
    val positionMs: Long,
    val durationMs: Long,
    val positionLabel: String,
    val durationLabel: String,
    val progress: Float,
    val isPlaying: Boolean,
    val isBuffering: Boolean,
    val errorMessage: String?,
)

object NowPlayingScreenStateProjector {
    fun project(
        playbackState: PlaybackUiState,
        lyrics: TimedLyrics?,
    ): NowPlayingScreenState {
        val track = playbackState.currentTrack
        if (track == null) {
            return NowPlayingScreenState(
                hasTrack = false,
                track = null,
                title = "未在播放",
                artistName = "",
                albumTitle = "",
                sourceQualityLabel = "",
                runtimeQualityLabel = "",
                queueLabel = playbackState.queueSummary.statusLabel,
                lyricLine = "选择一首本地歌曲开始播放。",
                positionMs = 0L,
                durationMs = 0L,
                positionLabel = "0:00",
                durationLabel = "0:00",
                progress = 0f,
                isPlaying = false,
                isBuffering = false,
                errorMessage = playbackState.errorMessage,
            )
        }

        val durationMs = playbackState.durationMs.takeIf { it > 0 } ?: track.durationMs
        val progress = if (durationMs > 0) {
            playbackState.positionMs.toFloat() / durationMs.toFloat()
        } else {
            0f
        }.coerceIn(0f, 1f)
        val sourceLabel = playbackState.audioInfo.sourceTechnicalLabel
            .ifBlank { "${track.format.displayName} · ${track.qualityLabel}" }
        val runtimeLabel = playbackState.audioInfo.runtimeTechnicalLabel
            .takeIf { it.isNotBlank() }
            ?.let { "运行: $it" }
            .orEmpty()

        return NowPlayingScreenState(
            hasTrack = true,
            track = track,
            title = track.title,
            artistName = track.artistName,
            albumTitle = track.albumTitle,
            sourceQualityLabel = sourceLabel,
            runtimeQualityLabel = runtimeLabel,
            queueLabel = listOf(
                playbackState.queueSummary.positionLabel,
                playbackState.queueSummary.statusLabel,
                playbackState.queueSummary.modeLabel,
            ).joinToString(" · "),
            lyricLine = lyrics?.lineAt(playbackState.positionMs)?.text ?: "暂无歌词",
            positionMs = playbackState.positionMs,
            durationMs = durationMs,
            positionLabel = formatDurationLabel(playbackState.positionMs),
            durationLabel = formatDurationLabel(durationMs),
            progress = progress,
            isPlaying = playbackState.isPlaying,
            isBuffering = playbackState.isBuffering,
            errorMessage = playbackState.errorMessage,
        )
    }

    private fun formatDurationLabel(durationMs: Long): String {
        val totalSeconds = (durationMs / 1000).coerceAtLeast(0)
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "$minutes:${seconds.toString().padStart(2, '0')}"
    }
}
