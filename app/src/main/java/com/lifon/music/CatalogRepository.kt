package com.lifon.music

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

private val SITE_ORIGIN: String = run {
    val base = ApiConfig.BASE
    val idx = base.indexOf("/api")
    if (idx > 0) base.substring(0, idx) else base.trimEnd('/')
}

private fun toAbsoluteUrl(raw: String?): String? {
    if (raw.isNullOrBlank() || raw == "null") return null
    if (raw.startsWith("http://") || raw.startsWith("https://")) return raw
    return if (raw.startsWith("/")) "$SITE_ORIGIN$raw" else "$SITE_ORIGIN/$raw"
}

sealed class FetchResult {
    data class Success(val albums: List<Album>) : FetchResult()
    data class Maintenance(val message: String) : FetchResult()
    object Offline : FetchResult()
    data class Error(val message: String = "Неизвестная ошибка") : FetchResult()
}

object CatalogRepository {

    suspend fun fetchAlbums(): FetchResult = withContext(Dispatchers.IO) {
        try {
            val conn = (URL("${ApiConfig.BASE}/albums").openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 10_000
                readTimeout = 10_000
            }
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val text = stream?.bufferedReader()?.use { it.readText() }.orEmpty()

            if (code == 503) {
                val obj = runCatching { JSONObject(text) }.getOrNull()
                if (obj?.optString("error") == "maintenance") {
                    val msg = obj.optString("message", "Сервис временно недоступен")
                    return@withContext FetchResult.Maintenance(msg)
                }
            }

            if (code != 200) return@withContext FetchResult.Error("Ошибка сервера ($code)")

            val obj = JSONObject(text)
            if (!obj.optBoolean("ok")) return@withContext FetchResult.Error("Сервер вернул ошибку")
            val arr = obj.getJSONArray("albums")

            val albums = (0 until arr.length()).map { i ->
                val a = arr.getJSONObject(i)
                val tracksArr = a.getJSONArray("tracks")
                val albumCoverUrl = toAbsoluteUrl(
                    a.optString("cover_url").ifBlank { a.optString("cover") }
                )
                Album(
                    id = a.getInt("id"),
                    title = a.getString("title"),
                    year = a.getString("year"),
                    coverUrl = albumCoverUrl,
                    glowColor = a.optString("glow_color").takeIf { it.isNotBlank() },
                    tracks = (0 until tracksArr.length()).map { j ->
                        val t = tracksArr.getJSONObject(j)
                        Track(
                            id = t.getInt("id"),
                            title = t.getString("title"),
                            duration = t.optString("duration", "0:00"),
                            audioUrl = toAbsoluteUrl(
                                t.optString("audio_url").ifBlank { t.optString("audio") }
                            ) ?: "",
                            albumId = t.optInt("album_id").takeIf { it > 0 }
                                ?: t.optInt("albumId").takeIf { it > 0 }
                                ?: a.getInt("id"),
                            artist = t.optString("artist", "CUPSIZE"),
                            coverUrl = toAbsoluteUrl(t.optString("cover_url"))
                        )
                    }
                )
            }
            FetchResult.Success(albums)
        } catch (e: java.net.UnknownHostException) {
            FetchResult.Offline
        } catch (e: java.net.ConnectException) {
            FetchResult.Offline
        } catch (e: java.net.SocketTimeoutException) {
            FetchResult.Offline
        } catch (_: java.io.IOException) {
            FetchResult.Offline
        } catch (_: Exception) {
            FetchResult.Error("Нет соединения")
        }
    }
}
