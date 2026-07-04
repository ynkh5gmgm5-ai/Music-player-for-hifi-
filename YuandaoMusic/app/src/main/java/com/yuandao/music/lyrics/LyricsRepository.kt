package com.yuandao.music.lyrics

import android.content.Context
import com.yuandao.music.data.model.Track
import java.io.File
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.nio.charset.CodingErrorAction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LyricsRepository(
    @Suppress("unused") private val context: Context,
) {
    suspend fun loadLyrics(track: Track?): TimedLyrics? =
        withContext(Dispatchers.IO) {
            if (track == null) return@withContext null
            val lrcFile = findSidecarLrc(track) ?: return@withContext null
            runCatching { LrcParser.parse(lrcFile.readLyricsText()) }.getOrNull()
        }

    private fun findSidecarLrc(track: Track): File? {
        val displayPath = track.displayPath ?: return null
        val audioFile = File(displayPath)
        val directory = audioFile.parentFile ?: return null
        val baseName = audioFile.name.substringBeforeLast('.', audioFile.name)
        val candidates = listOf(
            File(directory, "$baseName.lrc"),
            File(directory, "${track.title}.lrc"),
        )
        return candidates.firstOrNull { it.isFile && it.canRead() }
    }

    private fun File.readLyricsText(): String {
        val bytes = readBytes()
        return listOf(Charsets.UTF_8, Charset.forName("GB18030"))
            .firstNotNullOfOrNull { charset -> decodeStrict(bytes, charset) }
            ?: bytes.toString(Charsets.UTF_8)
    }

    private fun decodeStrict(bytes: ByteArray, charset: Charset): String? =
        runCatching {
            charset.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
                .decode(ByteBuffer.wrap(bytes))
                .toString()
        }.getOrNull()
}
