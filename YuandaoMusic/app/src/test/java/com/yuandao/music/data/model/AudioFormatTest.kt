package com.yuandao.music.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AudioFormatTest {
    @Test
    fun inferRecognizesCommonFirstPassFormats() {
        assertEquals(AudioFormat.FLAC, AudioFormat.infer("audio/flac", "track.flac"))
        assertEquals(AudioFormat.WAV, AudioFormat.infer("audio/wav", "track.wav"))
        assertEquals(AudioFormat.MP3, AudioFormat.infer("audio/mpeg", "track.mp3"))
        assertEquals(AudioFormat.AAC, AudioFormat.infer("audio/mp4a-latm", "track.m4a"))
        assertEquals(AudioFormat.ALAC, AudioFormat.infer("audio/alac", "track.m4a"))
    }

    @Test
    fun inferRecognizesDeferredHiFiFormats() {
        assertEquals(AudioFormat.APE, AudioFormat.infer(null, "album.ape"))
        assertEquals(AudioFormat.DSD, AudioFormat.infer(null, "album.dff"))
        assertEquals(AudioFormat.DSD, AudioFormat.infer(null, "album.dsf"))
        assertEquals(AudioFormat.CUE, AudioFormat.infer(null, "album.cue"))
    }

    @Test
    fun firstPassPlaybackBoundaryIsExplicit() {
        assertTrue(AudioFormat.FLAC.isFirstPassPlayable)
        assertTrue(AudioFormat.WAV.isFirstPassPlayable)
        assertTrue(AudioFormat.ALAC.isFirstPassPlayable)
        assertFalse(AudioFormat.APE.isFirstPassPlayable)
        assertFalse(AudioFormat.DSD.isFirstPassPlayable)
        assertFalse(AudioFormat.CUE.isFirstPassPlayable)
    }
}
