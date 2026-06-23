package com.lifon.music

import android.content.Context
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.media.audiofx.Equalizer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FormatColorText
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.palette.graphics.Palette
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.lifon.music.lyrics.LyricsView
import com.lifon.music.lyrics.LyricsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.max
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    private var player: ExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        TrackDownloadManager.init(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(
                arrayOf(
                    android.Manifest.permission.POST_NOTIFICATIONS,
                    android.Manifest.permission.READ_MEDIA_AUDIO
                ),
                1001
            )
        } else {
            requestPermissions(
                arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                1001
            )
        }

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )

        val serviceIntent = Intent(this, PlaybackService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        player = PlayerHolder.get(this)
        android.util.Log.e("PH", "Activity player hash=${player!!.hashCode()}")

        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Color(0xFFE8D5FF),
                    secondary = Color(0xFF80CBC4),
                    surface = Color(0xFF0D0D10),
                    background = Color(0xFF080809)
                ),
                typography = androidx.compose.material3.Typography(
                    displayLarge = TextStyle(fontFamily = GoogleSans),
                    displayMedium = TextStyle(fontFamily = GoogleSans),
                    displaySmall = TextStyle(fontFamily = GoogleSans),
                    headlineLarge = TextStyle(fontFamily = GoogleSans),
                    headlineMedium = TextStyle(fontFamily = GoogleSans),
                    headlineSmall = TextStyle(fontFamily = GoogleSans),
                    titleLarge = TextStyle(fontFamily = GoogleSans),
                    titleMedium = TextStyle(fontFamily = GoogleSans),
                    titleSmall = TextStyle(fontFamily = GoogleSans),
                    bodyLarge = TextStyle(fontFamily = GoogleSans),
                    bodyMedium = TextStyle(fontFamily = GoogleSans),
                    bodySmall = TextStyle(fontFamily = GoogleSans),
                    labelLarge = TextStyle(fontFamily = GoogleSans),
                    labelMedium = TextStyle(fontFamily = GoogleSans),
                    labelSmall = TextStyle(fontFamily = GoogleSans),
                )
            ) {
                LifonApp(player = player!!)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

    }
}

data class Album(
    val id: Int, val title: String, val year: String,
    val coverUrl: String?, val tracks: List<Track>,
    val glowColor: String? = null
)
data class Track(
    val id: Int, val title: String, val duration: String, val audioUrl: String,
    val albumId: Int, val artist: String = "CUPSIZE", val featArtist: String? = null,
    val coverUrl: String? = null
) {
    val displayArtist: String get() = if (featArtist.isNullOrBlank()) artist else "$artist ft. $featArtist"
}

enum class PlayContext { ALBUM, FAVORITES, ALL_TRACKS }
enum class AppScreen { LIBRARY, FAVORITES, PROFILE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LifonApp(player: ExoPlayer) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val playerEqualizer = remember { PlayerEqualizer(context) }
    var showEqualizer by remember { mutableStateOf(false) }
    val prefs = context.getSharedPreferences("lifon_prefs", Context.MODE_PRIVATE)
    var showDisclaimer by remember { mutableStateOf(!prefs.getBoolean("disclaimer_shown", false)) }
    var showDiscographyRoadmap by remember { mutableStateOf(false) }

    var albums by remember { mutableStateOf<List<Album>>(emptyList()) }
    var maintenanceMessage by remember { mutableStateOf<String?>(null) }
    var isOffline by remember { mutableStateOf(false) }
    var retryTrigger by remember { mutableStateOf(0) }
    LaunchedEffect(retryTrigger) {
        isOffline = false
        maintenanceMessage = null
        when (val result = CatalogRepository.fetchAlbums()) {
            is FetchResult.Success -> {
                albums = result.albums
                isOffline = false
            }
            is FetchResult.Maintenance -> maintenanceMessage = result.message
            is FetchResult.Offline -> isOffline = true
            is FetchResult.Error -> { }
        }
    }

    if (maintenanceMessage != null) {
        MaintenanceScreen(
            message = maintenanceMessage!!,
            player = player,
            onPlayPause = {
                if (player.isPlaying) player.pause() else player.play()
            }
        )
        return@LifonApp
    }

    if (isOffline) {
        OfflineScreen(
            player = player,
            onRetry = { retryTrigger++ }
        )
        return@LifonApp
    }

    // Active broadcast banner from admin panel
    var broadcastTitle by remember { mutableStateOf<String?>(null) }
    var broadcastBody by remember { mutableStateOf<String?>(null) }
    var broadcastSeenId by remember { mutableStateOf(-1) }
    var showBroadcast by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        try {
            val conn = withContext(Dispatchers.IO) {
                (URL("${ApiConfig.BASE}/notification").openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"; connectTimeout = 5000; readTimeout = 5000
                }
            }
            val text = withContext(Dispatchers.IO) { conn.inputStream.bufferedReader().readText() }
            val json = JSONObject(text)
            val items = json.optJSONArray("items")
            if (items != null && items.length() > 0) {
                val item = items.getJSONObject(0)
                val title = item.optString("title").takeIf { it.isNotBlank() }
                val body = item.optString("body").takeIf { it.isNotBlank() }
                val broadcastId = item.optInt("id", -1)
                val lastSeen = prefs.getInt("last_broadcast_id", -1)
                if (title != null && broadcastId != lastSeen) {
                    broadcastTitle = title
                    broadcastBody = body
                    broadcastSeenId = broadcastId
                    showBroadcast = true
                }
            }
        } catch (_: Exception) { }
    }

    val artworkCache = remember { mutableMapOf<String, ByteArray>() }
    var selectedAlbum by remember { mutableStateOf<Album?>(null) }
    var search by remember { mutableStateOf("") }
    var currentScreen by remember { mutableStateOf(AppScreen.LIBRARY) }
    var currentTrack by remember { mutableStateOf<Track?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var positionMs by remember { mutableStateOf(0L) }
    var durationMs by remember { mutableStateOf(0L) }
    var isShuffled by remember { mutableStateOf(false) }
    var isRepeating by remember { mutableStateOf(false) }
    var playContext by remember { mutableStateOf(PlayContext.ALBUM) }
    var shuffleQueue by remember { mutableStateOf<List<Track>>(emptyList()) }
    var shuffleIndex by remember { mutableStateOf(0) }

    val likedTracks = remember { mutableStateSetOf<Int>() }


    LaunchedEffect(Unit) {
        if (player.mediaItemCount > 0 && player.currentMediaItem != null) {
            val meta = player.mediaMetadata
            val title = meta.title?.toString() ?: return@LaunchedEffect
            val allTracks = albums.flatMap { it.tracks }
            val found = allTracks.firstOrNull {
                it.title.equals(title, ignoreCase = true)
            }
            if (found != null) {
                currentTrack = found
                isPlaying = player.isPlaying
                durationMs = max(0L, player.duration)
                positionMs = max(0L, player.currentPosition)
            }
        }
    }

    LaunchedEffect(Unit) {
        val token = prefs.getString("auth_token", null)
        if (token.isNullOrBlank() || token == "guest") {
            val saved = prefs.getStringSet("liked_tracks", emptySet()) ?: emptySet()
            saved.forEach { s -> s.toIntOrNull()?.let { likedTracks.add(it) } }
            return@LaunchedEffect
        }
        val serverLikes = LikesRepository.fetchLikes(token)
        if (serverLikes != null) {
            likedTracks.clear()
            likedTracks.addAll(serverLikes)
            prefs.edit().putStringSet("liked_tracks", serverLikes.map { it.toString() }.toSet())
                .apply()
        } else {
            val saved = prefs.getStringSet("liked_tracks", emptySet()) ?: emptySet()
            saved.forEach { s -> s.toIntOrNull()?.let { likedTracks.add(it) } }
        }
    }

    // Онлайн-присутствие: пинг при запуске и каждые 5 минут (только для авторизованных пользователей)
    LaunchedEffect(Unit) {
        val token = prefs.getString("auth_token", null)
        if (token.isNullOrBlank() || token == "guest") return@LaunchedEffect
        while (true) {
            try {
                withContext(Dispatchers.IO) {
                    val conn = (URL("${ApiConfig.BASE}/profile/ping").openConnection() as HttpURLConnection).apply {
                        requestMethod = "POST"
                        setRequestProperty("Authorization", "Bearer $token")
                        connectTimeout = 6000
                        readTimeout = 6000
                    }
                    conn.responseCode // execute request
                    conn.disconnect()
                }
            } catch (_: Exception) { }
            delay(5 * 60 * 1000L) // каждые 5 минут
        }
    }

    // Refresh likes from server whenever user opens the Favorites tab
    LaunchedEffect(currentScreen) {
        if (currentScreen == AppScreen.FAVORITES) {
            val token = prefs.getString("auth_token", null)
            if (!token.isNullOrBlank() && token != "guest") {
                val serverLikes = LikesRepository.fetchLikes(token)
                if (serverLikes != null) {
                    likedTracks.clear()
                    likedTracks.addAll(serverLikes)
                    prefs.edit().putStringSet("liked_tracks", serverLikes.map { it.toString() }.toSet()).apply()
                }
            }
        }
    }

    fun toggleLike(id: Int) {
        val token = prefs.getString("auth_token", null)
        if (id in likedTracks) {
            likedTracks.remove(id)
            prefs.edit().putStringSet("liked_tracks", likedTracks.map { it.toString() }.toSet())
                .apply()
            if (!token.isNullOrBlank() && token != "guest")
                scope.launch { LikesRepository.removeLike(token, id) }
            PlayerCallbacks.onLikeStateChanged?.invoke(id, id in likedTracks)
        } else {
            likedTracks.add(id)
            prefs.edit().putStringSet("liked_tracks", likedTracks.map { it.toString() }.toSet())
                .apply()
            if (!token.isNullOrBlank() && token != "guest")
                scope.launch { LikesRepository.addLike(token, id) }
            PlayerCallbacks.onLikeStateChanged?.invoke(id, id in likedTracks)
        }
    }

    fun downloadTrack(track: Track) {
        if (track.audioUrl.isBlank()) return
        val coverUrl = track.coverUrl ?: albums.firstOrNull { it.id == track.albumId }?.coverUrl
        TrackDownloadManager.download(context, track, coverUrl)
    }

    fun getPlaylist(ctx: PlayContext, fav: List<Track>): List<Track> = when (ctx) {
        PlayContext.FAVORITES -> fav
        PlayContext.ALL_TRACKS -> albums.flatMap { it.tracks }
        PlayContext.ALBUM -> currentTrack?.let { t -> albums.firstOrNull { it.id == t.albumId }?.tracks }
            ?: emptyList()
    }

    fun albumForTrack(t: Track?): Album? = t?.let { albums.firstOrNull { a -> a.id == it.albumId } }
    fun prevNext(track: Track?, ctx: PlayContext, fav: List<Track>): Pair<Track?, Track?> {
        val t = track ?: return null to null
        val pl = getPlaylist(ctx, fav); if (pl.isEmpty()) return null to null
        val idx = pl.indexOfFirst { it.id == t.id }.takeIf { it >= 0 } ?: return null to null
        return (if (pl.size > 1) pl[(idx - 1 + pl.size) % pl.size] else null) to (if (pl.size > 1) pl[(idx + 1) % pl.size] else null)
    }


    fun playTrack(track: Track, ctx: PlayContext = playContext) {
        playContext = ctx
        currentTrack = track
        isPlaying = true
        PlayerCallbacks.currentTrackId = track.id

        PlayerCallbacks.onLikeStateChanged?.invoke(track.id, track.id in likedTracks)

        if (isShuffled && shuffleQueue.isNotEmpty()) {
            val idx = shuffleQueue.indexOfFirst { it.id == track.id }
            if (idx >= 0) shuffleIndex = idx
        }

        scope.launch {
            val album = albumForTrack(track)
            val coverUrl = track.coverUrl ?: album?.coverUrl
            val artworkBytes: ByteArray? = withContext(Dispatchers.IO) {
                if (coverUrl.isNullOrBlank()) return@withContext null
                artworkCache[coverUrl] ?: try {
                    val request = ImageRequest.Builder(context)
                        .data(coverUrl)
                        .allowHardware(false)
                        .build()
                    val result = ImageLoader(context).execute(request)
                    val bmp = (result.drawable as? BitmapDrawable)?.bitmap
                        ?: return@withContext null
                    val stream = java.io.ByteArrayOutputStream()
                    val scaled = android.graphics.Bitmap.createScaledBitmap(bmp, 256, 256, true)
                    scaled.compress(android.graphics.Bitmap.CompressFormat.JPEG, 70, stream)
                    scaled.recycle()
                    stream.toByteArray().also { artworkCache[coverUrl] = it }
                } catch (_: Exception) {
                    null
                }
            }

            val metadata = MediaMetadata.Builder()
                .setTitle(track.title)
                .setArtist(track.displayArtist)
                .setAlbumTitle(album?.title)
                .apply {
                    if (artworkBytes != null) {
                        setArtworkData(artworkBytes, MediaMetadata.PICTURE_TYPE_FRONT_COVER)
                    }
                }
                .build()

            val mediaItem = MediaItem.Builder()
                .setUri(track.audioUrl)
                .setMediaMetadata(metadata)
                .build()

            player.setMediaItem(mediaItem)
            player.prepare()
            player.play()
        }
    }

    fun getFav(): List<Track> =
        albums.flatMap { it.tracks }.filter { it.id in likedTracks }

    fun playPrev() {
        if (isShuffled && shuffleQueue.isNotEmpty()) {
            val prevIdx = shuffleIndex - 1
            if (prevIdx >= 0) {
                shuffleIndex = prevIdx
                playTrack(shuffleQueue[prevIdx], playContext)
            }
        } else {
            prevNext(currentTrack, playContext, getFav()).first?.let {
                playTrack(it, playContext)
            }
        }
    }

    fun playNext() {
        if (isShuffled && shuffleQueue.isNotEmpty()) {
            val nextIdx = shuffleIndex + 1
            if (nextIdx < shuffleQueue.size) {
                shuffleIndex = nextIdx
                playTrack(shuffleQueue[nextIdx], playContext)
            }
        } else {
            val fav = getFav()
            prevNext(currentTrack, playContext, fav).second?.let {
                playTrack(it, playContext)
            }
        }
    }

    val currentPlayNext by rememberUpdatedState(::playNext)
    val currentPlayPrev by rememberUpdatedState(::playPrev)

    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onAudioSessionIdChanged(audioSessionId: Int) {
                playerEqualizer.attachToSession(audioSessionId)
            }
        }

        player.addListener(listener)

        val currentSession = player.audioSessionId
        if (currentSession > 0) {
            playerEqualizer.attachToSession(currentSession)
        }

        onDispose {
            player.removeListener(listener)
            playerEqualizer.release()
        }
    }
    DisposableEffect(Unit) {
        PlayerCallbacks.onNext = { currentPlayNext() }
        PlayerCallbacks.onPrev = { currentPlayPrev() }
        PlayerCallbacks.onPlayPause = {
            if (player.isPlaying) {
                player.pause()
                isPlaying = false
            } else {
                if (player.mediaItemCount == 0 && currentTrack != null) {
                    // playTrack не доступна здесь напрямую — используем player
                    player.play()
                    isPlaying = true
                } else {
                    player.play()
                    isPlaying = true
                }
            }
        }
        PlayerCallbacks.onLike = {
            currentTrack?.let { toggleLike(it.id) }
        }
        onDispose {
            PlayerCallbacks.onNext = null
            PlayerCallbacks.onPrev = null
            PlayerCallbacks.onPlayPause = null
            PlayerCallbacks.onLike = null
        }
    }

    fun togglePP() {
        if (player.isPlaying) {
            player.pause(); isPlaying = false
        } else {
            if (player.mediaItemCount == 0 && currentTrack != null) playTrack(currentTrack!!) else {
                player.play(); isPlaying = true
            }
        }
    }

    fun seekTo(ms: Long) {
        val s = ms.coerceIn(0L, max(0L, player.duration)); player.seekTo(s); positionMs = s
    }

    DisposableEffect(player) {
        val l = object : Player.Listener {
            override fun onIsPlayingChanged(p: Boolean) {
                isPlaying = p
            }

            override fun onPlaybackStateChanged(s: Int) {
                durationMs = max(0L, player.duration)
                if (s == Player.STATE_ENDED) {
                    val token = prefs.getString("auth_token", null)
                    val trackId = currentTrack?.id
                    val dur = player.duration
                    if (token != null && token != "guest" && trackId != null && dur > 30_000L) {
                        scope.launch { ListenTracker.recordListen(token, trackId, dur) }
                    }

                    if (isRepeating) {
                        player.seekTo(0); player.play()
                    } else playNext()
                }
            }
        }
        player.addListener(l); onDispose { player.removeListener(l) }
    }
    LaunchedEffect(currentTrack, isPlaying) {
        while (currentTrack != null) {
            positionMs = max(0L, player.currentPosition); durationMs =
                max(0L, player.duration); delay(if (isPlaying) 250 else 500)
        }
    }

    val colorCache = remember { mutableStateMapOf<String?, Color>() }
    var dominantColor by remember { mutableStateOf(Color(0xFF0D0D10)) }
    val barTrack = currentTrack
    val barAlbum = albumForTrack(barTrack)
    val viewAlbum = selectedAlbum ?: barAlbum
    val favNav = remember(likedTracks.toSet(), albums) {
        albums.flatMap { it.tracks }.filter { it.id in likedTracks }
    }


    LaunchedEffect(isShuffled, playContext) {
        if (isShuffled) {
            val pl = getPlaylist(playContext, getFav())
            val current = currentTrack
            val rest = pl.filter { it.id != current?.id }.shuffled()
            shuffleQueue = if (current != null) listOf(current) + rest else rest
            shuffleIndex = 0
        } else {
            shuffleQueue = emptyList()
            shuffleIndex = 0
        }
    }

    val barPlaylist = remember(playContext, currentTrack?.id, favNav) {
        getPlaylist(playContext, favNav)
    }
    val (barPrevLinear, barNextLinear) = prevNext(barTrack, playContext, favNav)
    val barPrev =
        if (isShuffled && shuffleQueue.isNotEmpty()) shuffleQueue.getOrNull(shuffleIndex - 1) else barPrevLinear
    val barNext =
        if (isShuffled && shuffleQueue.isNotEmpty()) shuffleQueue.getOrNull(shuffleIndex + 1) else barNextLinear

    LaunchedEffect(viewAlbum?.coverUrl) {
        val url = viewAlbum?.coverUrl
        val glowHex = viewAlbum?.glowColor

        val manualColor = glowHex?.let {
            runCatching { Color(android.graphics.Color.parseColor(it)) }.getOrNull()
        }

        if (manualColor != null) {
            dominantColor = manualColor.darken(0.22f)
        } else if (url != null) {
            if (!colorCache.containsKey(url)) colorCache[url] =
                computeDominantColorFromUrl(context, url).ensureNotTooBright().darken(0.22f)
            dominantColor = colorCache[url] ?: Color(0xFF0D0D10)
        } else dominantColor = Color(0xFF0D0D10)
    }

    val animDom by animateColorAsState(
        dominantColor,
        tween(700, easing = FastOutSlowInEasing),
        label = "dom"
    )
    val bgBrush = remember(animDom) {
        Brush.verticalGradient(
            colorStops = arrayOf(
                0.0f to animDom.copy(alpha = 0.95f),
                0.45f to animDom.darken(0.30f).copy(alpha = 0.85f),
                1.0f to Color(0xFF080809)
            )
        )
    }
    val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val listPad = if (barTrack != null) 96.dp + 64.dp + navBottom else 64.dp + navBottom

    var showPlayer by remember { mutableStateOf(false) }
    var accentTarget by remember { mutableStateOf(Color(0xFF1A1A2E)) }
    val accent by animateColorAsState(
        accentTarget,
        tween(500, easing = FastOutSlowInEasing),
        label = "acc"
    )
    LaunchedEffect(barAlbum?.coverUrl) {
        val url = barAlbum?.coverUrl ?: return@LaunchedEffect
        val glowHex = barAlbum?.glowColor

        val manualColor = glowHex?.let {
            runCatching { Color(android.graphics.Color.parseColor(it)) }.getOrNull()
        }

        accentTarget = manualColor ?: (colorCache[url] ?: computeDominantColorFromUrl(context, url))
                .ensureNotTooBright()
                .darken(0.15f)
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgBrush)
    ) {


        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            containerColor = Color.Transparent,
            topBar = {
                AppTopBar(
                    title = when {
                        selectedAlbum != null -> selectedAlbum!!.title
                        currentScreen == AppScreen.FAVORITES -> "Избранное"
                        currentScreen == AppScreen.PROFILE -> "Профиль"
                        else -> "CUPSIZE"
                    },
                    subtitle = when {
                        selectedAlbum != null -> selectedAlbum!!.year
                        currentScreen == AppScreen.FAVORITES -> "${likedTracks.size} треков"
                        currentScreen == AppScreen.PROFILE -> "Аккаунт и настройки"
                        else -> "Дискография"
                    },
                    showBack = selectedAlbum != null,
                    search = search,
                    showSearch = selectedAlbum == null && currentScreen == AppScreen.LIBRARY,
                    onSearchChange = { search = it },
                    onBack = { selectedAlbum = null },
                    onAboutClick = { showDisclaimer = true },
                    onDiscographyClick = {
                        if (selectedAlbum == null && currentScreen == AppScreen.LIBRARY) {
                            showDiscographyRoadmap = true
                        }
                    }
                )
            },
            bottomBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Transparent)
                        .navigationBarsPadding()
                ) {
                    AnimatedVisibility(
                        visible = barTrack != null && barAlbum != null,
                        enter = slideInVertically { it } + fadeIn(),
                        exit = slideOutVertically { it } + fadeOut()
                    ) {
                        if (barTrack != null && barAlbum != null) {
                            MiniPlayerBar(
                                track = barTrack,
                                coverUrl = barTrack.coverUrl ?: barAlbum.coverUrl,
                                isPlaying = isPlaying,
                                positionMs = positionMs,
                                durationMs = durationMs,
                                accentColor = accent,
                                isLiked = barTrack.id in likedTracks,
                                onPlayPause = { togglePP() },
                                onExpand = { showPlayer = true },
                                prevTrack = barPrev,
                                nextTrack = barNext,
                                prevCoverUrl = barPrev?.coverUrl ?: albumForTrack(barPrev)?.coverUrl,
                                nextCoverUrl = barNext?.coverUrl ?: albumForTrack(barNext)?.coverUrl,
                                onPrevious = { playPrev() },
                                onNext = { playNext() },
                                onToggleLike = { toggleLike(barTrack.id) },
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }

                    BottomNavBar(
                        currentScreen = currentScreen,
                        modifier = Modifier,
                        onScreenChange = { currentScreen = it; selectedAlbum = null }
                    )
                }
            }
        ) { padding ->
            if (selectedAlbum != null) {
                BackHandler { selectedAlbum = null }
            }
            if (search.isNotBlank() && selectedAlbum == null && currentScreen == AppScreen.LIBRARY) {
                BackHandler {
                    search = ""
                }
            }

            if ((currentScreen == AppScreen.FAVORITES || currentScreen == AppScreen.PROFILE) && selectedAlbum == null) {
                BackHandler { currentScreen = AppScreen.LIBRARY }
            }

            when {
                selectedAlbum != null -> AlbumScreen(
                    album = selectedAlbum!!,
                    contentPadding = padding,
                    bottomPadding = listPad,
                    currentTrackId = currentTrack?.id,
                    isPlaying = isPlaying,
                    likedTracks = likedTracks,
                    onTrackClick = { playTrack(it, PlayContext.ALBUM) },
                    onToggleLike = { toggleLike(it) },
                    onDownloadTrack = { downloadTrack(it) },
                    onOpenEqualizer = { showEqualizer = true },
                )

                currentScreen == AppScreen.FAVORITES -> {
                    val fav = remember(likedTracks.toSet(), albums) {
                        albums.flatMap { it.tracks }.filter { it.id in likedTracks }
                    }
                    FavoritesScreen(
                        tracks = fav,
                        albums = albums,
                        currentTrack = currentTrack,
                        isPlaying = isPlaying,
                        contentPadding = padding,
                        bottomPadding = listPad,
                        likedTracks = likedTracks,
                        onTrackClick = { playTrack(it, PlayContext.FAVORITES) },
                        onToggleLike = { toggleLike(it) },
                        onDownloadTrack = { downloadTrack(it) }
                    )
                }

                currentScreen == AppScreen.PROFILE -> {
                    ProfileScreen(
                        albums = albums,
                        contentPadding = padding,
                        bottomPadding = listPad
                    )
                }

                else -> {
                    val q = search.trim()

                    val filteredAlbums = if (q.isBlank()) albums
                    else albums.filter {
                        it.title.contains(q, ignoreCase = true) || it.year.contains(q)
                    }

                    val filteredTracks = if (q.isBlank()) emptyList<Track>()
                    else albums
                        .flatMap { it.tracks }
                        .filter { t ->
                            t.title.contains(q, ignoreCase = true) ||
                                    t.displayArtist.contains(q, ignoreCase = true)
                        }

                    LibraryScreen(
                        albums = filteredAlbums,
                        currentTrack = currentTrack,
                        allAlbums = albums,
                        isPlaying = isPlaying,
                        contentPadding = padding,
                        bottomPadding = listPad,
                        onAlbumClick = { selectedAlbum = it },
                        onTrackClick = { playTrack(it, PlayContext.ALL_TRACKS) },
                        onPlayAll = {
                            val allTracks = albums.flatMap { it.tracks }
                            allTracks.firstOrNull()?.let { playTrack(it, PlayContext.ALL_TRACKS) }
                        },
                        onDownloadTrack = { downloadTrack(it) },
                        filteredTracks = filteredTracks,
                        searchQuery = q
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = showPlayer && barTrack != null && barAlbum != null,
            enter = slideInVertically { it } + fadeIn(tween(280)),
            exit = slideOutVertically { it } + fadeOut(tween(220))
        ) {
            if (barTrack != null && barAlbum != null) {
                BackHandler { showPlayer = false }
                FullPlayerScreen(
                    accent = accent,
                    track = barTrack,
                    album = barAlbum,
                    isPlaying = isPlaying,
                    isShuffled = isShuffled,
                    isRepeating = isRepeating,
                    isLiked = barTrack.id in likedTracks,
                    positionMs = positionMs,
                    durationMs = durationMs,
                    prev = barPrev,
                    next = barNext,
                    prevCoverUrl = barPrev?.coverUrl ?: albumForTrack(barPrev)?.coverUrl,
                    nextCoverUrl = barNext?.coverUrl ?: albumForTrack(barNext)?.coverUrl,
                    onClose = { showPlayer = false },
                    onPlayPause = { togglePP() },
                    onPrev = { playPrev() },
                    onNext = { playNext() },
                    onOpenAlbum = { alb ->
                        selectedAlbum = alb
                        showPlayer = false
                    },
                    onSeekTo = { seekTo(it) },
                    onToggleShuffle = { isShuffled = !isShuffled },
                    onToggleRepeat = { isRepeating = !isRepeating },
                    onToggleLike = { toggleLike(it) },
                    onOpenEqualizer = { showEqualizer = true },
                )
            }
        }


        // Broadcast banner from admin panel
        AnimatedVisibility(
            visible = showBroadcast && broadcastTitle != null,
            enter = slideInVertically { -it } + fadeIn(tween(300)),
            exit = slideOutVertically { -it } + fadeOut(tween(200))
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF1C1C2E))
                        .border(1.dp, Color(0xFFE8D5FF).copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("📣", fontSize = 18.sp, modifier = Modifier.padding(end = 10.dp))
                            Text(
                                broadcastTitle ?: "",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (!broadcastBody.isNullOrBlank()) {
                            Spacer(Modifier.height(6.dp))
                            Text(broadcastBody!!, color = Color.White.copy(alpha = 0.70f), fontSize = 13.sp, lineHeight = 18.sp)
                        }
                        Spacer(Modifier.height(12.dp))
                        Box(
                            Modifier
                                .align(Alignment.End)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFE8D5FF).copy(alpha = 0.15f))
                                .border(1.dp, Color(0xFFE8D5FF).copy(alpha = 0.30f), RoundedCornerShape(10.dp))
                                .clickable(remember { MutableInteractionSource() }, null) {
                                    if (broadcastSeenId != -1)
                                        prefs.edit().putInt("last_broadcast_id", broadcastSeenId).apply()
                                    showBroadcast = false
                                }
                                .padding(horizontal = 18.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Понятно", color = Color(0xFFE8D5FF), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        if (showDisclaimer) {
            BackHandler {
                prefs.edit().putBoolean("disclaimer_shown", true).apply()
                showDisclaimer = false
            }
            DisclaimerBanner(
                onDismiss = {
                    prefs.edit().putBoolean("disclaimer_shown", true).apply()
                    showDisclaimer = false
                }
            )
        }
        if (showEqualizer) {
            EqualizerSheet(
                equalizer = playerEqualizer,
                onDismiss = { showEqualizer = false }
            )
        }

        if (showDiscographyRoadmap) {
            DiscographyRoadmapSheet(
                albums = albums,
                onAlbumClick = { alb ->
                    selectedAlbum = alb
                    showDiscographyRoadmap = false
                },
                onDismiss = {
                    showDiscographyRoadmap = false
                }
            )
        }
    }
}

@Composable
private fun BottomNavBar(
    currentScreen: AppScreen,
    onScreenChange: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    val pill = RoundedCornerShape(999.dp)

    Box(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 56.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            Modifier
                .height(44.dp)
                .fillMaxWidth()
                .clip(pill)
                .background(Color.White.copy(alpha = 0.055f))
                .border(1.dp, Color.White.copy(alpha = 0.085f), pill)
                .padding(horizontal = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MiniTab(
                selected = currentScreen == AppScreen.LIBRARY,
                onClick = { onScreenChange(AppScreen.LIBRARY) },
                icon = Icons.Filled.Search
            )

            Box(Modifier.width(1.dp).height(16.dp).background(Color.White.copy(alpha = 0.08f)))

            MiniTab(
                selected = currentScreen == AppScreen.FAVORITES,
                onClick = { onScreenChange(AppScreen.FAVORITES) },
                icon = Icons.Filled.Favorite
            )

            Box(Modifier.width(1.dp).height(16.dp).background(Color.White.copy(alpha = 0.08f)))

            MiniTab(
                selected = currentScreen == AppScreen.PROFILE,
                onClick = { onScreenChange(AppScreen.PROFILE) },
                icon = Icons.Filled.Person
            )
        }
    }
}

@Composable
private fun MiniTab(selected: Boolean, onClick: () -> Unit, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    val alpha by animateFloatAsState(if (selected) 1f else 0.45f, tween(180), label = "ta")
    val sc by animateFloatAsState(if (selected) 1f else 0.95f, tween(180), label = "ts")
    val ia by animateFloatAsState(if (selected) 1f else 0f, tween(180), label = "ti")
    Box(Modifier.size(40.dp).scale(sc).clip(CircleShape).clickable(remember { MutableInteractionSource() }, null) { onClick() }, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.offset(y = (-2).dp)) {
            Icon(icon, null, tint = Color.White.copy(alpha = alpha), modifier = Modifier.size(18.dp))
            Spacer(Modifier.height(4.dp))
            Box(Modifier.width(14.dp).height(2.dp).clip(RoundedCornerShape(2.dp)).background(Color.White.copy(alpha = 0.75f * ia)))
        }
    }
}


@Composable
private fun FavoritesScreen(tracks: List<Track>, albums: List<Album>, currentTrack: Track?, isPlaying: Boolean, contentPadding: PaddingValues, bottomPadding: Dp, likedTracks: Set<Int>, onTrackClick: (Track) -> Unit, onToggleLike: (Int) -> Unit, onDownloadTrack: (Track) -> Unit) {
    if (tracks.isEmpty()) {
        Box(Modifier.padding(contentPadding).fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.size(80.dp).background(Brush.radialGradient(listOf(Color(0xFFFF6B8A).copy(alpha = 0.20f), Color.Transparent)), CircleShape).border(1.dp, Color(0xFFFF6B8A).copy(alpha = 0.30f), CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.FavoriteBorder, null, tint = Color(0xFFFF6B8A).copy(alpha = 0.70f), modifier = Modifier.size(36.dp))
                }
                Spacer(Modifier.height(20.dp))
                Text("Нет избранных треков", color = Color.White.copy(alpha = 0.60f), fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                Text("Нажми ❤️ на треке чтобы добавить", color = Color.White.copy(alpha = 0.35f), fontSize = 13.sp, textAlign = TextAlign.Center)
            }
        }
    } else {
        LazyColumn(modifier = Modifier.padding(contentPadding).fillMaxSize(), contentPadding = PaddingValues(bottom = bottomPadding, top = 4.dp)) {
            item { SectionLabel("Избранное") }
                items(tracks, key = { it.id }) { track ->
                    val alb = albums.firstOrNull { it.id == track.albumId }
                    FavoriteTrackRow(track = track, coverUrl = track.coverUrl ?: alb?.coverUrl, isActive = currentTrack?.id == track.id, isPlaying = isPlaying, isLiked = track.id in likedTracks, isDownloaded = TrackDownloadManager.isDownloaded(track.id), onClick = { onTrackClick(track) }, onToggleLike = { onToggleLike(track.id) }, onDownload = { onDownloadTrack(track) })
            }
        }
    }
}

@Composable
private fun FavoriteTrackRow(track: Track, coverUrl: String?, isActive: Boolean, isPlaying: Boolean, isLiked: Boolean, isDownloaded: Boolean, onClick: () -> Unit, onToggleLike: () -> Unit, onDownload: () -> Unit) {
    val bgA by animateFloatAsState(if (isActive) 0.13f else 0f, tween(200), label = "fb")
    val brA by animateFloatAsState(if (isActive) 0.22f else 0.07f, tween(200), label = "fbb")
    Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 5.dp).clip(RoundedCornerShape(16.dp)).background(Color.White.copy(alpha = bgA)).border(1.dp, Color.White.copy(alpha = brA), RoundedCornerShape(16.dp)).clickable { onClick() }.padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(46.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFF2A2A2A)), contentAlignment = Alignment.Center) {
            if (coverUrl != null) {
                AsyncImage(
                    model = coil.request.ImageRequest.Builder(LocalContext.current)
                        .data(coverUrl)
                        .crossfade(200)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            if (isActive) Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.50f)), contentAlignment = Alignment.Center) {
                PlayingIndicatorDots(isPlaying = isPlaying)
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(track.title, color = if (isActive) Color.White else Color.White.copy(alpha = 0.90f), fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal, fontSize = 14.sp)
            Text(track.displayArtist, color = Color.White.copy(alpha = 0.42f), fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Spacer(Modifier.width(8.dp))
        Text(track.duration, color = Color.White.copy(alpha = 0.38f), fontSize = 12.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
        Spacer(Modifier.width(4.dp))
        if (isDownloaded) {
            Icon(
                Icons.Filled.Check,
                null,
                tint = Color(0xFF80CBC4),
                modifier = Modifier.size(16.dp)
            )
        } else {
            IconButton(onDownload, Modifier.size(32.dp)) {
                Icon(
                    Icons.Filled.Download,
                    null,
                    tint = Color.White.copy(alpha = 0.40f),
                    modifier = Modifier.size(14.dp)
                )
            }
        }
        IconButton(onClick = onToggleLike, modifier = Modifier.size(32.dp)) {
            Icon(if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder, null, tint = if (isLiked) Color(0xFFFF6B8A) else Color.White.copy(alpha = 0.28f), modifier = Modifier.size(16.dp))
        }
    }
}
@Composable
private fun ProfileScreen(
    albums: List<Album>,
    contentPadding: PaddingValues,
    bottomPadding: Dp
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember { context.getSharedPreferences("lifon_prefs", Context.MODE_PRIVATE) }

    var username by remember { mutableStateOf<String?>(null) }
    var avatarUrl by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(true) }
    var stats by remember { mutableStateOf<StatsResult?>(null) }
    var achievements by remember { mutableStateOf<List<UserAchievement>>(emptyList()) }
    var supporters by remember { mutableStateOf<List<Triple<String, String, String>>>(emptyList()) }
    var isUploadingAvatar by remember { mutableStateOf(false) }

    val allTracks = remember(albums) { albums.flatMap { it.tracks } }

    val avatarPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        val token = prefs.getString("auth_token", null) ?: return@rememberLauncherForActivityResult
        isUploadingAvatar = true
        scope.launch {
            try {
                val url = withContext(Dispatchers.IO) {
                    val contentResolver = context.contentResolver
                    val inputStream = contentResolver.openInputStream(uri) ?: return@withContext null
                    val bytes = inputStream.readBytes()
                    inputStream.close()

                    val boundary = "----LifonMUSIC${System.currentTimeMillis()}"
                    val conn = (URL("${ApiConfig.BASE}/profile/avatar").openConnection() as java.net.HttpURLConnection).apply {
                        requestMethod = "POST"
                        setRequestProperty("Authorization", "Bearer $token")
                        setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
                        doOutput = true
                        connectTimeout = 15000
                        readTimeout = 15000
                    }
                    val body = java.io.ByteArrayOutputStream()
                    body.write("--$boundary\r\n".toByteArray())
                    body.write("Content-Disposition: form-data; name=\"file\"; filename=\"avatar.jpg\"\r\n".toByteArray())
                    body.write("Content-Type: image/jpeg\r\n\r\n".toByteArray())
                    body.write(bytes)
                    body.write("\r\n--$boundary--\r\n".toByteArray())
                    conn.outputStream.use { it.write(body.toByteArray()) }

                    val code = conn.responseCode
                    val text = if (code in 200..299) conn.inputStream.bufferedReader().readText() else null
                    if (text != null) {
                        val json = JSONObject(text)
                        if (json.optBoolean("ok")) json.optString("avatar_url") else null
                    } else null
                }
                if (url != null) avatarUrl = url
            } catch (_: Exception) { }
            isUploadingAvatar = false
        }
    }

    LaunchedEffect(Unit) {
        val token = prefs.getString("auth_token", null)

        if (token.isNullOrBlank() || token == "guest") {
            username = "Гость"
            loading = false
            return@LaunchedEffect
        }

        try {
            val (code, body) = withContext(Dispatchers.IO) {
                val conn = (URL("${ApiConfig.BASE}/profile").openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    setRequestProperty("Authorization", "Bearer $token")
                    connectTimeout = 8000
                    readTimeout = 8000
                }
                val c = conn.responseCode
                val t = if (c == 200) conn.inputStream.bufferedReader().readText() else null
                c to t
            }
            if (code == 200 && body != null) {
                val json = JSONObject(body)
                if (json.optBoolean("ok")) {
                    val user = json.getJSONObject("user")
                    username = user.getString("username")
                    avatarUrl = user.optString("avatar_url").takeIf { it.isNotBlank() }
                }
            } else {
                username = "Недоступно"
            }
        } catch (_: Exception) {
            username = "Недоступно"
        }

        stats = ListenTracker.fetchStats(token)
        achievements = AchievementsRepository.fetchMyAchievements(token)
        loading = false
    }

    LaunchedEffect(Unit) {
        try {
            val conn = withContext(Dispatchers.IO) {
                (URL("${ApiConfig.BASE}/supporters").openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 6000
                    readTimeout = 6000
                }
            }
            val text = withContext(Dispatchers.IO) { conn.inputStream.bufferedReader().readText() }
            val json = JSONObject(text)
            if (json.optBoolean("ok")) {
                val arr = json.getJSONArray("supporters")
                supporters = (0 until arr.length()).map { i ->
                    val obj = arr.getJSONObject(i)
                    Triple(
                        obj.optString("name", ""),
                        obj.optString("handle", ""),
                        obj.optString("color", "#8b5cf6")
                    )
                }.filter { it.first.isNotBlank() }
            }
        } catch (_: Exception) { }
    }

    LazyColumn(
        modifier = Modifier.padding(contentPadding).fillMaxSize(),
        contentPadding = PaddingValues(bottom = bottomPadding, top = 12.dp)
    ) {
        item {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {

                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.06f))
                        .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(20.dp))
                        .padding(20.dp)
                ) {
                    if (loading) {
                        Text("Загрузка...", color = Color.White.copy(alpha = 0.6f))
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE8D5FF).copy(alpha = 0.15f))
                                    .border(1.dp, Color(0xFFE8D5FF).copy(alpha = 0.25f), CircleShape)
                                    .clickable(enabled = username != "Гость" && !isUploadingAvatar) {
                                        avatarPicker.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isUploadingAvatar) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = Color(0xFFE8D5FF),
                                        strokeWidth = 2.dp
                                    )
                                } else if (avatarUrl != null) {
                                    AsyncImage(
                                        model = avatarUrl,
                                        contentDescription = "Avatar",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            (username ?: "?").take(1).uppercase(),
                                            color = Color(0xFFE8D5FF),
                                            fontWeight = FontWeight.Black,
                                            fontSize = 22.sp
                                        )
                                        if (username != "Гость") {
                                            Text(
                                                "Фото",
                                                color = Color(0xFFE8D5FF).copy(alpha = 0.5f),
                                                fontSize = 8.sp
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(Modifier.width(14.dp))
                            Column {
                                Text(
                                    username ?: "Неизвестно",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    if (username == "Гость") "Гостевой аккаунт" else "Слушатель CUPSIZE",
                                    color = Color.White.copy(alpha = 0.40f),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                if (!loading && username != "Гость" && stats != null) {
                    Spacer(Modifier.height(16.dp))

                    Text(
                        "СТАТИСТИКА",
                        color = Color.White.copy(alpha = 0.35f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )

                    Spacer(Modifier.height(10.dp))

                    val totalMinutes = (stats!!.totalMs / 1000 / 60)
                    StatCard(
                        emoji = "🎧",
                        label = "Прослушано всего:",
                        value = "$totalMinutes мин"
                    )

                    Spacer(Modifier.height(10.dp))

                    val favoriteTrack = stats!!.topTracks.firstOrNull()
                        ?.let { top ->
                            allTracks.firstOrNull { it.id == top.trackId }
                        }

                    if (favoriteTrack != null) {
                        Text(
                            "ЛЮБИМЫЙ ТРЕК",
                            color = Color.White.copy(alpha = 0.35f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                        Spacer(Modifier.height(10.dp))

                        val favoriteAlbum = albums.firstOrNull { it.id == favoriteTrack.albumId }
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White.copy(alpha = 0.06f))
                                .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(16.dp))
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val favCoverUrl = favoriteTrack.coverUrl ?: favoriteAlbum?.coverUrl
                            if (favCoverUrl != null) {
                                AsyncImage(
                                    model = favCoverUrl,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(Modifier.width(14.dp))
                            }
                            Column(Modifier.weight(1f)) {
                                Text(
                                    "❤️ Любимый трек",
                                    color = Color(0xFFFF6B8A).copy(alpha = 0.80f),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 1.sp
                                )
                                Spacer(Modifier.height(3.dp))
                                Text(
                                    favoriteTrack.title,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    favoriteTrack.displayArtist,
                                    color = Color.White.copy(alpha = 0.40f),
                                    fontSize = 12.sp
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                            val topCount = stats!!.topTracks.firstOrNull()?.playCount ?: 0
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "$topCount",
                                    color = Color(0xFFE8D5FF),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 22.sp
                                )
                                Text(
                                    "раз",
                                    color = Color.White.copy(alpha = 0.35f),
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))
                    }

                    if (stats!!.topTracks.isNotEmpty()) {
                        Text(
                            "ТОП ТРЕКОВ",
                            color = Color.White.copy(alpha = 0.35f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                        Spacer(Modifier.height(10.dp))

                        stats!!.topTracks.take(5).forEachIndexed { index, topTrack ->
                            val track = allTracks.firstOrNull { it.id == topTrack.trackId }
                                ?: return@forEachIndexed
                            val album = albums.firstOrNull { it.id == track.albumId }

                            TopTrackRow(
                                position = index + 1,
                                track = track,
                                coverUrl = track.coverUrl ?: album?.coverUrl,
                                playCount = topTrack.playCount
                            )
                            if (index < minOf(stats!!.topTracks.size, 5) - 1) Spacer(Modifier.height(8.dp))
                        }

                        Spacer(Modifier.height(16.dp))
                    }
                }

                if (!loading && username != "Гость" && achievements.isNotEmpty()) {
                    Spacer(Modifier.height(16.dp))

                    Text(
                        "ДОСТИЖЕНИЯ",
                        color = Color.White.copy(alpha = 0.35f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    Spacer(Modifier.height(10.dp))

                    achievements.forEach { ach ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.White.copy(alpha = 0.06f))
                                .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(14.dp))
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (ach.iconUrl != null) {
                                AsyncImage(
                                    model = ach.iconUrl,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFE8D5FF).copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("🏆", fontSize = 16.sp)
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    ach.name,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                                if (ach.description.isNotBlank()) {
                                    Text(
                                        ach.description,
                                        color = Color.White.copy(alpha = 0.40f),
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                    }

                    Spacer(Modifier.height(16.dp))
                }

                if (!loading && username == "Гость") {
                    Spacer(Modifier.height(16.dp))
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFE8D5FF).copy(alpha = 0.05f))
                            .border(1.dp, Color(0xFFE8D5FF).copy(alpha = 0.12f), RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("📊", fontSize = 20.sp)
                            Column {
                                Text(
                                    "Статистика недоступна",
                                    color = Color.White.copy(alpha = 0.75f),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "Войдите в аккаунт чтобы отслеживать любимые треки и время прослушивания",
                                    color = Color.White.copy(alpha = 0.40f),
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }

                Spacer(Modifier.height(16.dp))

                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFFF5C6C))
                        .clickable {
                            prefs.edit().remove("auth_token").apply()
                            val intent = Intent(context, SplashActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            context.startActivity(intent)
                        }
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (username == "Гость") "Выйти из гостевого режима" else "Выйти из аккаунта",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                if (supporters.isNotEmpty()) {
                    Spacer(Modifier.height(20.dp))
                    Text(
                        "СПАСИБО ЗА ПОДДЕРЖКУ",
                        color = Color.White.copy(alpha = 0.25f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(10.dp))

                    supporters.forEach { (name, handle, colorHex) ->
                        val chipColor = runCatching {
                            Color(android.graphics.Color.parseColor(colorHex))
                        }.getOrElse { Color(0xFFE8D5FF) }
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.04f))
                                .border(1.dp, Color.White.copy(alpha = 0.07f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(chipColor.copy(alpha = 0.15f))
                                        .border(1.dp, chipColor.copy(alpha = 0.30f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        name.take(1).uppercase(),
                                        color = chipColor.copy(alpha = 0.85f),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                                Text(
                                    name,
                                    color = Color.White.copy(alpha = 0.70f),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                            }
                            Text(
                                handle,
                                color = Color.White.copy(alpha = 0.30f),
                                fontSize = 12.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
private fun StatCard(emoji: String, label: String, value: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(emoji, fontSize = 22.sp)
        Spacer(Modifier.width(14.dp))
        Column {
            Text(label, color = Color.White.copy(alpha = 0.45f), fontSize = 11.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(2.dp))
            Text(value, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun TopTrackRow(position: Int, track: Track, coverUrl: String?, playCount: Int) {
    val numColor = when (position) {
        1 -> Color(0xFFFFD700)
        2 -> Color(0xFFB0BEC5)
        else -> Color(0xFFCD7F32)
    }
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "$position",
            color = numColor,
            fontWeight = FontWeight.Black,
            fontSize = 18.sp,
            modifier = Modifier.width(24.dp),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.width(10.dp))
        if (coverUrl != null) {
            AsyncImage(
                model = coverUrl,
                contentDescription = null,
                modifier = Modifier.size(42.dp).clip(RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(10.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(track.title, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(track.displayArtist, color = Color.White.copy(alpha = 0.40f), fontSize = 11.sp)
        }
        Spacer(Modifier.width(8.dp))
        Text("$playCount прослуш.", color = Color.White.copy(alpha = 0.35f), fontSize = 11.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisclaimerBanner(onDismiss: () -> Unit) {
    val ss = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = ss, containerColor = Color.Transparent, dragHandle = null, tonalElevation = 0.dp) {
        Box(Modifier.fillMaxWidth().wrapContentHeight().background(Brush.verticalGradient(listOf(Color(0xFF1A1028), Color(0xFF0D0D10))), RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)).border(BorderStroke(1.dp, Color.White.copy(alpha = 0.10f)), RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)).navigationBarsPadding().padding(horizontal = 24.dp, vertical = 24.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.width(40.dp).height(4.dp).clip(RoundedCornerShape(2.dp)).background(Color.White.copy(alpha = 0.20f)).align(Alignment.CenterHorizontally))
                Spacer(Modifier.height(20.dp))
                Box(Modifier.size(56.dp).background(Brush.radialGradient(listOf(Color(0xFFE8D5FF).copy(alpha = 0.25f), Color.Transparent)), CircleShape).border(1.dp, Color(0xFFE8D5FF).copy(alpha = 0.30f), CircleShape), contentAlignment = Alignment.Center) { Text("ℹ️", fontSize = 24.sp) }
                Spacer(Modifier.height(16.dp))
                Text("О приложении", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp, letterSpacing = (-0.3).sp)
                Spacer(Modifier.height(12.dp))
                Text("LifonMUSIC — независимый проект и не имеет отношения к официальным представителям группы CUPSIZE. Если вам нравится их творчество, поддержите артистов: купите мерч или сходите на концерт!", color = Color.White.copy(alpha = 0.72f), fontSize = 14.sp, lineHeight = 21.sp, textAlign = TextAlign.Center)
                Spacer(Modifier.height(24.dp))
                Text("АВТОР ИДЕИ", color = Color.White.copy(alpha = 0.45f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.5.sp, modifier = Modifier.align(Alignment.Start))
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SocialButton("Telegram", "✈️", "https://t.me/+Z8CswTkqC4c0YzM6", Color(0xFF1A73C8), Modifier.weight(1f))
                    SocialButton("TikTok", "🎵", "https://www.tiktok.com/@dangeershark_t.t", Color(0xFF1A1A1A), Modifier.weight(1f))
                }
                Spacer(Modifier.height(16.dp))
                Text("КОДЕРЫ", color = Color.White.copy(alpha = 0.45f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.5.sp, modifier = Modifier.align(Alignment.Start))
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SocialButton("Telegram", "✈️", "https://t.me/videlsvet", Color(0xFF1A73C8), Modifier.weight(1f))
                    SocialButton("TikTok", "🎵", "https://www.tiktok.com/@wave66181?_r=1&_t=ZS-94DvxyuzLYi", Color(0xFF1A1A1A), Modifier.weight(1f))
                }
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SocialButton("Telegram", "✈️", "https://t.me/qyrex_", Color(0xFF1A73C8), Modifier.weight(1f))
                    SocialButton("TikTok", "🎵", "https://www.tiktok.com/@dev.magnum", Color(0xFF1A1A1A), Modifier.weight(1f))
                }
                Spacer(Modifier.height(16.dp))
                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color.White).clickable { onDismiss() }.padding(vertical = 14.dp), contentAlignment = Alignment.Center) {
                    Text("Понятно!", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}

@Composable
private fun SocialButton(label: String, emoji: String, url: String, bg: Color, modifier: Modifier = Modifier) {
    val ctx = LocalContext.current
    Box(modifier.clip(RoundedCornerShape(14.dp)).background(bg.copy(alpha = 0.85f)).border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(14.dp)).clickable { ctx.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))) }.padding(vertical = 12.dp, horizontal = 8.dp), contentAlignment = Alignment.Center) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(emoji, fontSize = 16.sp)
            Text(label, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CupsizeInfoSheet(onDismiss: () -> Unit) {
    val ss = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scroll = rememberScrollState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = ss,
        containerColor = Color.Transparent,
        dragHandle = null,
        tonalElevation = 0.dp
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .background(
                    Brush.verticalGradient(listOf(Color(0xFF1A1028), Color(0xFF0D0D10))),
                    RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                )
                .border(
                    BorderStroke(1.dp, Color.White.copy(alpha = 0.10f)),
                    RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                )
                .navigationBarsPadding()
                .padding(horizontal = 22.dp, vertical = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scroll)
            ) {
                Box(
                    Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color.White.copy(alpha = 0.20f))
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(Modifier.height(14.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.10f))
                            .border(1.dp, Color.White.copy(alpha = 0.16f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Info,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.90f),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            "CUPSIZE",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            letterSpacing = (-0.2).sp
                        )
                        Text(
                            "Краткая справка",
                            color = Color.White.copy(alpha = 0.55f),
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                Text(
                    "CUPSIZE — российская группа из Ярославля, играющая смесь гранжа, гаражного рока и абсурдной иронии. Название переводится как «Размер чашечки лифчика». Свой стиль музыканты называют «гаражной залупой», «компьютерным роком», «ПМС-гранжем» или «музыкой сюра».",
                    color = Color.White.copy(alpha = 0.78f),
                    fontSize = 14.sp,
                    lineHeight = 21.sp
                )

                Spacer(Modifier.height(18.dp))
                InfoSectionTitle("Текущий состав:")
                InfoBullet("Николай Мамаев (он же Лина Лифонова, род. 16 июля 2003) — фронтмен, вокалист, автор песен, основатель.")
                InfoBullet("Ярослав Макаренко — бас-гитара.")
                InfoBullet("Сергей Крылов (Серега КПЗ) — барабанщик (с 2023 года).")
                InfoBullet("Андрей Колесов — второй гитарист.")

                Spacer(Modifier.height(14.dp))
                InfoSectionTitle("Бывший состав:")
                InfoBullet("Андрей Калинин — гитара.")
                InfoBullet("Аркадий — барабаны.")
                InfoBullet("Семён Куликов — бас-гитара.")

                Spacer(Modifier.height(14.dp))
                InfoSectionTitle("Команда:")
                InfoBullet("Александр Пташинский (Птаха) — звукорежиссёр, «человек, который всё сводит».")

                Spacer(Modifier.height(14.dp))
                InfoSectionTitle("Дискография:")
                InfoBullet("2023 — «дели на два», «Как испортить вечеринку?»")
                InfoBullet("2024 — «кажется, в аду прикольно, но меня выгнали б утром»")
                InfoBullet("2025 — «в моих легких выросли цветы» (вошёл в топ-20 лучших релизов года по версии The Flow), «неуравновешеннолетниепесни pt.1», «прыгайдуравишлист!»")
                InfoBullet("2026 (в процессе) — «Заставь меня плакать».")

                Spacer(Modifier.height(14.dp))
                InfoSectionTitle("О музыке и смыслах:")
                InfoBullet("Свою музыку Коля описывает как «про юмор и иронию». Тексты часто строятся на абсурдных ситуациях из жизни друзей или выдуманных персонажах.")
                InfoBullet("Термин «ПМС-гранж» родился из-за резкой смены настроения в треках: «Бывает грустно, бывает, чувствую себя агрессивно, и вот так меня постоянно метает. Я подумал, что я буквально как девочка с этим синдромом».")
                InfoBullet("Некоторые песни написаны от лица девушек — «это расширяет сюжетные возможности».")
                InfoBullet("Псевдоним Лина Лифонова нужен, чтобы отделить сценический образ от повседневности: «Мне не очень нравится, когда меня называет Колей кто-то, кроме друзей». Под этим именем он планирует писать техно-рэп.")

                Spacer(Modifier.height(14.dp))
                InfoSectionTitle("Концерты и жизнь группы:")
                InfoBullet("В 2025 году группа дала больше 70 концертов, включая фестиваль «МТС Live Лето», и собрала 3000 зрителей в Москве.")
                InfoBullet("На концертах им часто кидают лифчики (из-за названия). Коля относится к этому с юмором: «Для Киркорова цветы, для нас — бюстгальтеры».")
                InfoBullet("Лучший концерт, по мнению Коли, был в Тюмени: «Мы просто поймали друг друга, а дальше всё полилось само. Потом подошли к пацанам, обнялись и легли спать вместе».")
                InfoBullet("Худший — в Москве после выписки из больницы, когда он перебрал с алкоголем и не мог нормально играть: «Сейчас на концертах я очень мало пью: только \"для связок\"».")

                Spacer(Modifier.height(14.dp))
                InfoSectionTitle("Личные истории:")
                InfoBullet("До успеха Коля работал в зоопарке (продавал магнитики), помогал дяде ставить камеры наблюдения и продавал плохие биты в стиле Lil Pump, удивляясь, что их кто-то покупал.")
                InfoBullet("Очень долго копил на свою первую гитару.")
                InfoBullet("Гитару, на которой сейчас играет, его друг каким-то образом выкупил у бездомного в США. Коля до сих пор в недоумении: «Странно, что человек без дома, но с гитарой... Теперь у него точно ничего нет. Зато он с бабками».")
                InfoBullet("Учился на слесаря (вдохновившись футболкой Платины), потом перевёлся на юриспруденцию, но оба колледжа бросил.")
                InfoBullet("Ненавидит причёску «полубокс» и готов «отдать любые деньги, чтоб люди так не стриглись».")

                Spacer(Modifier.height(14.dp))
                InfoSectionTitle("Важные факты:")
                InfoBullet("Раньше CUPSIZE был сольным проектом Николая, но с 2024 года превратился в группу. Со стримингов убрали старые фиты, оставив только записи с коллективом.")
                InfoBullet("В январе 2024 года Николая оштрафовали на 5000 рублей за пропаганду наркотиков в текстах, после чего альбом «Еби меня, малышка» удалили с площадок.")
                InfoBullet("Коля и Птаха лежали в одном психоневрологическом стационаре в Ярославле с разницей в год — оба с тревожным расстройством. Коля вспоминает, что друзья передали ему в пироге второй телефон, который он прятал от врачей.")
                InfoBullet("Дружит и часто общается с Madk1d, Sqwore, 17 Seventeen в общем дискорде.")
                InfoBullet("Несмотря на успех, остаётся жить в Ярославле, потому что там «все самые близкие кенты, мама, отец, брат». При этом узнают на улицах уже постоянно, даже в магазине за туалетной бумагой.")

                Spacer(Modifier.height(6.dp))
                InfoBullet("За информацию большое спасибо @strannow, интервью от The Flow")

                Spacer(Modifier.height(18.dp))

                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .clickable { onDismiss() }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Закрыть", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }

                Spacer(Modifier.height(6.dp))
            }
        }
    }
}

@Composable
private fun InfoSectionTitle(text: String) {
    Text(
        text,
        color = Color.White.copy(alpha = 0.85f),
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.2.sp
    )
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun InfoBullet(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text("•", color = Color.White.copy(alpha = 0.65f), fontSize = 16.sp, modifier = Modifier.padding(end = 10.dp))
        Text(
            text,
            color = Color.White.copy(alpha = 0.72f),
            fontSize = 14.sp,
            lineHeight = 20.sp,
            modifier = Modifier.weight(1f)
        )
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DiscographyRoadmapSheet(
    albums: List<Album>,
    onAlbumClick: (Album) -> Unit,
    onDismiss: () -> Unit
) {
    val ss = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val yearGroups = remember(albums) {
        albums
            .groupBy { it.year }
            .toList()
            .sortedBy { (year, _) -> year.toIntOrNull() ?: 0 }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = ss,
        containerColor = Color.Transparent,
        dragHandle = null,
        tonalElevation = 0.dp
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .background(
                    Brush.verticalGradient(listOf(Color(0xFF1A1028), Color(0xFF0D0D10))),
                    RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                )
                .border(
                    BorderStroke(1.dp, Color.White.copy(alpha = 0.10f)),
                    RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                )
                .navigationBarsPadding()
                .padding(horizontal = 18.dp, vertical = 16.dp)
        ) {
            Column(Modifier.fillMaxWidth()) {

                Box(
                    Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(44.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color.White.copy(alpha = 0.20f))
                )

                Spacer(Modifier.height(14.dp))

                Text(
                    "ДИСКОГРАФИЯ",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )

                Spacer(Modifier.height(14.dp))

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    items(
                        items = yearGroups,
                        key = { (year, _) -> year }
                    ) { (year, list) ->
                        val firstYear = yearGroups.firstOrNull()?.first
                        val lastYear = yearGroups.lastOrNull()?.first

                        YearRoadmapGroupItem(
                            year = year,
                            albums = list.sortedBy { it.id },
                            isFirst = year == firstYear,
                            isLast = year == lastYear,
                            onAlbumClick = onAlbumClick
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .clickable { onDismiss() }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Закрыть",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EqualizerSheet(
    equalizer: PlayerEqualizer,
    onDismiss: () -> Unit
) {
    val ss = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scroll = rememberScrollState()

    // Пресеты: название → уровни для каждой полосы (в миллибелах, масштабируется под реальный range)
    val presets = listOf(
        "Плоский" to listOf(0, 0, 0, 0, 0),
        "Бас" to listOf(800, 500, 0, -200, -300),
        "Вокал" to listOf(-300, 0, 500, 400, 0),
        "Рок" to listOf(500, 200, -300, 200, 500),
        "Поп" to listOf(-100, 300, 500, 300, -100),
        "Джаз" to listOf(300, 0, 200, 0, 300),
    )

    var selectedPreset by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = ss,
        containerColor = Color.Transparent,
        dragHandle = null,
        tonalElevation = 0.dp
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .background(
                    Brush.verticalGradient(listOf(Color(0xFF1A1028), Color(0xFF0D0D10))),
                    RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                )
                .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.10f)), RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth().verticalScroll(scroll)) {
                Box(
                    Modifier.align(Alignment.CenterHorizontally).width(44.dp).height(4.dp)
                        .clip(RoundedCornerShape(2.dp)).background(Color.White.copy(alpha = 0.20f))
                )

                Spacer(Modifier.height(16.dp))

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("ЭКВАЛАЙЗЕР", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
                    // Компактная кнопка сброса
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                            .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(10.dp))
                            .clickable {
                                equalizer.resetAll()
                                selectedPreset = "Плоский"
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Сбросить", color = Color.White.copy(alpha = 0.75f), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }

                Spacer(Modifier.height(14.dp))

                // Пресеты — горизонтальная прокрутка
                Text("ПРЕСЕТЫ", color = Color.White.copy(alpha = 0.35f), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
                Spacer(Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    items(presets, key = { it.first }) { (name, levels) ->
                        val isSelected = selectedPreset == name
                        Box(
                            Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(if (isSelected) Color.White else Color.White.copy(alpha = 0.08f))
                                .border(1.dp, if (isSelected) Color.Transparent else Color.White.copy(alpha = 0.12f), RoundedCornerShape(999.dp))
                                .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {
                                    selectedPreset = name
                                    val bandCount = equalizer.getBandCount()
                                    val range = equalizer.getBandLevelRange()
                                    levels.take(bandCount).forEachIndexed { i, rawLevel ->
                                        val scaled = rawLevel.coerceIn(range.first, range.last)
                                        equalizer.setBandLevel(i, scaled)
                                    }
                                }
                                .padding(horizontal = 14.dp, vertical = 7.dp)
                        ) {
                            Text(
                                name,
                                color = if (isSelected) Color.Black else Color.White.copy(alpha = 0.75f),
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(Modifier.height(18.dp))

                if (!equalizer.isReady() || equalizer.getBandCount() == 0) {
                    Box(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.06f))
                            .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Text(
                            "Эквалайзер недоступен на этом устройстве или аудиосессия ещё не готова.",
                            color = Color.White.copy(alpha = 0.65f), fontSize = 14.sp, lineHeight = 20.sp
                        )
                    }
                } else {
                    val bandCount = equalizer.getBandCount()
                    val range = equalizer.getBandLevelRange()

                    for (i in 0 until bandCount) {
                        var value by remember(i, bandCount) { mutableStateOf(equalizer.getBandLevel(i).toFloat()) }

                        // Обновляем слайдер когда применяется пресет
                        LaunchedEffect(selectedPreset) {
                            value = equalizer.getBandLevel(i).toFloat()
                        }

                        Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(equalizer.getCenterFreqLabel(i), color = Color.White.copy(alpha = 0.90f), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Text("${value.toInt() / 100f} dB", color = Color.White.copy(alpha = 0.40f), fontSize = 11.sp)
                            }
                            Slider(
                                value = value,
                                onValueChange = {
                                    value = it
                                    equalizer.setBandLevel(i, it.toInt())
                                    selectedPreset = null
                                },
                                valueRange = range.first.toFloat()..range.last.toFloat()
                            )
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                Box(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color.White)
                        .clickable { onDismiss() }.padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Готово", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                Spacer(Modifier.height(6.dp))
            }
        }
    }
}
@Composable
private fun YearRoadmapGroupItem(
    year: String,
    albums: List<Album>,
    isFirst: Boolean,
    isLast: Boolean,
    onAlbumClick: (Album) -> Unit
) {
    val lineColor = Color.White.copy(alpha = 0.14f)
    val chipBg = Color.White.copy(alpha = 0.08f)
    val chipBorder = Color.White.copy(alpha = 0.14f)

    Column(
        modifier = Modifier.width(240.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(34.dp)
                .drawBehind {
                    val cy = size.height / 2f
                    val cx = size.width / 2f

                    val left = if (isFirst) cx else 0f
                    val right = if (isLast) cx else size.width

                    drawLine(
                        color = lineColor,
                        start = Offset(left, cy),
                        end = Offset(right, cy),
                        strokeWidth = 4f
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(chipBg)
                    .border(1.dp, chipBorder, RoundedCornerShape(999.dp))
                    .padding(horizontal = 12.dp, vertical = 5.dp)
            ) {
                Text(
                    text = year,
                    color = Color(0xFFE8D5FF),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            albums.forEach { alb ->
                YearAlbumCardMini(
                    album = alb,
                    onClick = { onAlbumClick(alb) }
                )
            }
        }
    }
}

@Composable
private fun YearAlbumCardMini(
    album: Album,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(18.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Color.White.copy(alpha = 0.06f))
            .border(1.dp, Color.White.copy(alpha = 0.10f), shape)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() }
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = album.coverUrl,
            contentDescription = null,
            modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(14.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(Modifier.width(12.dp))

        Column(Modifier.weight(1f)) {
            Text(
                text = album.title,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "${album.tracks.size} треков",
                color = Color.White.copy(alpha = 0.45f),
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun AppTopBar(
    title: String,
    subtitle: String,
    showBack: Boolean,
    search: String,
    showSearch: Boolean,
    onSearchChange: (String) -> Unit,
    onBack: () -> Unit,
    onAboutClick: () -> Unit,
    onDiscographyClick: () -> Unit
) {
    var showInfo by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(Color.Black.copy(alpha = 0.55f), Color.Transparent)
                )
            )
            .statusBarsPadding()
            .padding(start = 8.dp, end = 16.dp, top = 8.dp, bottom = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (showBack) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, null, tint = Color.White, modifier = Modifier.size(22.dp))
                }
            } else {
                Spacer(Modifier.width(16.dp))
            }

            Column(Modifier.weight(1f)) {
                AnimatedContent(
                    targetState = title,
                    transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(180)) },
                    label = "tt"
                ) { t ->
                    if (!showBack && t == "CUPSIZE") {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = t,
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 26.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Clip,
                                letterSpacing = (-0.5).sp
                            )

                            Spacer(Modifier.width(8.dp))

                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.10f))
                                    .border(1.dp, Color.White.copy(alpha = 0.18f), CircleShape)
                                    .clickable(
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() }
                                    ) { showInfo = true },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Info,
                                    contentDescription = "О группе",
                                    tint = Color.White.copy(alpha = 0.85f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    } else {
                        Text(
                            text = t,
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = if (showBack) 19.sp else 26.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            letterSpacing = (-0.5).sp
                        )
                    }
                }

                val isDiscography = !showBack && subtitle == "Дискография"
                Text(
                    text = subtitle,
                    color = if (isDiscography) Color.White.copy(alpha = 0.70f) else Color.White.copy(alpha = 0.50f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(
                            enabled = isDiscography,
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { onDiscographyClick() }
                        .padding(vertical = 2.dp, horizontal = 2.dp)
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { onAboutClick() }
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            ) {
                Text("LifonMUSIC", color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Text("by videlsvet", color = Color.White.copy(alpha = 0.45f), fontSize = 10.sp)
            }
        }

        if (showSearch) {
            Spacer(Modifier.height(8.dp))
            SearchBarField(search, onSearchChange)
        }
    }

    if (showInfo) {
        CupsizeInfoSheet(onDismiss = { showInfo = false })
    }
}

@Composable
private fun SearchBarField(value: String, onChange: (String) -> Unit) {
    OutlinedTextField(value = value, onValueChange = onChange, singleLine = true,
        leadingIcon = { Icon(Icons.Filled.Search, null, tint = Color.White.copy(alpha = 0.45f), modifier = Modifier.size(18.dp)) },
        placeholder = { Text("Поиск", color = Color.White.copy(alpha = 0.38f), fontSize = 14.sp) },
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Color.White.copy(alpha = 0.25f), unfocusedBorderColor = Color.White.copy(alpha = 0.10f), cursorColor = Color.White, focusedContainerColor = Color.White.copy(alpha = 0.07f), unfocusedContainerColor = Color.White.copy(alpha = 0.04f)),
        shape = RoundedCornerShape(16.dp)
    )
}


@Composable
private fun LibraryScreen(
    albums: List<Album>,
    allAlbums: List<Album>,
    currentTrack: Track?,
    isPlaying: Boolean,
    contentPadding: PaddingValues,
    bottomPadding: Dp,
    onAlbumClick: (Album) -> Unit,
    onTrackClick: (Track) -> Unit,
    onPlayAll: () -> Unit,
    onDownloadTrack: (Track) -> Unit,
    filteredTracks: List<Track> = emptyList(),
    searchQuery: String = ""
) {
    var allTracksExpanded by remember { mutableStateOf(false) }
    val isSearching = searchQuery.isNotBlank()


    val albumById = remember(allAlbums) { allAlbums.associateBy { it.id } }


    val allTracks = remember(allAlbums) { allAlbums.flatMap { it.tracks } }

    LazyColumn(
        modifier = Modifier.padding(contentPadding).fillMaxSize(),
        contentPadding = PaddingValues(bottom = bottomPadding, top = 4.dp)
    ) {
        if (isSearching) {
            if (filteredTracks.isNotEmpty()) {
                item { SectionLabel("Треки") }
                items(filteredTracks, key = { "st_${it.id}" }) { track ->
                    val alb = albumById[track.albumId]
                    if (alb != null) {
                        FlatTrackRow(
                            track = track,
                            coverUrl = track.coverUrl ?: alb.coverUrl,
                            isActive = currentTrack?.id == track.id,
                            isPlaying = isPlaying,
                            isDownloaded = TrackDownloadManager.isDownloaded(track.id),
                            onClick = { onTrackClick(track) },
                            onDownload = { onDownloadTrack(track) }
                        )
                    }
                }
            }

            if (albums.isNotEmpty()) {
                item { SectionLabel("Альбомы") }
                items(albums.chunked(2), key = { it.joinToString { a -> a.id.toString() } }) { row ->
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        row.forEach { AlbumCard(it, Modifier.weight(1f)) { onAlbumClick(it) } }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(12.dp))
                }
            }

            if (filteredTracks.isEmpty() && albums.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(top = 30.dp), contentAlignment = Alignment.Center) {
                        Text("Ничего не найдено", color = Color.White.copy(alpha = 0.45f))
                    }
                }
            }
        } else {
            if (albums.isNotEmpty()) {
                item { SectionLabel("Альбомы") }
                items(albums.chunked(2), key = { it.joinToString { a -> a.id.toString() } }) { row ->
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        row.forEach { AlbumCard(it, Modifier.weight(1f)) { onAlbumClick(it) } }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(12.dp))
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { allTracksExpanded = !allTracksExpanded }
                        .padding(start = 16.dp, top = 22.dp, bottom = 10.dp, end = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "ВСЕ ТРЕКИ",
                        color = Color.White.copy(alpha = 0.35f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.5.sp
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (!allTracksExpanded) {
                            Text(
                                "${allTracks.size} тр.",
                                color = Color.White.copy(alpha = 0.25f),
                                fontSize = 11.sp
                            )
                        }
                        val rot by animateFloatAsState(
                            if (allTracksExpanded) 180f else 0f,
                            tween(220),
                            label = "arr"
                        )
                        Icon(
                            Icons.Filled.KeyboardArrowDown,
                            null,
                            tint = Color.White.copy(alpha = 0.35f),
                            modifier = Modifier.size(18.dp).graphicsLayer { rotationZ = rot }
                        )
                    }
                }
            }

            if (allTracksExpanded) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 4.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White.copy(alpha = 0.07f))
                            .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(14.dp))
                            .clickable { onPlayAll() }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Filled.PlayArrow, null, tint = Color.White.copy(alpha = 0.80f), modifier = Modifier.size(18.dp))
                        Text("Играть всё", color = Color.White.copy(alpha = 0.80f), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                items(allTracks, key = { it.id }) { track ->
                    val alb = albumById[track.albumId] ?: return@items
                    FlatTrackRow(track, track.coverUrl ?: alb.coverUrl, currentTrack?.id == track.id, isPlaying, TrackDownloadManager.isDownloaded(track.id), { onTrackClick(track) }, { onDownloadTrack(track) })
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text.uppercase(), color = Color.White.copy(alpha = 0.35f), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.5.sp, modifier = Modifier.padding(start = 16.dp, top = 22.dp, bottom = 10.dp))
}

@Composable
private fun AlbumCard(
    album: Album,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(20.dp)
    var pressed by remember { mutableStateOf(false) }
    val sc by animateFloatAsState(
        if (pressed) 0.95f else 1f,
        spring(stiffness = Spring.StiffnessMediumLow),
        label = "cs"
    )
    LaunchedEffect(pressed) { if (pressed) { delay(120); pressed = false } }

    Card(
        modifier = modifier
            .scale(sc)
            .clickable(remember { MutableInteractionSource() }, null) {
                pressed = true
                onClick()
            },
        shape = shape,
        colors = CardDefaults.cardColors(Color.Transparent),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(Color.White.copy(alpha = 0.06f))
                .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.10f)), shape)
                .padding(10.dp)
        ) {
            Column {
                AsyncImage(
                    model = album.coverUrl,
                    contentDescription = album.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .shadow(16.dp, RoundedCornerShape(14.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(Modifier.height(10.dp))

                Text(
                    text = album.title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    YearBadge(album.year)
                    Text(
                        text = "${album.tracks.size} тр.",
                        color = Color.White.copy(alpha = 0.45f),
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun YearBadge(year: String) {
    Box(Modifier.background(Color.White.copy(alpha = 0.10f), RoundedCornerShape(999.dp)).border(BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)), RoundedCornerShape(999.dp)).padding(horizontal = 8.dp, vertical = 3.dp)) {
        Text(year, color = Color.White.copy(alpha = 0.75f), fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun FlatTrackRow(track: Track, coverUrl: String?, isActive: Boolean, isPlaying: Boolean, isDownloaded: Boolean, onClick: () -> Unit, onDownload: () -> Unit) {
    val bg by animateFloatAsState(if (isActive) 0.12f else 0f, tween(200), label = "fb")
    Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 3.dp).clip(RoundedCornerShape(14.dp)).background(Color.White.copy(alpha = bg)).clickable { onClick() }.padding(horizontal = 10.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
        AsyncImage(model = coverUrl, contentDescription = null, modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)), contentScale = ContentScale.Crop)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(track.title, color = if (isActive) Color.White else Color.White.copy(alpha = 0.88f), fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal, fontSize = 13.sp)
            Text(track.displayArtist, color = Color.White.copy(alpha = 0.45f), fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        if (isActive) { PlayingIndicatorDots(isPlaying = isPlaying); Spacer(Modifier.width(8.dp)) }
        if (isDownloaded) {
            Icon(Icons.Filled.Check, null, tint = Color(0xFF80CBC4), modifier = Modifier.size(14.dp))
        } else {
            IconButton(onDownload, Modifier.size(28.dp)) {
                Icon(Icons.Filled.Download, null, tint = Color.White.copy(alpha = 0.35f), modifier = Modifier.size(13.dp))
            }
        }
        Text(track.duration, color = Color.White.copy(alpha = 0.35f), fontSize = 11.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
    }
}

@Composable
fun PlayingIndicatorDots(isPlaying: Boolean = true) {
    val inf = rememberInfiniteTransition(label = "dots")
    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        listOf(0, 150, 300).forEach { d ->
            val h by inf.animateFloat(
                initialValue = 4f,
                targetValue = if (isPlaying) 14f else 4f,
                animationSpec = if (isPlaying) {
                    infiniteRepeatable(
                        tween(500, delayMillis = d, easing = FastOutSlowInEasing),
                        repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
                    )
                } else {
                    infiniteRepeatable(tween(1))
                },
                label = "d$d"
            )
            Box(
                Modifier
                    .width(3.dp)
                    .height(h.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFFE8D5FF))
            )
        }
    }
}


@Composable
private fun AlbumScreen(
    album: Album,
    contentPadding: PaddingValues,
    bottomPadding: Dp,
    currentTrackId: Int?,
    isPlaying: Boolean,
    likedTracks: Set<Int>,
    onTrackClick: (Track) -> Unit,
    onToggleLike: (Int) -> Unit,
    onDownloadTrack: (Track) -> Unit,
    onOpenEqualizer: () -> Unit
) {
    val listState = rememberLazyListState()

    LazyColumn(
        state = listState,
        modifier = Modifier
            .padding(contentPadding)
            .fillMaxSize(),
        contentPadding = PaddingValues(
            start = 14.dp,
            end = 14.dp,
            top = 10.dp,
            bottom = bottomPadding
        )
    ) {
        item(key = "hdr") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AsyncImage(
                    model = album.coverUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(300.dp)
                        .shadow(28.dp, RoundedCornerShape(28.dp))
                        .clip(RoundedCornerShape(28.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    text = album.title,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 28.sp,
                    letterSpacing = (-0.5).sp,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    YearBadge(album.year)
                    Text(
                        text = "${album.tracks.size} треков",
                        color = Color.White.copy(alpha = 0.50f),
                        fontSize = 13.sp
                    )
                }

                Spacer(Modifier.height(14.dp))
            }
        }

        items(
            items = album.tracks,
            key = { it.id }
        ) { track ->
            AlbumTrackRow(
                track = track,
                isActive = currentTrackId == track.id,
                isPlaying = isPlaying,
                isLiked = track.id in likedTracks,
                isDownloaded = TrackDownloadManager.isDownloaded(track.id),
                onClick = { onTrackClick(track) },
                onToggleLike = { onToggleLike(track.id) },
                onDownload = { onDownloadTrack(track) }
            )
        }

        if (album.id == 9 || album.id == 10) {
            item(key = "lyrics_cta") {
                Text(
                    text = "Нет текста песни? Помогите проекту и создайте синхронизированный текст, обращайтесь за инструкциями в ТГ @videlsvet",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, end = 4.dp, top = 20.dp, bottom = 8.dp),
                    color = Color.White.copy(alpha = 0.45f),
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun AlbumTrackRow(track: Track, isActive: Boolean, isPlaying: Boolean, isLiked: Boolean, isDownloaded: Boolean, onClick: () -> Unit, onToggleLike: () -> Unit, onDownload: () -> Unit) {
    val bg by animateFloatAsState(if (isActive) 0.13f else 0f, tween(200), label = "ab")
    val br by animateFloatAsState(if (isActive) 0.20f else 0.07f, tween(200), label = "abb")
    Row(Modifier.fillMaxWidth().padding(vertical = 5.dp).clip(RoundedCornerShape(16.dp)).background(Color.White.copy(alpha = bg)).border(1.dp, Color.White.copy(alpha = br), RoundedCornerShape(16.dp)).clickable { onClick() }.padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.width(24.dp), contentAlignment = Alignment.Center) {
            if (isActive) PlayingIndicatorDots(isPlaying = isPlaying)
            else Text("${track.id % 100}", color = Color.White.copy(alpha = 0.30f), fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(track.title, color = if (isActive) Color.White else Color.White.copy(alpha = 0.90f), fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal, fontSize = 14.sp)
            if (track.featArtist != null) Text("ft. ${track.featArtist}", color = Color.White.copy(alpha = 0.40f), fontSize = 11.sp)
        }
        Spacer(Modifier.width(8.dp))
        if (isDownloaded) {
            Icon(
                Icons.Filled.Check,
                null,
                tint = Color(0xFF80CBC4),
                modifier = Modifier.size(18.dp)
            )
        } else {
            IconButton(onDownload, Modifier.size(32.dp)) {
                Icon(
                    Icons.Filled.Download,
                    null,
                    tint = Color.White.copy(alpha = 0.50f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        Spacer(Modifier.width(4.dp))
        IconButton(onToggleLike, Modifier.size(32.dp)) {
            val ha by animateFloatAsState(if (isLiked) 1f else 0.35f, spring(stiffness = Spring.StiffnessMediumLow), label = "ha")
            Icon(if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder, null, tint = if (isLiked) Color(0xFFFF6B8A) else Color.White.copy(alpha = ha), modifier = Modifier.size(16.dp))
        }
        Text(track.duration, color = Color.White.copy(alpha = 0.40f), fontSize = 12.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
    }
}

@Composable
private fun MiniPlayerBar(
    track: Track, coverUrl: String?, isPlaying: Boolean, positionMs: Long, durationMs: Long,
    accentColor: Color, isLiked: Boolean, onPlayPause: () -> Unit, onExpand: () -> Unit,
    prevTrack: Track?, nextTrack: Track?, prevCoverUrl: String?, nextCoverUrl: String?,
    onPrevious: () -> Unit, onNext: () -> Unit, onToggleLike: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val prog = if (durationMs > 0) positionMs.toFloat() / durationMs else 0f
    val animProg by animateFloatAsState(prog, tween(500, easing = LinearEasing), label = "mp")
    val off = remember { Animatable(0f) }
    var sw by remember { mutableStateOf(false) }

    BoxWithConstraints(modifier.fillMaxWidth()) {
        val den = LocalDensity.current
        val peek = 10.dp;
        val gap = 10.dp
        val cw = maxWidth - (peek * 2)
        val nsPx = with(den) { (cw.toPx() + gap.toPx()).coerceAtLeast(1f) }
        val prevX = -nsPx;
        val curX = 0f;
        val nextX = +nsPx
        val dThr = nsPx * 0.22f;
        val vThr = 1050f;
        val maxD = nsPx * 0.65f

        fun x(b: Float) = b + off.value
        fun resist(d: Float): Float {
            val nv =
                off.value + d; return if ((prevTrack == null && nv > 0f) || (nextTrack == null && nv < 0f)) d * 0.25f else d
        }

        val ds = rememberDraggableState { raw ->
            if (sw) return@rememberDraggableState; scope.launch {
            off.snapTo((off.value + resist(raw)).coerceIn(-maxD, maxD))
        }
        }

        suspend fun goNext() {
            if (sw || nextTrack == null) return; sw = true; off.animateTo(
                -nextX,
                tween(210, easing = FastOutSlowInEasing)
            ); onNext(); off.snapTo(0f); sw = false
        }

        suspend fun goPrev() {
            if (sw || prevTrack == null) return; sw = true; off.animateTo(
                -prevX,
                tween(210, easing = FastOutSlowInEasing)
            ); onPrevious(); off.snapTo(0f); sw = false
        }

        val shape = RoundedCornerShape(22.dp)
        val safeE = { if (!sw) onExpand() }
        val safeP = { if (!sw) onPlayPause() }

        @Composable
        fun MiniCard(t: Track, url: String?, active: Boolean, bx: Float, onClick: () -> Unit) {
            val bg = accentColor.darken(0.10f)
            Box(
                Modifier.width(cw).height(68.dp).offset { IntOffset(x(bx).roundToInt(), 0) }
                    .shadow(18.dp, shape).clip(shape).background(bg).let { m ->
                        if (active) m.drawBehind {
                            drawRect(
                                Color.White.copy(alpha = 0.08f),
                                size = Size(size.width * animProg, size.height)
                            )
                        } else m
                    }.border(1.dp, Color.White.copy(alpha = 0.12f), shape)
                    .clickable(remember { MutableInteractionSource() }, null) { onClick() }
                    .padding(horizontal = 12.dp)
            ) {
                Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = url,
                            contentDescription = null,
                        modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            t.title,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            fontSize = 13.sp
                        )
                        Text(
                            t.displayArtist,
                            color = Color.White.copy(alpha = 0.55f),
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (active) {
                        Spacer(Modifier.width(4.dp))
                        IconButton(
                            onClick = { if (!sw) onToggleLike() },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                null,
                                tint = if (isLiked) Color(0xFFFF6B8A) else Color.White.copy(alpha = 0.45f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(Modifier.width(2.dp))
                        Box(
                            Modifier.size(40.dp).clip(CircleShape).background(Color.White)
                                .clickable(remember { MutableInteractionSource() }, null) { safeP() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                null,
                                tint = Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .draggable(
                    state = ds,
                    orientation = Orientation.Horizontal,
                    onDragStopped = { vel ->
                        if (sw) return@draggable
                        val xn = off.value
                        scope.launch {
                            when {
                                ((xn <= -dThr) || (vel < -vThr)) && nextTrack != null -> goNext()
                                ((xn >= dThr) || (vel > vThr)) && prevTrack != null -> goPrev()
                                else -> off.animateTo(0f, spring(0.88f, 320f))
                            }
                        }
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (prevTrack != null) {
                MiniCard(prevTrack, prevCoverUrl ?: coverUrl, false, prevX) { scope.launch { goPrev() } }
            }
            if (nextTrack != null) {
                MiniCard(nextTrack, nextCoverUrl ?: coverUrl, false, nextX) { scope.launch { goNext() } }
            }
            MiniCard(track, coverUrl, true, curX, safeE)
        }
    }
}

@Composable
private fun FullPlayerScreen(
    accent: Color,
    track: Track,
    album: Album,
    isPlaying: Boolean,
    isShuffled: Boolean,
    isRepeating: Boolean,
    isLiked: Boolean,
    positionMs: Long,
    durationMs: Long,
    prev: Track?,
    next: Track?,
    prevCoverUrl: String?,
    nextCoverUrl: String?,
    onClose: () -> Unit,
    onPlayPause: () -> Unit,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    onToggleLike: (Int) -> Unit,
    onOpenAlbum: (Album) -> Unit,
    onOpenEqualizer: () -> Unit,
    lyricsVm: LyricsViewModel = viewModel(factory = LyricsViewModel.Factory)
) {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val swipeY = remember { Animatable(0f) }
    val closeThr = with(density) { 120.dp.toPx() }
    val maxSwipe = with(density) { 280.dp.toPx() }

    val swipeDrag = rememberDraggableState { delta ->
        scope.launch { swipeY.snapTo((swipeY.value + delta).coerceIn(0f, maxSwipe)) }
    }

    val screenAlpha = (1f - (swipeY.value / maxSwipe) * 0.6f).coerceIn(0.3f, 1f)
    val baseA = accent.darken(0.08f)
    val bgBrush = Brush.verticalGradient(
        0.0f to baseA,
        0.55f to baseA.darken(0.30f),
        1.0f to Color(0xFF040406)
    )

    var isDragging by remember { mutableStateOf(false) }
    var dragProg by remember { mutableFloatStateOf(0f) }
    val realProg = if (durationMs > 0) (positionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f
    val displayProg = if (isDragging) dragProg else realProg

    val coverScale by animateFloatAsState(
        if (isPlaying) 1f else 0.92f,
        spring(dampingRatio = 0.6f, stiffness = 100f),
        label = "coverScale"
    )
    val coverElevation by animateFloatAsState(
        if (isPlaying) 1f else 0.6f,
        tween(400),
        label = "coverElevation"
    )

    var showLyrics by rememberSaveable { mutableStateOf(false) }
    val latestPositionMs by rememberUpdatedState(positionMs)
    val ctx = LocalContext.current

    LaunchedEffect(track.id) {
        lyricsVm.load(ctx, track.id)
        if (showLyrics) lyricsVm.updatePosition(latestPositionMs)
    }

    LaunchedEffect(showLyrics) {
        if (showLyrics) {
            lyricsVm.updatePosition(latestPositionMs)
            lyricsVm.startPositionTicker { latestPositionMs }
        } else {
            lyricsVm.stopPositionTicker()
        }
    }

    DisposableEffect(Unit) {
        onDispose { lyricsVm.stopPositionTicker() }
    }

    val lyricsState by lyricsVm.state.collectAsState()

    val controlsLift = 100.dp
    val headerToCoverGap = 50.dp

    // Офсет карусели, сбрасывается при смене трека
    val carouselOff = remember(track.id) { Animatable(0f) }
    var carouselSwiping by remember(track.id) { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .offset { IntOffset(0, swipeY.value.roundToInt()) }
            .draggable(
                state = swipeDrag,
                orientation = Orientation.Vertical,
                onDragStopped = { vel ->
                    scope.launch {
                        if (swipeY.value > closeThr || vel > 1000f) {
                            swipeY.animateTo(maxSwipe, tween(160, easing = FastOutSlowInEasing))
                            onClose()
                        } else {
                            swipeY.animateTo(0f, spring(0.72f, 260f))
                        }
                    }
                }
            )
            .background(bgBrush)
            .graphicsLayer { alpha = screenAlpha }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(Modifier.height(12.dp))

                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onClose) {
                        Icon(
                            Icons.Filled.KeyboardArrowDown,
                            null,
                            tint = Color.White.copy(alpha = 0.7f)
                        )
                    }
                    Column(
                        Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "СЕЙЧАС ИГРАЕТ",
                            color = Color.White.copy(alpha = 0.35f),
                            fontSize = 10.sp,
                            letterSpacing = 2.sp
                        )
                        Text(
                            album.title,
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(onClick = { onToggleLike(track.id) }) {
                        Icon(
                            if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            null,
                            tint = if (isLiked) Color(0xFFFF6B8A) else Color.White.copy(alpha = 0.6f)
                        )
                    }
                }

                Spacer(Modifier.height(headerToCoverGap))

                AnimatedContent(
                    targetState = showLyrics,
                    label = "coverLyricsSwitch",
                    transitionSpec = {
                        (fadeIn(tween(220)) + slideInVertically(tween(260)) { it / 12 }) togetherWith
                                (fadeOut(tween(160)) + slideOutVertically(tween(160)) { -it / 14 })
                    }
                ) { lyricsOn ->
                    if (!lyricsOn) {
                        // peek по 36dp с каждой стороны — видны края соседних обложек
                        val peek = 36.dp
                        val gap = 14.dp

                        BoxWithConstraints(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = peek)
                        ) {
                            val cardW = maxWidth
                            val stepPx = with(density) { (cardW + gap).toPx() }
                            val maxDrag = stepPx * 0.72f

                            fun resist(d: Float): Float {
                                val nv = carouselOff.value + d
                                return when {
                                    nv < 0f && next == null -> d * 0.12f
                                    nv > 0f && prev == null -> d * 0.12f
                                    else -> d
                                }
                            }

                            val dragState = rememberDraggableState { raw ->
                                if (carouselSwiping) return@rememberDraggableState
                                scope.launch {
                                    carouselOff.snapTo(
                                        (carouselOff.value + resist(raw)).coerceIn(
                                            -maxDrag,
                                            maxDrag
                                        )
                                    )
                                }
                            }

                            suspend fun goNext() {
                                if (carouselSwiping || next == null) return
                                carouselSwiping = true
                                carouselOff.animateTo(
                                    -stepPx,
                                    tween(320, easing = FastOutSlowInEasing)
                                )
                                onNext()
                                // snapTo сбросится автоматически через remember(track.id)
                                carouselSwiping = false
                            }

                            suspend fun goPrev() {
                                if (carouselSwiping || prev == null) return
                                carouselSwiping = true
                                carouselOff.animateTo(
                                    stepPx,
                                    tween(320, easing = FastOutSlowInEasing)
                                )
                                onPrev()
                                carouselSwiping = false
                            }

                            @Composable
                            fun CoverCard(coverUrl: String?, slotX: Float) {
                                val totalOff = slotX + carouselOff.value
                                val normDist = (totalOff / stepPx).coerceIn(-1f, 1f)
                                val absNorm = kotlin.math.abs(normDist)

                                // Текущая карточка — слотX == 0
                                val isCurrent = slotX == 0f
                                val cardScale = if (isCurrent) {
                                    (coverScale - absNorm * 0.08f).coerceAtLeast(0.84f)
                                } else {
                                    (0.84f + (1f - absNorm) * 0.08f).coerceIn(0.84f, coverScale)
                                }
                                val cardAlpha = if (isCurrent) {
                                    (1f - absNorm * 0.45f).coerceIn(0.45f, 1f)
                                } else {
                                    (0.45f + (1f - absNorm) * 0.45f).coerceIn(0.45f, 1f)
                                }
                                val elev = if (isCurrent) {
                                    (40f * coverElevation * (1f - absNorm * 0.7f)).coerceAtLeast(4f)
                                } else {
                                    4f + (1f - absNorm) * 20f
                                }

                                Box(
                                    modifier = Modifier
                                        .width(cardW)
                                        .aspectRatio(1f)
                                        .offset { IntOffset(x = totalOff.roundToInt(), y = 0) }
                                        .graphicsLayer {
                                            scaleX = cardScale
                                            scaleY = cardScale
                                            alpha = cardAlpha
                                            shadowElevation = elev
                                            shape = RoundedCornerShape(20.dp)
                                            clip = true
                                        }
                                ) {
                                    AsyncImage(
                                        model = coverUrl,
                                                            contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .width(cardW)
                                    .aspectRatio(1f)
                                    .draggable(
                                        state = dragState,
                                        orientation = Orientation.Horizontal,
                                        onDragStopped = { vel ->
                                            if (carouselSwiping) return@draggable
                                            scope.launch {
                                                when {
                                                    (carouselOff.value < -stepPx * 0.22f || vel < -700f) && next != null -> goNext()
                                                    (carouselOff.value > stepPx * 0.22f || vel > 700f) && prev != null -> goPrev()
                                                    else -> carouselOff.animateTo(
                                                        0f,
                                                        spring(
                                                            dampingRatio = 0.78f,
                                                            stiffness = 300f
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                // Соседние рендерятся первыми (под текущей)
                                prev?.let {
                                    CoverCard(prevCoverUrl ?: album.coverUrl, -stepPx)
                                }
                                next?.let {
                                    CoverCard(nextCoverUrl ?: album.coverUrl, stepPx)
                                }
                                // Текущая — поверх
                                CoverCard(album.coverUrl, 0f)
                            }
                        }
                    } else {
                        LyricsView(
                            viewModel = lyricsVm,
                            modifier = Modifier.fillMaxSize(),
                            onLineClick = { ms -> onSeekTo(ms) }
                        )
                    }
                }
            }
            if (!showLyrics) {
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = track.title,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) {
                                    onClose()
                                    onOpenAlbum(album)
                                }
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            track.displayArtist,
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 14.sp
                        )
                    }
                    LyricsToggleButton(
                        isActive = showLyrics,
                        accentColor = accent,
                        lyricsState = lyricsState,
                        onClick = {
                            val nextLyrics = !showLyrics
                            if (nextLyrics) lyricsVm.updatePosition(positionMs)
                            showLyrics = nextLyrics
                        }
                    )
                }
            } else {
                Spacer(Modifier.height(10.dp))
            }

            if (!showLyrics) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = controlsLift)
                ) {
                    MinimalProgressBar(
                        progress = displayProg,
                        accentColor = accent,
                        modifier = Modifier.fillMaxWidth(),
                        onDragStart = { isDragging = true; dragProg = it },
                        onDragEnd = {
                            onSeekTo((it * max(1L, durationMs)).toLong()); isDragging = false
                        },
                        onTapSeek = {
                            onSeekTo((it * max(1L, durationMs)).toLong()); isDragging = false
                        }
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(formatMs(positionMs), color = Color.White.copy(alpha = 0.4f))
                        Text(formatMs(durationMs), color = Color.White.copy(alpha = 0.4f))
                    }
                    Spacer(Modifier.height(16.dp))
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onToggleShuffle) {
                            Icon(
                                Icons.Filled.Shuffle,
                                null,
                                tint = if (isShuffled) Color(0xFFD4BCFF) else Color.White.copy(alpha = 0.5f)
                            )
                        }
                        IconButton(onClick = { if (prev != null) onPrev() }) {
                            Icon(
                                Icons.Filled.SkipPrevious,
                                null,
                                tint = if (prev != null) Color.White.copy(alpha = 0.9f) else Color.White.copy(
                                    alpha = 0.25f
                                )
                            )
                        }
                        MainPlayButton(isPlaying = isPlaying, onClick = onPlayPause)
                        IconButton(onClick = { if (next != null) onNext() }) {
                            Icon(
                                Icons.Filled.SkipNext,
                                null,
                                tint = if (next != null) Color.White.copy(alpha = 0.9f) else Color.White.copy(
                                    alpha = 0.25f
                                )
                            )
                        }
                        IconButton(onClick = onToggleRepeat) {
                            Icon(
                                Icons.Filled.Repeat,
                                null,
                                tint = if (isRepeating) Color(0xFFD4BCFF) else Color.White.copy(
                                    alpha = 0.5f
                                )
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Box(
                            Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(Color.White.copy(alpha = 0.08f))
                                .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(999.dp))
                                .clickable { onOpenEqualizer() }
                                .padding(horizontal = 14.dp, vertical = 7.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Filled.FormatColorText, null, tint = Color.White.copy(alpha = 0.65f), modifier = Modifier.size(14.dp))
                                Text("EQ", color = Color.White.copy(alpha = 0.75f), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 22.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    LyricsToggleButton(
                        isActive = showLyrics,
                        accentColor = accent,
                        lyricsState = lyricsState,
                        onClick = { showLyrics = false }
                    )
                }
            }
        }
    }
}


    @Composable
    private fun LyricsToggleButton(
        isActive: Boolean,
        accentColor: Color,
        lyricsState: LyricsViewModel.State,
        onClick: () -> Unit
    ) {
        val isEnabled = lyricsState !is LyricsViewModel.State.NotFound

        val bgAlpha by animateFloatAsState(
            targetValue = if (isActive) 0.22f else 0.10f,
            animationSpec = tween(200),
            label = "lyrBg"
        )
        val iconAlpha by animateFloatAsState(
            targetValue = if (isActive) 1f else 0.45f,
            animationSpec = tween(200),
            label = "lyrIcon"
        )
        val scale by animateFloatAsState(
            targetValue = if (isActive) 1f else 0.93f,
            animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f),
            label = "lyrScale"
        )

        Box(
            modifier = Modifier
                .scale(scale)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    if (isActive) accentColor.copy(alpha = bgAlpha)
                    else Color.White.copy(alpha = bgAlpha)
                )
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    enabled = isEnabled
                ) { onClick() }
                .padding(horizontal = 12.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.MusicNote,
                    contentDescription = "Текст",
                    tint = if (isActive) accentColor else Color.White.copy(alpha = iconAlpha),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = when (lyricsState) {
                        is LyricsViewModel.State.Loading -> "…"
                        is LyricsViewModel.State.NotFound -> "Нет текста"
                        else -> "Текст"
                    },
                    color = if (isActive) accentColor else Color.White.copy(alpha = iconAlpha),
                    fontSize = 13.sp,
                    fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }

    @Composable
    private fun MinimalProgressBar(
        progress: Float,
        accentColor: Color,
        modifier: Modifier = Modifier,
        onDragStart: (Float) -> Unit,
        onDragEnd: (Float) -> Unit,
        onTapSeek: (Float) -> Unit
    ) {
        var currentProgress by remember { mutableFloatStateOf(progress) }
        var dragging by remember { mutableStateOf(false) }

        LaunchedEffect(progress) { if (!dragging) currentProgress = progress }

        val trackColor = Color.White.copy(alpha = 0.14f)
        val fillColor = Color.White
        val thumbColor = Color.White
        val glowColor = accentColor.copy(alpha = 0.5f)

        val thumbRadius by animateFloatAsState(
            if (dragging) 9f else 6f,
            spring(0.5f, Spring.StiffnessMedium),
            label = "thr"
        )
        val trackH by animateFloatAsState(
            if (dragging) 4f else 3f,
            spring(0.5f, Spring.StiffnessMedium),
            label = "th"
        )

        BoxWithConstraints(
            modifier
                .height(34.dp)
        ) {
            val widthPx = with(LocalDensity.current) { maxWidth.toPx().coerceAtLeast(1f) }

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(widthPx) {
                        detectTapGestures { pos ->
                            val p = (pos.x / widthPx).coerceIn(0f, 1f)
                            currentProgress = p
                            dragging = false
                            onTapSeek(p)
                        }
                    }
                    .draggable(
                        state = rememberDraggableState { delta ->
                            val newProg = (currentProgress + delta / widthPx).coerceIn(0f, 1f)
                            currentProgress = newProg
                            onDragStart(newProg)
                        },
                        orientation = Orientation.Horizontal,
                        onDragStarted = { offset ->
                            val newProg = (offset.x / widthPx).coerceIn(0f, 1f)
                            currentProgress = newProg
                            dragging = true
                            onDragStart(newProg)
                        },
                        onDragStopped = {
                            dragging = false
                            onDragEnd(currentProgress)
                        }
                    )
            ) {
                val cy = size.height / 2f
                val halfH = trackH / 2f

                drawRoundRect(
                    color = trackColor,
                    topLeft = Offset(0f, cy - halfH),
                    size = Size(size.width, trackH),
                    cornerRadius = CornerRadius(trackH / 2f)
                )

                val fillW = size.width * currentProgress
                if (fillW > 0f) {
                    drawRoundRect(
                        color = fillColor,
                        topLeft = Offset(0f, cy - halfH),
                        size = Size(fillW, trackH),
                        cornerRadius = CornerRadius(trackH / 2f)
                    )
                }

                if (dragging) {
                    drawCircle(
                        color = glowColor,
                        radius = thumbRadius * 2.8f,
                        center = Offset(fillW, cy)
                    )
                }

                drawCircle(
                    color = thumbColor,
                    radius = thumbRadius,
                    center = Offset(fillW, cy)
                )
            }
        }
    }


    @Composable
    private fun MainPlayButton(isPlaying: Boolean, onClick: () -> Unit) {
        val sc by animateFloatAsState(
            if (isPlaying) 1f else 0.93f,
            spring(0.55f, Spring.StiffnessMedium),
            label = "psc"
        )
        Box(
            Modifier
                .size(62.dp)
                .scale(sc)
                .shadow(
                    if (isPlaying) 28.dp else 12.dp,
                    CircleShape,
                    ambientColor = Color.White.copy(alpha = 0.15f)
                )
                .clip(CircleShape)
                .background(Color.White)
                .clickable(remember { MutableInteractionSource() }, null) { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(30.dp)
            )
        }
    }


    @Composable
    private fun GhostIconButton(
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        isActive: Boolean = false,
        enabled: Boolean = true,
        activeColor: Color = Color(0xFFD4BCFF),
        content: @Composable () -> Unit
    ) {
        val sc by animateFloatAsState(if (enabled) 1f else 0.8f, tween(150), label = "gs")
        val dotAlpha by animateFloatAsState(if (isActive) 1f else 0f, tween(200), label = "gd")

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
        ) {
            Box(
                Modifier
                    .size(44.dp)
                    .scale(sc)
                    .clip(CircleShape)
                    .clickable(
                        remember { MutableInteractionSource() },
                        null,
                        enabled = enabled
                    ) { onClick() },
                contentAlignment = Alignment.Center
            ) { content() }

            Box(
                Modifier
                    .size(4.dp)
                    .offset(y = (-2).dp)
                    .clip(CircleShape)
                    .background(activeColor.copy(alpha = dotAlpha * 0.75f))
            )
        }
    }

    @Composable
    fun WelcomeScreen(onDismiss: () -> Unit) {
        val alpha = remember { Animatable(0f) }
        val slideY = remember { Animatable(40f) }

        LaunchedEffect(Unit) {
            launch { alpha.animateTo(1f, tween(600, easing = FastOutSlowInEasing)) }
            launch { slideY.animateTo(0f, tween(600, easing = FastOutSlowInEasing)) }
        }

        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer { this.alpha = alpha.value }
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to Color(0xFF1A1A1F),
                            0.5f to Color(0xFF111114),
                            1.0f to Color(0xFF080809)
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
                    .offset(y = slideY.value.dp)
                    .graphicsLayer { this.alpha = alpha.value },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.weight(0.12f))

                val context = LocalContext.current
                val icon = remember {
                    val drawable = context.packageManager.getApplicationIcon(context.packageName)
                    val bitmap = android.graphics.Bitmap.createBitmap(
                        drawable.intrinsicWidth, drawable.intrinsicHeight,
                        android.graphics.Bitmap.Config.ARGB_8888
                    )
                    val canvas = android.graphics.Canvas(bitmap)
                    drawable.setBounds(0, 0, canvas.width, canvas.height)
                    drawable.draw(canvas)
                    bitmap.asImageBitmap()
                }

                Image(
                    bitmap = icon,
                    contentDescription = null,
                    modifier = Modifier
                        .size(88.dp)
                        .clip(RoundedCornerShape(22.dp))
                )

                Spacer(Modifier.height(24.dp))

                Text(
                    "LifonMUSIC",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 34.sp,
                    letterSpacing = (-1).sp
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    "by videlsvet",
                    color = Color(0xFFE8D5FF).copy(alpha = 0.55f),
                    fontSize = 13.sp,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(Modifier.height(36.dp))

                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White.copy(alpha = 0.055f))
                        .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(24.dp))
                        .padding(22.dp)
                ) {
                    Column {
                        Text(
                            "О проекте",
                            color = Color(0xFFE8D5FF).copy(alpha = 0.55f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(
                            "LifonMUSIC — неофициальное фан-приложение для слушателей CUPSIZE. Здесь собрана вся дискография группы, тексты песен и возможность создать свой плейлист из избранных треков.",
                            color = Color.White.copy(alpha = 0.78f),
                            fontSize = 15.sp,
                            lineHeight = 23.sp
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFFFF6B8A).copy(alpha = 0.07f))
                        .border(
                            1.dp,
                            Color(0xFFFF6B8A).copy(alpha = 0.18f),
                            RoundedCornerShape(24.dp)
                        )
                        .padding(18.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("⚠️", fontSize = 16.sp, modifier = Modifier.padding(top = 2.dp))
                        Text(
                            "LifonMUSIC это независимый проект, не связанный с официальными представителями CUPSIZE. Все используемые материалы принадлежат группе CUPSIZE!",
                            color = Color.White.copy(alpha = 0.65f),
                            fontSize = 13.sp,
                            lineHeight = 20.sp
                        )
                    }
                }

                Spacer(Modifier.weight(1f))

                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .clickable { onDismiss() }
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Начать слушать",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        letterSpacing = (-0.2).sp
                    )
                }

                Spacer(Modifier.height(20.dp))
            }
        }
    }

    private fun formatMs(ms: Long): String {
        if (ms <= 0L) return "0:00"
        val s = ms / 1000L; return "${s / 60}:${(s % 60).let { if (it < 10) "0$it" else "$it" }}"
    }

    private suspend fun computeDominantColorFromUrl(
        context: android.content.Context,
        url: String?
    ): Color =
        withContext(Dispatchers.IO) {
            if (url.isNullOrBlank()) return@withContext Color(0xFF111111)
            try {
                val request = ImageRequest.Builder(context)
                    .data(url)
                    .allowHardware(false)
                    .build()
                val result = ImageLoader(context).execute(request)
                val bitmap = (result.drawable as? BitmapDrawable)?.bitmap
                    ?: return@withContext Color(0xFF111111)
                withContext(Dispatchers.Default) {
                    Color(
                        Palette.Builder(bitmap).generate().getDominantColor(0xFF111111.toInt())
                    )
                }
            } catch (_: Exception) {
                Color(0xFF111111)
            }
        }

    private fun Color.darken(a: Float): Color =
        Color(red * (1f - a), green * (1f - a), blue * (1f - a), alpha)

    private fun Color.ensureNotTooBright(): Color {
        val l =
            0.2126 * red + 0.7152 * green + 0.0722 * blue; return if (l > 0.72f) darken(0.38f) else this
    }

    class PlayerEqualizer(private val context: Context) {
        private var equalizer: Equalizer? = null
        private val prefs = context.getSharedPreferences("lifon_prefs", Context.MODE_PRIVATE)

        fun attachToSession(audioSessionId: Int) {
            if (audioSessionId <= 0) return

            release()

            try {
                equalizer = Equalizer(0, audioSessionId).apply {
                    enabled = true
                }
                restoreSavedLevels()
            } catch (_: Exception) {
                equalizer = null
            }
        }

        fun isReady(): Boolean = equalizer != null

        fun getBandCount(): Int = equalizer?.numberOfBands?.toInt() ?: 0

        fun getBandLevelRange(): IntRange {
            val range = equalizer?.bandLevelRange
            return if (range != null && range.size >= 2) {
                range[0].toInt()..range[1].toInt()
            } else {
                -1500..1500
            }
        }

        fun getBandLevel(band: Int): Int {
            return try {
                equalizer?.getBandLevel(band.toShort())?.toInt() ?: 0
            } catch (_: Exception) {
                0
            }
        }

        fun setBandLevel(band: Int, level: Int) {
            try {
                equalizer?.setBandLevel(band.toShort(), level.toShort())
                prefs.edit().putInt("eq_band_$band", level).apply()
            } catch (_: Exception) {
            }
        }

        fun getCenterFreqLabel(band: Int): String {
            return try {
                val freqMilliHz = equalizer?.getCenterFreq(band.toShort()) ?: 0
                val hz = freqMilliHz / 1000
                when {
                    hz >= 1000 -> {
                        val khz = hz / 1000f
                        if (khz % 1f == 0f) "${khz.toInt()} kHz" else String.format("%.1f kHz", khz)
                    }

                    else -> "$hz Hz"
                }
            } catch (_: Exception) {
                "Band ${band + 1}"
            }
        }

        fun resetAll() {
            val range = getBandLevelRange()
            val neutral = 0.coerceIn(range.first, range.last)
            for (i in 0 until getBandCount()) {
                setBandLevel(i, neutral)
            }
        }

        private fun restoreSavedLevels() {
            val range = getBandLevelRange()
            for (i in 0 until getBandCount()) {
                val saved = prefs.getInt("eq_band_$i", 0).coerceIn(range.first, range.last)
                try {
                    equalizer?.setBandLevel(i.toShort(), saved.toShort())
                } catch (_: Exception) {
                }
            }
        }

        fun release() {
            try {
                equalizer?.release()
            } catch (_: Exception) {
            }
            equalizer = null
        }
    }

