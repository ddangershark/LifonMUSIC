package com.lifon.music

import android.net.Uri
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay

@Composable
fun OfflineScreen(
    player: androidx.media3.exoplayer.ExoPlayer,
    onRetry: () -> Unit
) {
    val context = LocalContext.current
    var downloadedTracks by remember { mutableStateOf(listOf<DownloadedTrack>()) }
    var currentPlayingId by remember { mutableStateOf<Int?>(null) }
    var isPlaying by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            downloadedTracks = TrackDownloadManager.getDownloadedTracks(context)
            delay(2000)
        }
    }

    LaunchedEffect(player) {
        val listener = object : androidx.media3.common.Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }
            override fun onPlaybackStateChanged(state: Int) {
                if (state == androidx.media3.common.Player.STATE_ENDED) {
                    currentPlayingId = null
                }
            }
        }
        player.addListener(listener)
        awaitCancellation()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.0f to Color(0xFF0D0D10),
                        0.5f to Color(0xFF080809),
                        1.0f to Color(0xFF050506)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 28.dp)
        ) {
            Spacer(Modifier.height(40.dp))

            Box(
                modifier = Modifier
                    .size(72.dp)
                    .align(Alignment.CenterHorizontally)
                    .shadow(20.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color(0xFFFF6B8A).copy(alpha = 0.12f))
                    .border(1.5.dp, Color(0xFFFF6B8A).copy(alpha = 0.25f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.WifiOff,
                    contentDescription = null,
                    tint = Color(0xFFFF6B8A).copy(alpha = 0.8f),
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Нет подключения к интернету",
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 24.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = "Проверьте подключение к сети и попробуйте снова",
                    color = Color.White.copy(alpha = 0.55f),
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFE8D5FF))
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { onRetry() }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Попробовать снова",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(Modifier.height(28.dp))

            if (downloadedTracks.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "СКАЧАННЫЕ ТРЕКИ",
                        color = Color.White.copy(alpha = 0.35f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "${downloadedTracks.size} тр.",
                        color = Color.White.copy(alpha = 0.25f),
                        fontSize = 11.sp
                    )
                }

                Spacer(Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(downloadedTracks, key = { it.id }) { track ->
                        OfflineTrackRow(
                            track = track,
                            isActive = currentPlayingId == track.id,
                            isPlaying = isPlaying && currentPlayingId == track.id,
                            onClick = {
                                if (currentPlayingId == track.id) {
                                    if (player.isPlaying) player.pause() else player.play()
                                } else {
                                    currentPlayingId = track.id
                                    val localFile = TrackDownloadManager.getLocalFile(context, track.id)
                                    if (localFile != null) {
                                        val localCover = TrackDownloadManager.getLocalCover(context, track.id)
                                        val mediaItem = androidx.media3.common.MediaItem.Builder()
                                            .setUri(Uri.fromFile(localFile))
                                            .setMediaMetadata(
                                                androidx.media3.common.MediaMetadata.Builder()
                                                    .setTitle(track.title)
                                                    .setArtist(track.artist)
                                                    .setArtworkUri(
                                                        if (localCover != null) Uri.fromFile(localCover)
                                                        else track.coverUrl?.let { Uri.parse(it) }
                                                    )
                                                    .build()
                                            )
                                            .build()
                                        player.setMediaItem(mediaItem)
                                        player.prepare()
                                        player.play()
                                    }
                                }
                            }
                        )
                    }
                }
            } else {
                Spacer(Modifier.weight(1f))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.06f))
                            .border(1.dp, Color.White.copy(alpha = 0.10f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.MusicNote,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.25f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                    Text(
                        "Нет скачанных треков",
                        color = Color.White.copy(alpha = 0.45f),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Подключитесь к интернету чтобы\nзагрузить музыку для оффлайн-прослушивания",
                        color = Color.White.copy(alpha = 0.25f),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }

                Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun OfflineTrackRow(
    track: DownloadedTrack,
    isActive: Boolean,
    isPlaying: Boolean,
    onClick: () -> Unit
) {
    val bg by animateFloatAsState(
        if (isActive) 0.12f else 0f,
        tween(200),
        label = "obg"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = bg))
            .border(
                1.dp,
                Color.White.copy(alpha = if (isActive) 0.18f else 0.06f),
                RoundedCornerShape(14.dp)
            )
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            if (track.coverUrl != null) {
                AsyncImage(
                    model = track.coverUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    Icons.Filled.MusicNote,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.30f),
                    modifier = Modifier.size(18.dp)
                )
            }
            if (isActive) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.50f)),
                    contentAlignment = Alignment.Center
                ) {
                    PlayingIndicatorDots(isPlaying = isPlaying)
                }
            }
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                color = if (isActive) Color.White else Color.White.copy(alpha = 0.90f),
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = track.artist,
                color = Color.White.copy(alpha = 0.40f),
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(Modifier.width(8.dp))

        Text(
            text = track.duration,
            color = Color.White.copy(alpha = 0.35f),
            fontSize = 12.sp,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
        )

        Spacer(Modifier.width(4.dp))

        IconButton(
            onClick = onClick,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.70f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
