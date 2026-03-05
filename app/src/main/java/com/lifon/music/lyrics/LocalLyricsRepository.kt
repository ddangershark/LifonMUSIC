package com.lifon.music.lyrics

import android.content.Context
import android.util.LruCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object LocalLyricsRepository {

    // кэшируем результат, чтобы не читать asset заново при каждом открытии
    private val cache = LruCache<Int, List<LyricsLine>?>(120)

    suspend fun getLyrics(context: Context, trackId: Int): List<LyricsLine>? =
        withContext(Dispatchers.IO) {
            cache.get(trackId)?.let { return@withContext it }

            val candidates = listOf(
                "lyrics/$trackId.lrc",
                "lyrics/$trackId.txt"
            )

            val text = candidates.firstNotNullOfOrNull { path ->
                runCatching {
                    context.assets.open(path).bufferedReader().use { it.readText() }
                }.getOrNull()
            }

            val lines = text?.let { parseAny(it) }
            cache.put(trackId, lines)
            lines
        }

    private fun parseAny(text: String): List<LyricsLine>? {
        val lrc = LrcParser.parse(text)
        if (lrc.isNotEmpty()) return lrc

        val plain = text.lines().map { it.trim() }.filter { it.isNotBlank() }
        if (plain.isEmpty()) return null

        // без таймкодов — просто показываем строки (псевдо-время)
        return plain.mapIndexed { i, line -> LyricsLine(i * 3_000L, line) }
    }
}