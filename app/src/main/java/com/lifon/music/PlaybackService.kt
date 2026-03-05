package com.lifon.music

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import androidx.media.session.MediaButtonReceiver
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import kotlinx.coroutines.*

class PlaybackService : MediaSessionService(), Player.Listener {

    private lateinit var player: ExoPlayer
    private lateinit var compatSession: MediaSessionCompat
    private lateinit var mediaSession: MediaSession

    private val notificationId = 1001
    private val channelId = "playback_channel"

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val nm by lazy { getSystemService(NotificationManager::class.java) }

    private var notifyJob: Job? = null
    private var lastArtworkHash: Int = 0
    private var cachedLargeIcon: Bitmap? = null
    private var decodeJob: Job? = null
    private var isDecoding = false

    private var lastTitle = ""
    private var lastArtist = ""
    private var lastIsPlaying = false

    override fun onCreate() {
        super.onCreate()

        player = PlayerHolder.get(this)
        player.addListener(this)

        mediaSession = MediaSession.Builder(this, player).build()

        compatSession = MediaSessionCompat(this, "CupSizeSession").apply {
            setCallback(object : MediaSessionCompat.Callback() {
                override fun onPlay() = player.play()
                override fun onPause() = player.pause()
                override fun onSkipToNext() { PlayerCallbacks.onNext?.invoke() }
                override fun onSkipToPrevious() { PlayerCallbacks.onPrev?.invoke() }
                override fun onSeekTo(pos: Long) = player.seekTo(pos)
            })
            isActive = true
        }

        createNotificationChannel()
        startForeground(notificationId, buildPlaceholderNotification())
        pushNotificationDebounced()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        MediaButtonReceiver.handleIntent(compatSession, intent)
        return START_STICKY
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession = mediaSession

    override fun onEvents(player: Player, events: Player.Events) {
        val isTrackChange = events.contains(Player.EVENT_MEDIA_ITEM_TRANSITION) ||
                events.contains(Player.EVENT_MEDIA_METADATA_CHANGED)
        val isStateChange = events.contains(Player.EVENT_IS_PLAYING_CHANGED)

        if (isTrackChange) pushNotificationDebounced(forceArtworkReset = true)
        else if (isStateChange) pushNotificationDebounced()
    }

    private fun pushNotificationDebounced(forceArtworkReset: Boolean = false) {
        val md = player.mediaMetadata
        val title = md.title?.toString() ?: "LifonMUSIC"
        val artist = md.artist?.toString() ?: "CUPSIZE"
        val isPlaying = player.isPlaying

        Log.d("ArtworkDebug", "pushNotification: title=$title forceReset=$forceArtworkReset cachedIcon=${cachedLargeIcon != null}")

        if (forceArtworkReset) {
            Log.d("ArtworkDebug", "Сбрасываем кэш обложки (forceReset)")
            cachedLargeIcon = null
            lastArtworkHash = 0
            isDecoding = false
            decodeJob?.cancel()
        }

        if (title == lastTitle && artist == lastArtist && isPlaying == lastIsPlaying && cachedLargeIcon != null) {
            Log.d("ArtworkDebug", "Пропускаем — ничего не изменилось")
            return
        }

        lastTitle = title
        lastArtist = artist
        lastIsPlaying = isPlaying

        notifyJob?.cancel()
        notifyJob = scope.launch {
            delay(150)
            val n = buildMediaNotification()
            nm.notify(notificationId, n)
        }
    }

    private fun buildPlaceholderNotification(): Notification {
        val pi = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("LifonMUSIC")
            .setContentText("Готов к воспроизведению")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pi)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setStyle(MediaStyle().setMediaSession(compatSession.sessionToken))
            .build()
    }

    private fun requestArtworkDecodeIfNeeded(artworkData: ByteArray?) {
        Log.d("ArtworkDebug", "requestArtworkDecode: dataSize=${artworkData?.size ?: "null"} hash=$lastArtworkHash isDecoding=$isDecoding cached=${cachedLargeIcon != null}")

        if (artworkData == null) {
            Log.w("ArtworkDebug", "artworkData == null — обложка не передана в MediaMetadata!")
            return
        }

        val hash = artworkData.contentHashCode()

        if (hash == lastArtworkHash && cachedLargeIcon != null) {
            Log.d("ArtworkDebug", "Обложка уже в кэше, hash совпадает")
            return
        }

        if (isDecoding) {
            Log.d("ArtworkDebug", "Уже декодируем, пропускаем")
            return
        }

        Log.d("ArtworkDebug", "Начинаем декод обложки, размер=${artworkData.size} байт")
        lastArtworkHash = hash
        isDecoding = true
        decodeJob?.cancel()
        decodeJob = scope.launch(Dispatchers.Default) {
            val bmp = runCatching {
                val opts = BitmapFactory.Options().apply { inSampleSize = 2 }
                BitmapFactory.decodeByteArray(artworkData, 0, artworkData.size, opts)
            }.getOrNull()

            withContext(Dispatchers.Main) {
                if (bmp != null) {
                    Log.d("ArtworkDebug", "Декод успешен: ${bmp.width}x${bmp.height}")
                    cachedLargeIcon = bmp
                } else {
                    Log.e("ArtworkDebug", "Декод вернул null!")
                }
                isDecoding = false
                notifyJob?.cancel()
                notifyJob = scope.launch {
                    val n = buildMediaNotification()
                    nm.notify(notificationId, n)
                }
            }
        }
    }

    private suspend fun buildMediaNotification(): Notification {
        val md = player.mediaMetadata
        val title = md.title?.toString() ?: "LifonMUSIC"
        val artist = md.artist?.toString() ?: "CUPSIZE"
        val isPlaying = player.isPlaying
        val artworkData = md.artworkData

        Log.d("ArtworkDebug", "buildMediaNotification: title=$title artworkData=${artworkData?.size ?: "null"}")

        requestArtworkDecodeIfNeeded(artworkData)

        val fallback = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)
        val iconToUse = cachedLargeIcon ?: fallback

        Log.d("ArtworkDebug", "iconToUse: ${if (cachedLargeIcon != null) "обложка альбома" else "иконка приложения (fallback)"}")

        compatSession.setMetadata(
            MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
                .putLong(
                    MediaMetadataCompat.METADATA_KEY_DURATION,
                    player.duration.takeIf { it > 0 } ?: -1L
                )
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, iconToUse)
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ART, iconToUse)
                .putBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, iconToUse)
                .build()
        )

        compatSession.setPlaybackState(
            PlaybackStateCompat.Builder()
                .setActions(
                    PlaybackStateCompat.ACTION_PLAY_PAUSE or
                            PlaybackStateCompat.ACTION_PLAY or
                            PlaybackStateCompat.ACTION_PAUSE or
                            PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                            PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                            PlaybackStateCompat.ACTION_SEEK_TO
                )
                .setState(
                    if (isPlaying) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED,
                    player.currentPosition.coerceAtLeast(0L),
                    if (isPlaying) 1f else 0f
                )
                .build()
        )

        val contentPi = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        val playPauseIcon =
            if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(artist)
            .setLargeIcon(iconToUse)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(contentPi)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .addAction(
                android.R.drawable.ic_media_previous, "Previous",
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                    this, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                )
            )
            .addAction(
                playPauseIcon, "Play/Pause",
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                    this,
                    if (isPlaying) PlaybackStateCompat.ACTION_PAUSE else PlaybackStateCompat.ACTION_PLAY
                )
            )
            .addAction(
                android.R.drawable.ic_media_next, "Next",
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                    this, PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                )
            )
            .setStyle(
                MediaStyle()
                    .setMediaSession(compatSession.sessionToken)
                    .setShowActionsInCompactView(0, 1, 2)
            )
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(
                channelId, "Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Media playback"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            nm.createNotificationChannel(ch)
        }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        player.stop()
        player.clearMediaItems()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        player.removeListener(this)
        notifyJob?.cancel()
        decodeJob?.cancel()
        compatSession.release()
        mediaSession.release()
        stopForeground(STOP_FOREGROUND_REMOVE)
        scope.cancel()
        PlayerHolder.release()
        super.onDestroy()
    }
}