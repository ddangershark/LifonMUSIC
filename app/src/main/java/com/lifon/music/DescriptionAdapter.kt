package com.lifon.music

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.media3.common.Player
import androidx.media3.ui.PlayerNotificationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DescriptionAdapter(
    private val context: Context
) : PlayerNotificationManager.MediaDescriptionAdapter {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private var lastArtworkHash: Int = 0
    private var cachedBitmap: Bitmap? = null
    private var decoding: Boolean = false

    override fun getCurrentContentTitle(player: Player): CharSequence =
        player.mediaMetadata.title ?: "LifonMUSIC"

    override fun getCurrentContentText(player: Player): CharSequence? =
        player.mediaMetadata.artist

    override fun getCurrentLargeIcon(
        player: Player,
        callback: PlayerNotificationManager.BitmapCallback
    ): Bitmap? {
        val data = player.mediaMetadata.artworkData

        if (data == null) {
            return cachedBitmap ?: BitmapFactory.decodeResource(
                context.resources,
                R.mipmap.ic_launcher
            )
        }

        val hash = data.contentHashCode()

        if (hash == lastArtworkHash && cachedBitmap != null) {
            return cachedBitmap
        }

        if (hash != lastArtworkHash) {
            cachedBitmap = null
            lastArtworkHash = hash
        }

        if (decoding) return null

        decoding = true
        scope.launch {
            val bmp = withContext(Dispatchers.Default) {
                runCatching {
                    BitmapFactory.decodeByteArray(data, 0, data.size)
                }.getOrNull()
            }
            cachedBitmap = bmp
            decoding = false
            if (bmp != null) {
                callback.onBitmap(bmp)
            }
        }

        return BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher)
    }

    override fun createCurrentContentIntent(player: Player): PendingIntent? {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}