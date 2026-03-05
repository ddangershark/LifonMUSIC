package com.lifon.music

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView

@SuppressLint("CustomSplashScreen")
class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )

        setContent {
            val prefs = getSharedPreferences("lifon_prefs", MODE_PRIVATE)

            var splashDone by remember { mutableStateOf(false) }
            var welcomeDone by remember { mutableStateOf(prefs.getBoolean("welcome_shown", false)) }


            var authToken by remember {
                mutableStateOf(prefs.getString("auth_token", null)?.takeIf { it.isNotBlank() })
            }

            when {
                !splashDone -> {
                    SplashVideo(onFinished = { splashDone = true })
                }

                !welcomeDone -> {
                    WelcomeScreen(onDismiss = {
                        prefs.edit().putBoolean("welcome_shown", true).apply()
                        welcomeDone = true
                    })
                }

                authToken.isNullOrBlank() -> {
                    AuthScreen(
                        onAuthSuccess = { newToken ->
                            prefs.edit().putString("auth_token", newToken).apply()
                            authToken = newToken
                        }
                    )
                }

                else -> {
                    LaunchedEffect(Unit) {
                        startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                        finish()
                    }
                }
            }
        }
    }
}

@Composable
private fun SplashVideo(onFinished: () -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        val context = LocalContext.current
        var finishedOnce by remember { mutableStateOf(false) }

        val player = remember(context) {
            ExoPlayer.Builder(context).build().apply {
                setMediaItem(
                    MediaItem.fromUri(
                        Uri.parse("android.resource://${context.packageName}/${R.raw.splash}")
                    )
                )
                repeatMode = Player.REPEAT_MODE_OFF
                volume = 0f
                playWhenReady = true
                prepare()
            }
        }

        DisposableEffect(player) {
            val listener = object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    if (!finishedOnce && state == Player.STATE_ENDED) {
                        finishedOnce = true
                        onFinished()
                    }
                }
            }
            player.addListener(listener)
            onDispose {
                player.removeListener(listener)
                player.release()
            }
        }

        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    this.player = player
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    setBackgroundColor(android.graphics.Color.BLACK)
                }
            },
            update = { view ->
                if (view.player !== player) view.player = player
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}