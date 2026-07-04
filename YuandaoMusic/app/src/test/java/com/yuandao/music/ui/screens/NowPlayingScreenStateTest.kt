package com.yuandao.music.ui.screens

import android.net.FakeUri
import com.yuandao.music.data.model.AudioFormat
import com.yuandao.music.data.model.AudioSource
import com.yuandao.music.data.model.AudioSourceType
import com.yuandao.music.data.model.Track
import com.yuandao.music.lyrics.TimedLyricLine
import com.yuandao.music.lyrics.TimedLyrics
import com.yuandao.music.playback.PlaybackAudioInfo
import com.yuandao.music.playback.PlaybackRepeatMode
import com.yuandao.music.playback.PlaybackUiState
import com.yuandao.music.playback.RuntimeAudioInfo
import com.yuandao.music.playback.SourceAudioInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NowPlayingScreenStateTest {
    @Test
    fun emptyPlaybackStateShowsStableEmptyCopy() {
        val state = NowPlayingScreenStateProjector.project(
            playbackState = PlaybackUiState(),
            lyrics = null,
        )

        assertFalse(state.hasTrack)
        assertNull(state.track)
        assertEquals("未在播放", state.title)
        assertEquals("选择一首本地歌曲开始播放。", state.lyricLine)
        assertEquals(0f, state.progress)
        assertEquals(0L, state.positionMs)
        assertEquals(0L, state.durationMs)
        assertEquals("0:00", state.positionLabel)
        assertEquals("0:00", state.durationLabel)
    }

    @Test
    fun activeTrackProjectionIncludesQualityProgressQueueAndLyricLine() {
        val track = track(
            id = "night-drive",
            title = "Night Drive",
            artist = "M83",
            album = "Hurry Up, We're Dreaming",
            format = AudioFormat.FLAC,
            durationMs = 240_000L,
        )
        val state = NowPlayingScreenStateProjector.project(
            playbackState = PlaybackUiState(
                queue = listOf(track),
                currentIndex = 0,
                currentTrack = track,
                isPlaying = true,
                positionMs = 96_000L,
                durationMs = 240_000L,
                repeatMode = PlaybackRepeatMode.ALL,
                shuffled = true,
                audioInfo = PlaybackAudioInfo(
                    source = SourceAudioInfo(
                        formatName = "FLAC",
                        sampleRateHz = 96_000,
                        bitDepth = 24,
                        channelCount = 2,
                    ),
                    runtime = RuntimeAudioInfo(
                        decoderName = "AudioTrack",
                        sampleRateHz = 48_000,
                        bitDepth = 16,
                        channelCount = 2,
                    ),
                ),
            ),
            lyrics = TimedLyrics(
                listOf(
                    TimedLyricLine(timeMs = 10_000L, text = "Earlier line"),
                    TimedLyricLine(timeMs = 90_000L, text = "Current lyric line"),
                    TimedLyricLine(timeMs = 120_000L, text = "Later line"),
                )
            ),
        )

        assertTrue(state.hasTrack)
        assertEquals(track, state.track)
        assertEquals("Night Drive", state.title)
        assertEquals("M83", state.artistName)
        assertEquals("Hurry Up, We're Dreaming", state.albumTitle)
        assertEquals("FLAC · 24-bit / 96kHz · 2 ch", state.sourceQualityLabel)
        assertEquals("运行: AudioTrack · 16-bit / 48kHz · 2 ch", state.runtimeQualityLabel)
        assertEquals("1 / 1 · 播放中 · 随机播放 / 列表循环", state.queueLabel)
        assertEquals("Current lyric line", state.lyricLine)
        assertEquals("1:36", state.positionLabel)
        assertEquals("4:00", state.durationLabel)
        assertEquals(96_000L, state.positionMs)
        assertEquals(240_000L, state.durationMs)
        assertEquals(0.4f, state.progress)
    }

    @Test
    fun progressIsClampedWhenPositionExceedsDuration() {
        val track = track(durationMs = 100_000L)
        val state = NowPlayingScreenStateProjector.project(
            playbackState = PlaybackUiState(
                queue = listOf(track),
                currentIndex = 0,
                currentTrack = track,
                positionMs = 150_000L,
                durationMs = 100_000L,
            ),
            lyrics = null,
        )

        assertEquals(1f, state.progress)
        assertEquals(150_000L, state.positionMs)
        assertEquals(100_000L, state.durationMs)
        assertEquals("2:30", state.positionLabel)
        assertEquals("1:40", state.durationLabel)
        assertEquals("暂无歌词", state.lyricLine)
    }

    private fun track(
        id: String = "track",
        title: String = "Track",
        artist: String = "Artist",
        album: String = "Album",
        format: AudioFormat = AudioFormat.WAV,
        durationMs: Long = 180_000L,
    ): Track =
        Track(
            id = id,
            source = AudioSource(
                type = AudioSourceType.LOCAL,
                id = "local",
                label = "Local",
            ),
            uri = FakeUri("content://local/$id"),
            displayPath = "/music/$id.${format.displayName.lowercase()}",
            fileName = "$id.${format.displayName.lowercase()}",
            title = title,
            artistId = "artist-$artist",
            artistName = artist,
            albumId = "album-$album",
            albumTitle = album,
            albumArtistName = artist,
            durationMs = durationMs,
            sizeBytes = 10_000L,
            mimeType = null,
            format = format,
            sampleRateHz = 96_000,
            bitDepth = 24,
            channelCount = 2,
            bitrateKbps = null,
            coverUri = null,
            dateModifiedMs = 1L,
            indexedAtMs = 1L,
        )
}
