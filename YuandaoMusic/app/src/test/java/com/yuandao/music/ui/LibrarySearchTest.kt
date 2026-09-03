package com.yuandao.music.ui

import android.net.FakeUri
import com.yuandao.music.data.model.AudioFormat
import com.yuandao.music.data.model.AudioSource
import com.yuandao.music.data.model.AudioSourceType
import com.yuandao.music.data.model.Track
import org.junit.Assert.assertEquals
import org.junit.Test

class LibrarySearchTest {
    private val tracks = listOf(
        track(
            id = "one",
            title = "夜曲",
            artist = "周杰伦",
            album = "十一月的肖邦",
        ),
        track(
            id = "two",
            title = "Imagine",
            artist = "John Lennon",
            album = "Imagine",
        ),
        track(
            id = "three",
            title = "Blue Train",
            artist = "John Coltrane",
            album = "Blue Train",
        ),
    )

    @Test
    fun blankQueryPreservesLibraryOrder() {
        assertEquals(
            listOf("one", "two", "three"),
            LibrarySearch.filterTracks(tracks, " ").map { it.id },
        )
    }

    @Test
    fun matchesTitleArtistAndAlbum() {
        assertEquals(listOf("one"), LibrarySearch.filterTracks(tracks, "夜曲").map { it.id })
        assertEquals(listOf("two", "three"), LibrarySearch.filterTracks(tracks, "john").map { it.id })
        assertEquals(listOf("one"), LibrarySearch.filterTracks(tracks, "肖邦").map { it.id })
    }

    @Test
    fun matchingIgnoresCaseAndSurroundingWhitespace() {
        assertEquals(
            listOf("two"),
            LibrarySearch.filterTracks(tracks, "  imagine  ").map { it.id },
        )
    }

    @Test
    fun unmatchedQueryReturnsEmptyList() {
        assertEquals(emptyList<Track>(), LibrarySearch.filterTracks(tracks, "不存在"))
    }

    private fun track(
        id: String,
        title: String,
        artist: String,
        album: String,
    ): Track = Track(
        id = id,
        source = AudioSource(AudioSourceType.LOCAL, "source-$id", "本机曲库"),
        uri = FakeUri("content://music/$id"),
        displayPath = "/music/$id.flac",
        fileName = "$id.flac",
        title = title,
        artistId = "artist-$artist",
        artistName = artist,
        albumId = "album-$album",
        albumTitle = album,
        albumArtistName = artist,
        durationMs = 180_000L,
        sizeBytes = 10_000L,
        mimeType = "audio/flac",
        format = AudioFormat.FLAC,
        sampleRateHz = 96_000,
        bitDepth = 24,
        channelCount = 2,
        bitrateKbps = 2_400,
        coverUri = null,
        dateModifiedMs = 1L,
        indexedAtMs = 1L,
    )
}
