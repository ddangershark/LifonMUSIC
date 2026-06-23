package com.lifon.music

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class Achievement(
    val id: Int,
    val name: String,
    val description: String,
    val iconUrl: String?,
    val conditionType: String,
    val conditionValue: Int
)

data class UserAchievement(
    val id: Int,
    val name: String,
    val description: String,
    val iconUrl: String?,
    val conditionType: String,
    val conditionValue: Int,
    val earnedAt: Long
)

object AchievementsRepository {

    suspend fun fetchAllAchievements(): List<Achievement> = withContext(Dispatchers.IO) {
        try {
            val conn = (URL("${ApiConfig.BASE}/achievements").openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 8000
                readTimeout = 8000
            }
            if (conn.responseCode != 200) return@withContext emptyList()
            val text = conn.inputStream.bufferedReader().readText()
            val obj = JSONObject(text)
            if (!obj.optBoolean("ok")) return@withContext emptyList()
            val arr = obj.getJSONArray("achievements")
            (0 until arr.length()).map { i ->
                val a = arr.getJSONObject(i)
                Achievement(
                    id = a.getInt("id"),
                    name = a.getString("name"),
                    description = a.optString("description", ""),
                    iconUrl = a.optString("icon_url").takeIf { it.isNotBlank() },
                    conditionType = a.getString("condition_type"),
                    conditionValue = a.getInt("condition_value")
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun fetchMyAchievements(token: String): List<UserAchievement> = withContext(Dispatchers.IO) {
        try {
            val conn = (URL("${ApiConfig.BASE}/achievements/my").openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("Authorization", "Bearer $token")
                connectTimeout = 8000
                readTimeout = 8000
            }
            if (conn.responseCode != 200) return@withContext emptyList()
            val text = conn.inputStream.bufferedReader().readText()
            val obj = JSONObject(text)
            if (!obj.optBoolean("ok")) return@withContext emptyList()
            val arr = obj.getJSONArray("achievements")
            (0 until arr.length()).map { i ->
                val a = arr.getJSONObject(i)
                UserAchievement(
                    id = a.getInt("id"),
                    name = a.getString("name"),
                    description = a.optString("description", ""),
                    iconUrl = a.optString("icon_url").takeIf { it.isNotBlank() },
                    conditionType = a.getString("condition_type"),
                    conditionValue = a.getInt("condition_value"),
                    earnedAt = a.optLong("earned_at", 0)
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }
}
