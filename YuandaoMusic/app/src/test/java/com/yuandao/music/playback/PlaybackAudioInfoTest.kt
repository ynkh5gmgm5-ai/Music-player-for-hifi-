package com.yuandao.music.playback

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaybackAudioInfoTest {
    @Test
    fun sourceQualityLabelUsesScannedFileMetadataOnly() {
        val info = PlaybackAudioInfo(
            source = SourceAudioInfo(
                formatName = "FLAC",
                sampleRateHz = 96_000,
                bitDepth = 24,
                channelCount = 2,
                bitrateKbps = 2_400,
            ),
            runtime = RuntimeAudioInfo(
                codecName = "pcm",
                sampleRateHz = 48_000,
                bitDepth = 16,
                channelCount = 2,
                decoderName = "c2.android.flac.decoder",
            ),
        )

        assertEquals("24-bit / 96kHz", info.source.qualityLabel)
        assertEquals("24-bit / 96kHz", info.qualityLabel)
        assertFalse(info.source.technicalLabel.contains("48kHz"))
    }

    @Test
    fun runtimeTechnicalLabelDoesNotPretendToBeSourceQuality() {
        val info = PlaybackAudioInfo(
            source = SourceAudioInfo(
                formatName = "ALAC",
                sampleRateHz = 192_000,
                bitDepth = 24,
            ),
            runtime = RuntimeAudioInfo(
                codecName = "raw",
                sampleRateHz = 48_000,
                bitDepth = 16,
                channelCount = 2,
                decoderName = "AudioTrack",
            ),
        )

        assertEquals("ALAC · 24-bit / 192kHz", info.sourceTechnicalLabel)
        assertTrue(info.runtimeTechnicalLabel.contains("AudioTrack"))
        assertTrue(info.runtimeTechnicalLabel.contains("16-bit / 48kHz"))
        assertFalse(info.runtimeTechnicalLabel.contains("192kHz"))
    }
}
