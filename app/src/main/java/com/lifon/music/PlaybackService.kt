package com.lifon.music

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
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
    private var lastIsLiked = false

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
                // CustomAction — срабатывает на MIUI и других системах
                override fun onCustomAction(action: String, extras: android.os.Bundle?) {
                    if (action == ACTION_LIKE_CUSTOM) {
                        PlayerCallbacks.onLike?.invoke()
                    }
                }
            })
            isActive = true
        }

        PlayerCallbacks.onLikeStateChanged = { trackId, isLiked ->
            val currentTrackId = PlayerCallbacks.currentTrackId
            if (trackId == currentTrackId) {
                lastIsLiked = isLiked
                pushNotificationDebounced(forceLikeUpdate = true)
            }
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

        if (isTrackChange) {
            // Сбрасываем лайк — MainActivity пришлёт актуальное состояние через onLikeStateChanged
            pushNotificationDebounced(forceArtworkReset = true, forceLikeUpdate = true)
        } else if (isStateChange) {
            pushNotificationDebounced()
        }
    }

    private fun pushNotificationDebounced(
        forceArtworkReset: Boolean = false,
        forceLikeUpdate: Boolean = false
    ) {
        val md = player.mediaMetadata
        val title = md.title?.toString() ?: "LifonMUSIC"
        val artist = md.artist?.toString() ?: "CUPSIZE"
        val isPlaying = player.isPlaying

        if (forceArtworkReset) {
            cachedLargeIcon = null
            lastArtworkHash = 0
            isDecoding = false
            decodeJob?.cancel()
        }

        if (!forceLikeUpdate
            && title == lastTitle && artist == lastArtist
            && isPlaying == lastIsPlaying && cachedLargeIcon != null) {
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
        if (artworkData == null) return

        val hash = artworkData.contentHashCode()
        if (hash == lastArtworkHash && cachedLargeIcon != null) return
        if (isDecoding) return

        lastArtworkHash = hash
        isDecoding = true
        decodeJob?.cancel()
        decodeJob = scope.launch(Dispatchers.Default) {
            val bmp = runCatching {
                val opts = BitmapFactory.Options().apply { inSampleSize = 2 }
                BitmapFactory.decodeByteArray(artworkData, 0, artworkData.size, opts)
            }.getOrNull()

            withContext(Dispatchers.Main) {
                if (bmp != null) cachedLargeIcon = bmp
                isDecoding = false
                notifyJob?.cancel()
                notifyJob = scope.launch {
                    val n = buildMediaNotification()
                    nm.notify(notificationId, n)
                }
            }
        }
    }

    private fun makeActionIntent(action: String, requestCode: Int): PendingIntent {
        val intent = Intent(action).apply { setPackage(packageName) }
        return PendingIntent.getBroadcast(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private suspend fun buildMediaNotification(): Notification {
        val md = player.mediaMetadata
        val title = md.title?.toString() ?: "LifonMUSIC"
        val artist = md.artist?.toString() ?: "CUPSIZE"
        val isPlaying = player.isPlaying
        val artworkData = md.artworkData

        requestArtworkDecodeIfNeeded(artworkData)

        val fallback = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)
        val iconToUse = cachedLargeIcon ?: fallback

        val likeIcon = if (lastIsLiked) R.drawable.ic_favorite else R.drawable.ic_favorite_border

        // CustomAction — показывается на MIUI как отдельная кнопка рядом с треком
        val likeCustomAction = PlaybackStateCompat.CustomAction.Builder(
            ACTION_LIKE_CUSTOM,
            if (lastIsLiked) "Убрать лайк" else "Лайк",
            likeIcon
        ).build()

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
                .addCustomAction(likeCustomAction)
                .build()
        )

        val contentPi = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        val playPauseIcon =
            if (isPlaying) android.R.drawable.ic_media_pause
            else android.R.drawable.ic_media_play

        val prevPi      = makeActionIntent(NotificationReceiver.ACTION_PREV,       10)
        val playPausePi = makeActionIntent(NotificationReceiver.ACTION_PLAY_PAUSE, 11)
        val nextPi      = makeActionIntent(NotificationReceiver.ACTION_NEXT,       12)
        val likePi      = makeActionIntent(NotificationReceiver.ACTION_LIKE,       13)

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(artist)
            .setLargeIcon(iconToUse)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(contentPi)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .addAction(android.R.drawable.ic_media_previous, "Previous", prevPi)
            .addAction(playPauseIcon, "Play/Pause", playPausePi)
            .addAction(android.R.drawable.ic_media_next, "Next", nextPi)
            .addAction(likeIcon, "Like", likePi)
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
        PlayerCallbacks.onLikeStateChanged = null
        super.onDestroy()
    }

    companion object {
        const val ACTION_LIKE_CUSTOM = "ACTION_LIKE"
    }
}