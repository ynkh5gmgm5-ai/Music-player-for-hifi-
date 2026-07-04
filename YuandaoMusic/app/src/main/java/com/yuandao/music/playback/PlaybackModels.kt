package com.yuandao.music.playback

import com.yuandao.music.data.model.Track

enum class PlaybackRepeatMode {
    NONE,
    ONE,
    ALL,
}

data class PlaybackUiState(
    val queue: List<Track> = emptyList(),
    val currentIndex: Int = -1,
    val currentTrack: Track? = null,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val audioInfo: PlaybackAudioInfo = PlaybackAudioInfo(),
    val repeatMode: PlaybackRepeatMode = PlaybackRepeatMode.NONE,
    val shuffled: Boolean = false,
    val errorMessage: String? = null,
) {
    val hasQueue: Boolean = queue.isNotEmpty()
    val queueSummary: PlaybackQueueSummary
        get() = PlaybackQueueSummarizer.summarize(
            queueSize = queue.size,
            currentIndex = currentIndex,
            isPlaying = isPlaying,
            isBuffering = isBuffering,
            shuffled = shuffled,
            repeatMode = repeatMode,
            errorMessage = errorMessage,
        )
    val queueDrawerState: PlaybackQueueDrawerState
        get() = PlaybackQueueDrawerProjector.project(
            queue = queue,
            currentIndex = currentIndex,
            shuffled = shuffled,
            repeatMode = repeatMode,
        ) { track, isCurrent ->
            PlaybackQueueDrawerItem(
                id = track.id,
                title = track.title,
                artistName = track.artistName,
                qualityLabel = track.qualityLabel,
                isCurrent = isCurrent,
            )
        }
}

data class SourceAudioInfo(
    val formatName: String? = null,
    val sampleRateHz: Int? = null,
    val bitDepth: Int? = null,
    val channelCount: Int? = null,
    val bitrateKbps: Int? = null,
) {
    val qualityLabel: String
        get() {
            val depth = bitDepth?.let { "$it-bit" }
            val rate = sampleRateHz?.let { "${it / 1000.0}".trimEndZero() + "kHz" }
            return listOfNotNull(depth, rate).joinToString(" / ")
        }

    val technicalLabel: String
        get() = listOfNotNull(
            formatName,
            qualityLabel.takeIf { it.isNotBlank() },
            channelCount?.let { "$it ch" },
            bitrateKbps?.let { "$it kbps" },
        ).joinToString(" · ")
}

data class RuntimeAudioInfo(
    val codecName: String? = null,
    val sampleRateHz: Int? = null,
    val bitDepth: Int? = null,
    val channelCount: Int? = null,
    val bitrateKbps: Int? = null,
    val decoderName: String? = null,
) {
    val qualityLabel: String
        get() {
            val depth = bitDepth?.let { "$it-bit" }
            val rate = sampleRateHz?.let { "${it / 1000.0}".trimEndZero() + "kHz" }
            return listOfNotNull(depth, rate).joinToString(" / ")
        }

    val technicalLabel: String
        get() = listOfNotNull(
            decoderName,
            codecName,
            qualityLabel.takeIf { it.isNotBlank() },
            channelCount?.let { "$it ch" },
            bitrateKbps?.let { "$it kbps" },
        ).joinToString(" · ")
}

data class PlaybackAudioInfo(
    val source: SourceAudioInfo = SourceAudioInfo(),
    val runtime: RuntimeAudioInfo = RuntimeAudioInfo(),
) {
    val qualityLabel: String
        get() = source.qualityLabel

    val sourceTechnicalLabel: String
        get() = source.technicalLabel

    val runtimeTechnicalLabel: String
        get() = runtime.technicalLabel

    val technicalLabel: String
        get() = listOfNotNull(
            sourceTechnicalLabel.takeIf { it.isNotBlank() },
            runtimeTechnicalLabel.takeIf { it.isNotBlank() }?.let { "运行: $it" },
        ).joinToString(" · ")
}

private fun String.trimEndZero(): String =
    trimEnd('0').trimEnd('.')
