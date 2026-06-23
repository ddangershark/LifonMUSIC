package com.lifon.music

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_PREV -> PlayerCallbacks.onPrev?.invoke()
            ACTION_NEXT -> PlayerCallbacks.onNext?.invoke()
            ACTION_PLAY_PAUSE -> PlayerCallbacks.onPlayPause?.invoke()
            ACTION_LIKE -> PlayerCallbacks.onLike?.invoke()
        }
    }

    companion object {
        const val ACTION_PREV = "com.lifon.music.PREV"
        const val ACTION_NEXT = "com.lifon.music.NEXT"
        const val ACTION_PLAY_PAUSE = "com.lifon.music.PLAY_PAUSE"
        const val ACTION_LIKE = "com.lifon.music.LIKE"
    }
}