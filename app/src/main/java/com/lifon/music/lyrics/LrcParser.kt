package com.lifon.music.lyrics

object LrcParser {

    // Поддержка [MM:SS.xx], [MM:SS.xxx], [MM:SS:xx]
    private val TIME_REGEX = Regex("""\[(\d{2}):(\d{2})[.:](\d{2,3})]""")

    fun parse(lrc: String): List<LyricsLine> {
        return lrc.lines()
            .mapNotNull { line ->
                val match = TIME_REGEX.find(line) ?: return@mapNotNull null
                val (min, sec, ms) = match.destructured
                val millis = min.toLong() * 60_000L +
                        sec.toLong() * 1_000L +
                        ms.toLong() * if (ms.length == 2) 10L else 1L
                val text = line.substring(match.range.last + 1).trim()
                if (text.isBlank()) null else LyricsLine(millis, text)
            }
            .sortedBy { it.timeMs }
    }
}