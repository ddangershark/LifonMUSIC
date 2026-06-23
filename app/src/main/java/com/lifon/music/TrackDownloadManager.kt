package com.lifon.music

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.net.Uri
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

enum class DownloadStatus { NONE, DOWNLOADING, DONE }

data class DownloadedTrack(
    val id: Int,
    val title: String,
    val artist: String,
    val duration: String,
    val coverUrl: String?
)

object TrackDownloadManager {

    private val _statuses = mutableMapOf<Int, DownloadStatus>()
    private val _statusFlows = mutableMapOf<Int, MutableStateFlow<DownloadStatus>>()
    private val lock = Any()

    private var dm: DownloadManager? = null
    private var appContext: Context? = null
    private var receiverRegistered = false

    private fun getMusicDir(context: Context): java.io.File {
        val dir = java.io.File(context.getExternalFilesDir(null), "Music")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun init(context: Context) {
        if (dm != null) return
        appContext = context.applicationContext
        dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        registerReceiver(context)
    }

    fun getStatusFlow(trackId: Int): StateFlow<DownloadStatus> {
        synchronized(lock) {
            return _statusFlows.getOrPut(trackId) {
                val initial = if (isDownloaded(trackId)) DownloadStatus.DONE else DownloadStatus.NONE
                MutableStateFlow(initial)
            }
        }
    }

    fun isDownloaded(context: Context, trackId: Int): Boolean {
        val dir = getMusicDir(context)
        return dir.listFiles()
            ?.any { it.name.startsWith("track_$trackId") && it.length() > 0 } == true
    }

    fun isDownloaded(trackId: Int): Boolean {
        val ctx = appContext ?: return false
        return isDownloaded(ctx, trackId)
    }

    fun getLocalFile(context: Context, trackId: Int): java.io.File? {
        val dir = getMusicDir(context)
        return dir.listFiles()?.firstOrNull {
            it.name.startsWith("track_$trackId") && it.length() > 0
        }
    }

    fun getLocalCover(context: Context, trackId: Int): java.io.File? {
        val dir = getMusicDir(context)
        return dir.listFiles()?.firstOrNull {
            it.name == "cover_$trackId.jpg" && it.length() > 0
        }
    }

    fun getDownloadedTracks(context: Context): List<DownloadedTrack> {
        val prefs = context.getSharedPreferences("lifon_prefs", Context.MODE_PRIVATE)
        val raw = prefs.getString("downloaded_tracks", "{}") ?: "{}"
        val json = runCatching { JSONObject(raw) }.getOrNull() ?: return emptyList()

        val result = mutableListOf<DownloadedTrack>()
        for (key in json.keys()) {
            val id = key.toIntOrNull() ?: continue
            if (!isDownloaded(context, id)) continue
            val obj = json.getJSONObject(key)
            result.add(
                DownloadedTrack(
                    id = id,
                    title = obj.optString("title", "Трек #$id"),
                    artist = obj.optString("artist", "CUPSIZE"),
                    duration = obj.optString("duration", "0:00"),
                    coverUrl = obj.optString("coverUrl").takeIf { it.isNotBlank() }
                )
            )
        }
        return result
    }

    fun download(context: Context, track: Track, coverUrl: String?) {
        if (isDownloaded(context, track.id)) return
        synchronized(lock) {
            if (_statuses[track.id] == DownloadStatus.DOWNLOADING) return
            _statuses[track.id] = DownloadStatus.DOWNLOADING
        }
        notifyFlow(track.id, DownloadStatus.DOWNLOADING)

        val dir = getMusicDir(context)

        val ext = when {
            track.audioUrl.contains(".opus") -> ".opus"
            track.audioUrl.contains(".ogg") -> ".ogg"
            track.audioUrl.contains(".m4a") -> ".m4a"
            track.audioUrl.contains(".wav") -> ".wav"
            track.audioUrl.contains(".flac") -> ".flac"
            else -> ".mp3"
        }

        val fileName = "track_${track.id}$ext"
        val file = java.io.File(dir, fileName)

        val request = DownloadManager.Request(Uri.parse(track.audioUrl))
            .setTitle(track.title)
            .setDescription("LifonMUSIC")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationUri(Uri.fromFile(file))
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val downloadId = dm?.enqueue(request) ?: run {
            synchronized(lock) { _statuses[track.id] = DownloadStatus.NONE }
            notifyFlow(track.id, DownloadStatus.NONE)
            return
        }

        storeMetadata(context, track, coverUrl)
        storeDownloadId(context, track.id, downloadId)

        if (!coverUrl.isNullOrBlank()) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val url = java.net.URL(coverUrl)
                    val conn = url.openConnection() as java.net.HttpURLConnection
                    conn.connectTimeout = 8000
                    conn.readTimeout = 8000
                    if (conn.responseCode == 200) {
                        val bytes = conn.inputStream.readBytes()
                        val coverFile = java.io.File(getMusicDir(context), "cover_${track.id}.jpg")
                        coverFile.writeBytes(bytes)
                    }
                } catch (_: Exception) { }
            }
        }
    }

    fun delete(context: Context, trackId: Int) {
        val dir = getMusicDir(context)
        dir.listFiles()
            ?.filter { it.name.startsWith("track_$trackId") }
            ?.forEach { it.delete() }

        val prefs = context.getSharedPreferences("lifon_prefs", Context.MODE_PRIVATE)
        val raw = prefs.getString("downloaded_tracks", "{}") ?: "{}"
        val json = runCatching { JSONObject(raw) }.getOrNull()
        if (json != null && json.has(trackId.toString())) {
            json.remove(trackId.toString())
            prefs.edit().putString("downloaded_tracks", json.toString()).apply()
        }

        synchronized(lock) { _statuses[trackId] = DownloadStatus.NONE }
        notifyFlow(trackId, DownloadStatus.NONE)
    }

    private fun notifyFlow(trackId: Int, status: DownloadStatus) {
        synchronized(lock) {
            _statusFlows[trackId]?.value = status
        }
    }

    private fun storeMetadata(context: Context, track: Track, coverUrl: String?) {
        val prefs = context.getSharedPreferences("lifon_prefs", Context.MODE_PRIVATE)
        val raw = prefs.getString("downloaded_tracks", "{}") ?: "{}"
        val json = runCatching { JSONObject(raw) }.getOrNull() ?: JSONObject()
        json.put(track.id.toString(), JSONObject().apply {
            put("title", track.title)
            put("artist", track.displayArtist)
            put("duration", track.duration)
            put("coverUrl", coverUrl ?: "")
        })
        prefs.edit().putString("downloaded_tracks", json.toString()).apply()
    }

    private fun storeDownloadId(context: Context, trackId: Int, downloadId: Long) {
        val prefs = context.getSharedPreferences("lifon_prefs", Context.MODE_PRIVATE)
        val set = prefs.getStringSet("download_ids", emptySet())?.toMutableSet() ?: mutableSetOf()
        set.add("$trackId:$downloadId")
        prefs.edit().putStringSet("download_ids", set).apply()
    }

    private fun registerReceiver(context: Context) {
        if (receiverRegistered) return
        receiverRegistered = true

        val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        ContextCompat.registerReceiver(
            context,
            object : BroadcastReceiver() {
                override fun onReceive(ctx: Context, intent: Intent) {
                    val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                    if (downloadId == -1L) return

                    val prefs = ctx.getSharedPreferences("lifon_prefs", Context.MODE_PRIVATE)
                    val set = prefs.getStringSet("download_ids", emptySet())?.toMutableSet() ?: return
                    val entry = set.firstOrNull { it.endsWith(":$downloadId") } ?: return
                    val trackId = entry.substringBefore(":").toIntOrNull() ?: return

                    val query = DownloadManager.Query().setFilterById(downloadId)
                    val cursor: Cursor? = dm?.query(query)
                    cursor?.use {
                        if (it.moveToFirst()) {
                            val col = it.getColumnIndex(DownloadManager.COLUMN_STATUS)
                            if (col >= 0) {
                                val status = it.getInt(col)
                                val newStatus = if (status == DownloadManager.STATUS_SUCCESSFUL) {
                                    DownloadStatus.DONE
                                } else {
                                    DownloadStatus.NONE
                                }
                                synchronized(lock) { _statuses[trackId] = newStatus }
                                notifyFlow(trackId, newStatus)
                            }
                        }
                    }

                    set.remove(entry)
                    prefs.edit().putStringSet("download_ids", set).apply()
                }
            },
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }
}
