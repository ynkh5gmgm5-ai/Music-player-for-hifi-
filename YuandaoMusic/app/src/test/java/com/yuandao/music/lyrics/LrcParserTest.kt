package com.yuandao.music.lyrics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LrcParserTest {
    @Test
    fun parsesMultipleTimestampsOnOneLine() {
        val lyrics = LrcParser.parse("[00:01.00][00:02.50]hello")

        assertEquals(listOf(1000L, 2500L), lyrics.lines.map { it.timeMs })
        assertEquals(listOf("hello", "hello"), lyrics.lines.map { it.text })
    }

    @Test
    fun appliesGlobalOffsetAndSortsLines() {
        val lyrics = LrcParser.parse(
            """
            [offset:+250]
            [00:02.005]second
            [00:01.50]first
            """.trimIndent()
        )

        assertEquals(listOf(1750L, 2255L), lyrics.lines.map { it.timeMs })
        assertEquals("first", lyrics.lines[0].text)
        assertEquals("second", lyrics.lines[1].text)
    }

    @Test
    fun lineAtReturnsLatestElapsedLine() {
        val lyrics = LrcParser.parse(
            """
            [00:01.00]first
            [00:03.00]second
            """.trimIndent()
        )

        assertNull(lyrics.lineAt(500))
        assertEquals("first", lyrics.lineAt(1500)?.text)
        assertEquals("second", lyrics.lineAt(4000)?.text)
    }
}
