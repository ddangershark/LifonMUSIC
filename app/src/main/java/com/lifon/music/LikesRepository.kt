package com.lifon.music

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

private const val BASE = "https://cupsize-api.usvidelsvet.workers.dev"

object LikesRepository {

    suspend fun fetchLikes(token: String): Set<Int>? = withContext(Dispatchers.IO) {
        try {
            val conn = (URL("$BASE/likes").openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("Authorization", "Bearer $token")
                connectTimeout = 6000
                readTimeout = 6000
            }
            if (conn.responseCode != 200) return@withContext null
            val text = conn.inputStream.bufferedReader().readText()
            val arr: JSONArray = JSONObject(text).getJSONArray("liked")
            (0 until arr.length()).map { arr.getInt(it) }.toSet()
        } catch (_: Exception) {
            null
        }
    }

    suspend fun addLike(token: String, trackId: Int): Boolean =
        sendLikeRequest("POST", token, trackId)

    suspend fun removeLike(token: String, trackId: Int): Boolean =
        sendLikeRequest("DELETE", token, trackId)

    private suspend fun sendLikeRequest(
        method: String,
        token: String,
        trackId: Int
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val conn = (URL("$BASE/likes").openConnection() as HttpURLConnection).apply {
                requestMethod = method
                setRequestProperty("Authorization", "Bearer $token")
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                connectTimeout = 6000
                readTimeout = 6000
                doOutput = true
            }
            val body = JSONObject().put("track_id", trackId).toString()
            conn.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
            conn.responseCode in 200..299
        } catch (_: Exception) {
            false
        }
    }
}