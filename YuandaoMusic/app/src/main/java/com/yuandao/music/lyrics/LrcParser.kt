package com.yuandao.music.lyrics

data class TimedLyricLine(
    val timeMs: Long,
    val text: String,
)

data class TimedLyrics(
    val lines: List<TimedLyricLine>,
) {
    fun lineAt(positionMs: Long): TimedLyricLine? =
        lines.lastOrNull { it.timeMs <= positionMs }
}

object LrcParser {
    private val timestampRegex = Regex("\\[(\\d{1,2}):(\\d{2})(?:\\.(\\d{1,3}))?]")
    private val offsetRegex = Regex("\\[offset:([+-]?\\d+)]", RegexOption.IGNORE_CASE)

    fun parse(raw: String): TimedLyrics {
        val offsetMs = offsetRegex.find(raw)
            ?.groupValues
            ?.getOrNull(1)
            ?.toLongOrNull()
            ?: 0L
        val lines = raw.lineSequence()
            .flatMap { line -> parseLine(line).asSequence() }
            .map { line -> line.copy(timeMs = (line.timeMs + offsetMs).coerceAtLeast(0L)) }
            .sortedBy { it.timeMs }
            .toList()
        return TimedLyrics(lines)
    }

    private fun parseLine(line: String): List<TimedLyricLine> {
        val matches = timestampRegex.findAll(line).toList()
        if (matches.isEmpty()) return emptyList()
        val text = line.replace(timestampRegex, "").trim()
        return matches.mapNotNull { match ->
            val minutes = match.groupValues[1].toLongOrNull() ?: return@mapNotNull null
            val seconds = match.groupValues[2].toLongOrNull() ?: return@mapNotNull null
            val fraction = match.groupValues.getOrNull(3).orEmpty()
            val millis = when (fraction.length) {
                0 -> 0L
                1 -> fraction.toLong() * 100
                2 -> fraction.toLong() * 10
                else -> fraction.take(3).toLong()
            }
            TimedLyricLine(
                timeMs = minutes * 60_000 + seconds * 1000 + millis,
                text = text,
            )
        }
    }
}
