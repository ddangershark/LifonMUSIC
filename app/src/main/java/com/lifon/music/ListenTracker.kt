package com.lifon.music

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

private const val BASE = "https://cupsize-api.usvidelsvet.workers.dev"

object ListenTracker {

    suspend fun recordListen(token: String, trackId: Int, durationMs: Long): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val conn = (URL("$BASE/listens").openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("Authorization", "Bearer $token")
                    setRequestProperty("Content-Type", "application/json; charset=utf-8")
                    connectTimeout = 6000
                    readTimeout = 6000
                    doOutput = true
                }
                val body = JSONObject()
                    .put("track_id", trackId)
                    .put("duration_ms", durationMs)
                    .toString()
                conn.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
                conn.responseCode in 200..299
            } catch (_: Exception) {
                false
            }
        }

    suspend fun fetchStats(token: String): StatsResult? = withContext(Dispatchers.IO) {
        try {
            val conn = (URL("$BASE/stats").openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("Authorization", "Bearer $token")
                connectTimeout = 6000
                readTimeout = 6000
            }
            if (conn.responseCode != 200) return@withContext null
            val text = conn.inputStream.bufferedReader().readText()
            val obj = JSONObject(text)
            val arr = obj.getJSONArray("top_tracks")
            val top = (0 until arr.length()).map {
                val item = arr.getJSONObject(it)
                TopTrack(item.getInt("track_id"), item.getInt("play_count"))
            }
            StatsResult(
                topTracks = top,
                totalMs = obj.getLong("total_ms")
            )
        } catch (_: Exception) {
            null
        }
    }
}

data class TopTrack(val trackId: Int, val playCount: Int)
data class StatsResult(val topTracks: List<TopTrack>, val totalMs: Long)