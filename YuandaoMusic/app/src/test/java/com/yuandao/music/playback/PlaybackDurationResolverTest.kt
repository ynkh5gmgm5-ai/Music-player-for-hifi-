package com.yuandao.music.playback

import org.junit.Assert.assertEquals
import org.junit.Test

class PlaybackDurationResolverTest {
    @Test
    fun prefersPositiveTrackMetadataDuration() {
        assertEquals(
            180_000L,
            PlaybackDurationResolver.resolve(
                trackDurationMs = 180_000L,
                playerDurationMs = 200_000L,
            ),
        )
    }

    @Test
    fun fallsBackToPlayerDurationWhenTrackMetadataIsMissing() {
        assertEquals(
            20_000L,
            PlaybackDurationResolver.resolve(
                trackDurationMs = 0L,
                playerDurationMs = 20_000L,
            ),
        )
    }

    @Test
    fun returnsZeroWhenNoPositiveDurationIsKnown() {
        assertEquals(
            0L,
            PlaybackDurationResolver.resolve(
                trackDurationMs = 0L,
                playerDurationMs = -1L,
            ),
        )
    }
}
