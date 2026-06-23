package com.lifon.music.lyrics

import android.content.Context
import android.util.LruCache
import com.lifon.music.ApiConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

object LocalLyricsRepository {

    // кэшируем результат, чтобы не читать asset/сеть заново при каждом открытии
    private val cache = LruCache<Int, List<LyricsLine>?>(120)

    suspend fun getLyrics(context: Context, trackId: Int): List<LyricsLine>? =
        withContext(Dispatchers.IO) {
            cache.get(trackId)?.let { return@withContext it }

            // 1. Сначала ищем локально в assets
            val candidates = listOf("lyrics/$trackId.lrc", "lyrics/$trackId.txt")
            val localText = candidates.firstNotNullOfOrNull { path ->
                runCatching {
                    context.assets.open(path).bufferedReader().use { it.readText() }
                }.getOrNull()
            }
            if (localText != null) {
                val lines = parseAny(localText)
                if (lines != null) cache.put(trackId, lines)
                return@withContext lines
            }

            // 2. Если нет локально — запрашиваем с сервера
            val apiText = runCatching {
                val conn = (URL("${ApiConfig.BASE}/albums/tracks/$trackId/lyrics")
                    .openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 6000
                    readTimeout = 6000
                }
                if (conn.responseCode != 200) return@runCatching null
                val body = conn.inputStream.bufferedReader().readText()
                val obj = org.json.JSONObject(body)
                obj.optString("lrc").takeIf { it.isNotBlank() }
            }.getOrNull()

            val lines = apiText?.let { parseAny(it) }
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