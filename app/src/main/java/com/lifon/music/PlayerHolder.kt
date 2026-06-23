package com.lifon.music

import android.content.Context
import android.util.Log
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer

object PlayerHolder {
    @Volatile private var _player: ExoPlayer? = null

    fun get(context: Context): ExoPlayer {
        return _player ?: synchronized(this) {
            _player ?: ExoPlayer.Builder(context.applicationContext)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(C.USAGE_MEDIA)
                        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                        .build(),
                    true
                )
                .setHandleAudioBecomingNoisy(true)
                .build().also {
                    _player = it
                    Log.d("PH", "create player hash=${it.hashCode()}")
                }
        }
    }

    fun peek(): ExoPlayer? = _player

    fun release() {
        synchronized(this) {
            _player?.release()
            _player = null
        }
    }
}