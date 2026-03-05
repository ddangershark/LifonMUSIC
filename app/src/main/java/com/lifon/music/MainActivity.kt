package com.lifon.music

import android.graphics.BitmapFactory
import android.os.Bundle
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lifon.music.lyrics.LyricsViewModel
import androidx.compose.ui.graphics.asImageBitmap
import com.lifon.music.lyrics.LyricsView
import androidx.compose.foundation.lazy.LazyRow
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import android.net.Uri
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.ui.PlayerView
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.media3.common.MediaMetadata
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import android.content.Intent
import android.os.Build
import androidx.media3.common.MediaItem
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.palette.graphics.Palette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.compose.material.icons.filled.FormatColorText
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.runtime.rememberUpdatedState
import java.net.URL
import kotlin.math.max
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    private var player: ExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                1001
            )
        }

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

data class Album(val id: Int, val title: String, val year: String, val coverRes: Int, val tracks: List<Track>)
data class Track(
    val id: Int, val title: String, val duration: String, val audioRes: Int,
    val albumId: Int, val artist: String = "CUPSIZE", val featArtist: String? = null
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
    val prefs = context.getSharedPreferences("lifon_prefs", Context.MODE_PRIVATE)
    var showDisclaimer by remember { mutableStateOf(!prefs.getBoolean("disclaimer_shown", false)) }
    var showDiscographyRoadmap by remember { mutableStateOf(false) }

    val albums = remember {
        listOf(
            Album(
                1, "Еби меня, малышка", "2023", R.drawable.album_1, listOf(
                    Track(1, "ДАВАЙ ТРАХАТЬСЯ В МАШИНЕ", "1:55", R.raw.fuck_1, 1),
                    Track(2, "Люби меня, алина", "1:37", R.raw.fuck_2, 1),
                    Track(3, "ГИДРОПОН", "2:24", R.raw.fuck_3, 1),
                    Track(4, "ПАПИК", "1:58", R.raw.fuck_4, 1, featArtist = "17 SEVENTEEN"),
                    Track(5, "лиза,настя", "1:47", R.raw.fuck_5, 1),
                    Track(6, "вайфуу", "2:10", R.raw.fuck_6, 1),
                    Track(7, "я схавал опиат", "2:18", R.raw.fuck_7, 1),
                    Track(8, "Вирус", "2:29", R.raw.fuck_8, 1),
                    Track(9, "МОЯ МАМА ПЬЁТ", "2:09", R.raw.fuck_9, 1),
                    Track(10, "Ты любишь травку", "2:13", R.raw.fuck_10, 1),
                    Track(11, "Забуду", "3:03", R.raw.fuck_11, 1),
                    Track(12, "Мне похуй", "1:53", R.raw.fuck_12, 1),
                )
            ),
            Album(
                2, "дели на два", "2023", R.drawable.album_2, listOf(
                    Track(101, "ты любишь танцевать", "2:24", R.raw.album2_track1, 2),
                    Track(102, "пятый элемент", "2:14", R.raw.album2_track2, 2),
                    Track(103, "целую тебя", "2:09", R.raw.album2_track3, 2),
                    Track(104, "воздух", "2:04", R.raw.album2_track4, 2),
                )
            ),
            Album(
                3, "Как испортить вечеринку?", "2023", R.drawable.album_3, listOf(
                    Track(201, "Юра, Юра", "2:08", R.raw.album3_track1, 3),
                    Track(202, "По улице иду я", "2:32", R.raw.album3_track2, 3),
                    Track(
                        203,
                        "Они все дрочат на тебя в интернете",
                        "1:48",
                        R.raw.album3_track3,
                        3
                    ),
                    Track(204, "Стенки моего подъезда", "2:29", R.raw.album3_track4, 3),
                    Track(205, "Василий", "2:26", R.raw.album3_track5, 3),
                    Track(206, "Травматика", "2:40", R.raw.album3_track6, 3),
                    Track(207, "И это прекрасно", "3:25", R.raw.album3_track7, 3),
                    Track(208, "Клей", "2:25", R.raw.album3_track8, 3),
                    Track(209, "Целовались", "2:32", R.raw.album3_track9, 3),
                    Track(210, "Пьяные", "2:08", R.raw.album3_track10, 3),
                    Track(211, "Высокий градус", "2:33", R.raw.album3_track11, 3),
                    Track(212, "Но им не смешно", "2:23", R.raw.album3_track12, 3),
                    Track(213, "Семнадцатилетняя", "2:32", R.raw.album3_track13, 3),
                    Track(214, "Я схожу с ума", "3:07", R.raw.album3_track14, 3),
                    Track(215, "ДПП (Аутро)", "1:48", R.raw.album3_track15, 3),
                )
            ),
            Album(
                4,
                "кажется, в аду прикольно, но меня выгнали б утром",
                "2024",
                R.drawable.album_4,
                listOf(
                    Track(301, "Влечение", "2:11", R.raw.album4_track1, 4),
                    Track(302, "привет, если ты мне не ответишь", "2:03", R.raw.album4_track2, 4),
                    Track(303, "фура", "2:14", R.raw.album4_track3, 4),
                    Track(
                        304,
                        "мой врач думает что у меня шизофрения",
                        "2:14",
                        R.raw.album4_track4,
                        4
                    ),
                    Track(305, "маршрутка", "3:10", R.raw.album4_track5, 4),
                    Track(306, "ну почему", "2:52", R.raw.album4_track6, 4),
                    Track(307, "я тупая, моя жизнь тупая", "3:07", R.raw.album4_track7, 4),
                    Track(308, "пока-пока", "2:53", R.raw.album4_track8, 4),
                    Track(309, "нам это нравится", "2:57", R.raw.album4_track9, 4),
                    Track(310, "больше, чем творчество", "2:34", R.raw.album4_track10, 4),
                )
            ),
            Album(
                5, "в моих легких выросли цветы", "2025", R.drawable.album_5, listOf(
                    Track(401, "107.1", "1:58", R.raw.album5_track1, 5),
                    Track(402, "печаль", "2:10", R.raw.album5_track2, 5),
                    Track(403, "минус,плюс", "3:44", R.raw.album5_track3, 5),
                    Track(404, "переломай мои кости", "2:48", R.raw.album5_track4, 5),
                    Track(405, "давай увидимся", "4:00", R.raw.album5_track5, 5),
                    Track(406, "твои поцелуи", "2:32", R.raw.album5_track6, 5),
                    Track(407, "кислород", "2:22", R.raw.album5_track7, 5),
                    Track(408, "или хотя бы завтра...", "1:45", R.raw.album5_track8, 5),
                    Track(409, "самокрутки", "2:34", R.raw.album5_track9, 5),
                    Track(410, "улыбнись", "4:29", R.raw.album5_track10, 5),
                )
            ),
            Album(
                6, "неуравновешеннолетниепесни pt.1", "2025", R.drawable.album_6, listOf(
                    Track(501, "дьявол!", "3:28", R.raw.album6_track1, 6),
                    Track(502, "оригами", "2:24", R.raw.album6_track2, 6),
                    Track(503, "шАхАшАхА", "2:54", R.raw.album6_track3, 6),
                    Track(504, "песня про спид", "2:50", R.raw.album6_track4, 6),
                    Track(505, "конъюктивит", "3:28", R.raw.album6_track5, 6),
                    Track(506, "тварьтварьтварьтварь...", "3:06", R.raw.album6_track6, 6),
                    Track(507, "злой отчим", "3:45", R.raw.album6_track7, 6),
                )
            ),
            Album(
                7, "прыгайдуравишлист!", "2025", R.drawable.album_7, listOf(
                    Track(601, "прыгай, дура!", "1:59", R.raw.album7_track1, 7),
                    Track(602, "вишлист", "2:07", R.raw.album7_track2, 7),
                )
            ),
            Album(
                8, "Совместные релизы", "2030", R.drawable.album_8, listOf(
                    Track(701, "Сколько мы не спали", "1:51", R.raw.album8_track1, 8, featArtist = "Рэйчи"),
                    Track(702, "1 мая", "2:07", R.raw.album8_track2, 8, featArtist = "madk1d"),
                    Track(703, "Круче чем вы", "1:40", R.raw.album8_track3, 8, featArtist = "madk1d"),
                    Track(704, "Виолетта", "1:45", R.raw.album8_track4, 8, featArtist = "Рэйчи"),
                    Track(705, "Бардак", "1:53", R.raw.album8_track5, 8, featArtist = "17 SEVENTEEN"),
                    Track(706, "ВШБ", "1:49", R.raw.album8_track6, 8, featArtist = "GRILLYAZH"),
                    Track(707, "НЕ ПО СЕБЕ", "3:01", R.raw.album8_track7, 8, featArtist = "источник,Niño"),
                )
            ),
            Album(
                9, "UNRELEASE", "2030", R.drawable.album_9, listOf(
                    Track(801, "Трамадол", "1:22", R.raw.album9_track1, 9),
                    Track(802, "Компромат", "2:78", R.raw.album9_track2, 9),
                    Track(803, "Я стану популярным в интернете", "1:15", R.raw.album9_track3, 9),
                    Track(804, "Я проститутка", "1:45", R.raw.album9_track4, 9),
                    Track(805, "Откуда ты взялась", "3:41", R.raw.album9_track5, 9),
                    Track(806, "тогда мы не были вдвоем", "3:59", R.raw.album9_track6, 9),
                    Track(807, "забываю", "2:03", R.raw.album9_track7, 9),
                )
            ),
        )
    }
    val artworkCache = remember { mutableMapOf<Int, ByteArray>() }
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
    var shuffleNext by remember { mutableStateOf<Track?>(null) }

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
            prefs.edit().putStringSet("liked_tracks", serverLikes.map { it.toString() }.toSet()).apply()
        } else {
            val saved = prefs.getStringSet("liked_tracks", emptySet()) ?: emptySet()
            saved.forEach { s -> s.toIntOrNull()?.let { likedTracks.add(it) } }
        }
    }

    fun toggleLike(id: Int) {
        val token = prefs.getString("auth_token", null)
        if (id in likedTracks) {
            likedTracks.remove(id)
            prefs.edit().putStringSet("liked_tracks", likedTracks.map { it.toString() }.toSet()).apply()
            if (!token.isNullOrBlank() && token != "guest")
                scope.launch { LikesRepository.removeLike(token, id) }
        } else {
            likedTracks.add(id)
            prefs.edit().putStringSet("liked_tracks", likedTracks.map { it.toString() }.toSet()).apply()
            if (!token.isNullOrBlank() && token != "guest")
                scope.launch { LikesRepository.addLike(token, id) }
        }
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

        scope.launch {
            val album = albumForTrack(track)
            val artworkBytes: ByteArray? = withContext(Dispatchers.IO) {
                album?.let { alb ->
                    artworkCache[alb.coverRes] ?: try {
                        val bmp = BitmapFactory.decodeResource(context.resources, alb.coverRes)
                        val stream = java.io.ByteArrayOutputStream()
                        val scaled = android.graphics.Bitmap.createScaledBitmap(bmp, 256, 256, true)
                        bmp.recycle()
                        scaled.compress(android.graphics.Bitmap.CompressFormat.JPEG, 70, stream)
                        scaled.recycle()
                        stream.toByteArray().also { artworkCache[alb.coverRes] = it }
                    } catch (e: Exception) { null }
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
                .setUri("android.resource://com.lifon.music/${track.audioRes}")
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
        prevNext(currentTrack, playContext, getFav()).first?.let {
            playTrack(it, playContext)
        }
    }

    fun playNext() {
        val fav = getFav()
        val pl = getPlaylist(playContext, fav)
        if (pl.isEmpty()) return

        if (isShuffled) {
            val target = shuffleNext ?: pl.filter { it.id != currentTrack?.id }.randomOrNull()
            target?.let { playTrack(it, playContext) }
        } else {
            prevNext(currentTrack, playContext, fav).second?.let {
                playTrack(it, playContext)
            }
        }
    }

    val currentPlayNext by rememberUpdatedState(::playNext)
    val currentPlayPrev by rememberUpdatedState(::playPrev)

    DisposableEffect(Unit) {
        PlayerCallbacks.onNext = { currentPlayNext() }
        PlayerCallbacks.onPrev = { currentPlayPrev() }
        onDispose {
            PlayerCallbacks.onNext = null
            PlayerCallbacks.onPrev = null
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

    val colorCache = remember { mutableStateMapOf<Int, Color>() }
    var dominantColor by remember { mutableStateOf(Color(0xFF0D0D10)) }
    val barTrack = currentTrack
    val barAlbum = albumForTrack(barTrack)
    val favNav = remember(likedTracks.toSet()) {
        albums.flatMap { it.tracks }.filter { it.id in likedTracks }
    }


    LaunchedEffect(currentTrack?.id, isShuffled, playContext) {
        shuffleNext = null
        if (isShuffled) {
            val pl = getPlaylist(playContext, getFav())
            shuffleNext = pl.filter { it.id != currentTrack?.id }.randomOrNull()
        }
    }

    val barPlaylist = remember(playContext, currentTrack?.id, favNav) {
        getPlaylist(playContext, favNav)
    }
    val (barPrevLinear, barNextLinear) = prevNext(barTrack, playContext, favNav)
    val barPrev = barPrevLinear
    val barNext = if (isShuffled) shuffleNext else barNextLinear

    LaunchedEffect(selectedAlbum?.coverRes ?: barAlbum?.coverRes) {
        val c = selectedAlbum?.coverRes ?: barAlbum?.coverRes
        if (c != null) {
            if (!colorCache.containsKey(c)) colorCache[c] =
                computeDominantColorFromRes(context, c).ensureNotTooBright().darken(0.22f)
            dominantColor = colorCache[c] ?: Color(0xFF0D0D10)
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
    LaunchedEffect(barAlbum?.coverRes) {
        val c = barAlbum?.coverRes ?: return@LaunchedEffect
        accentTarget =
            (colorCache[c] ?: computeDominantColorFromRes(context, c)).ensureNotTooBright()
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
                                coverRes = barAlbum.coverRes,
                                isPlaying = isPlaying,
                                positionMs = positionMs,
                                durationMs = durationMs,
                                accentColor = accent,
                                isLiked = barTrack.id in likedTracks,
                                onPlayPause = { togglePP() },
                                onExpand = { showPlayer = true },
                                prevTrack = barPrev,
                                nextTrack = barNext,
                                prevCoverRes = albumForTrack(barPrev)?.coverRes ?: barAlbum.coverRes,
                                nextCoverRes = albumForTrack(barNext)?.coverRes ?: barAlbum.coverRes,
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
                    onToggleLike = { toggleLike(it) }
                )

                currentScreen == AppScreen.FAVORITES -> {
                    val fav = remember(likedTracks.toSet()) {
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
                        onToggleLike = { toggleLike(it) }
                    )
                }

                currentScreen == AppScreen.PROFILE -> {
                    ProfileScreen(
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
                    onToggleLike = { toggleLike(it) }
                )
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
private fun FavoritesScreen(tracks: List<Track>, albums: List<Album>, currentTrack: Track?, isPlaying: Boolean, contentPadding: PaddingValues, bottomPadding: Dp, likedTracks: Set<Int>, onTrackClick: (Track) -> Unit, onToggleLike: (Int) -> Unit) {
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
                FavoriteTrackRow(track = track, coverRes = alb?.coverRes, isActive = currentTrack?.id == track.id, isPlaying = isPlaying, isLiked = track.id in likedTracks, onClick = { onTrackClick(track) }, onToggleLike = { onToggleLike(track.id) })
            }
        }
    }
}

@Composable
private fun FavoriteTrackRow(track: Track, coverRes: Int?, isActive: Boolean, isPlaying: Boolean, isLiked: Boolean, onClick: () -> Unit, onToggleLike: () -> Unit) {
    val bgA by animateFloatAsState(if (isActive) 0.13f else 0f, tween(200), label = "fb")
    val brA by animateFloatAsState(if (isActive) 0.22f else 0.07f, tween(200), label = "fbb")
    Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 5.dp).clip(RoundedCornerShape(16.dp)).background(Color.White.copy(alpha = bgA)).border(1.dp, Color.White.copy(alpha = brA), RoundedCornerShape(16.dp)).clickable { onClick() }.padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(46.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFF2A2A2A)), contentAlignment = Alignment.Center) {
            if (coverRes != null) Image(painterResource(coverRes), null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
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
        IconButton(onClick = onToggleLike, modifier = Modifier.size(32.dp)) {
            Icon(if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder, null, tint = if (isLiked) Color(0xFFFF6B8A) else Color.White.copy(alpha = 0.28f), modifier = Modifier.size(16.dp))
        }
    }
}
@Composable
private fun ProfileScreen(
    contentPadding: PaddingValues,
    bottomPadding: Dp
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("lifon_prefs", Context.MODE_PRIVATE) }

    var username by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(true) }
    var stats by remember { mutableStateOf<StatsResult?>(null) }

    val albums = remember {
        listOf(
            Album(1, "Еби меня, малышка", "2023", R.drawable.album_1, listOf(
                Track(1, "ДАВАЙ ТРАХАТЬСЯ В МАШИНЕ", "1:55", R.raw.fuck_1, 1),
                Track(2, "Люби меня, алина", "1:37", R.raw.fuck_2, 1),
                Track(3, "ГИДРОПОН", "2:24", R.raw.fuck_3, 1),
                Track(4, "ПАПИК", "1:58", R.raw.fuck_4, 1, featArtist = "17 SEVENTEEN"),
                Track(5, "лиза,настя", "1:47", R.raw.fuck_5, 1),
                Track(6, "вайфуу", "2:10", R.raw.fuck_6, 1),
                Track(7, "я схавал опиат", "2:18", R.raw.fuck_7, 1),
                Track(8, "Вирус", "2:29", R.raw.fuck_8, 1),
                Track(9, "МОЯ МАМА ПЬЁТ", "2:09", R.raw.fuck_9, 1),
                Track(10, "Ты любишь травку", "2:13", R.raw.fuck_10, 1),
                Track(11, "Забуду", "3:03", R.raw.fuck_11, 1),
                Track(12, "Мне похуй", "1:53", R.raw.fuck_12, 1),
            )),
            Album(2, "дели на два", "2023", R.drawable.album_2, listOf(
                Track(101, "ты любишь танцевать", "2:24", R.raw.album2_track1, 2),
                Track(102, "пятый элемент", "2:14", R.raw.album2_track2, 2),
                Track(103, "целую тебя", "2:09", R.raw.album2_track3, 2),
                Track(104, "воздух", "2:04", R.raw.album2_track4, 2),
            )),
            Album(3, "Как испортить вечеринку?", "2023", R.drawable.album_3, listOf(
                Track(201, "Юра, Юра", "2:08", R.raw.album3_track1, 3),
                Track(202, "По улице иду я", "2:32", R.raw.album3_track2, 3),
                Track(203, "Они все дрочат на тебя в интернете", "1:48", R.raw.album3_track3, 3),
                Track(204, "Стенки моего подъезда", "2:29", R.raw.album3_track4, 3),
                Track(205, "Василий", "2:26", R.raw.album3_track5, 3),
                Track(206, "Травматика", "2:40", R.raw.album3_track6, 3),
                Track(207, "И это прекрасно", "3:25", R.raw.album3_track7, 3),
                Track(208, "Клей", "2:25", R.raw.album3_track8, 3),
                Track(209, "Целовались", "2:32", R.raw.album3_track9, 3),
                Track(210, "Пьяные", "2:08", R.raw.album3_track10, 3),
                Track(211, "Высокий градус", "2:33", R.raw.album3_track11, 3),
                Track(212, "Но им не смешно", "2:23", R.raw.album3_track12, 3),
                Track(213, "Семнадцатилетняя", "2:32", R.raw.album3_track13, 3),
                Track(214, "Я схожу с ума", "3:07", R.raw.album3_track14, 3),
                Track(215, "ДПП (Аутро)", "1:48", R.raw.album3_track15, 3),
            )),
            Album(4, "кажется, в аду прикольно, но меня выгнали б утром", "2024", R.drawable.album_4, listOf(
                Track(301, "Влечение", "2:11", R.raw.album4_track1, 4),
                Track(302, "привет, если ты мне не ответишь", "2:03", R.raw.album4_track2, 4),
                Track(303, "фура", "2:14", R.raw.album4_track3, 4),
                Track(304, "мой врач думает что у меня шизофрения", "2:14", R.raw.album4_track4, 4),
                Track(305, "маршрутка", "3:10", R.raw.album4_track5, 4),
                Track(306, "ну почему", "2:52", R.raw.album4_track6, 4),
                Track(307, "я тупая, моя жизнь тупая", "3:07", R.raw.album4_track7, 4),
                Track(308, "пока-пока", "2:53", R.raw.album4_track8, 4),
                Track(309, "нам это нравится", "2:57", R.raw.album4_track9, 4),
                Track(310, "больше, чем творчество", "2:34", R.raw.album4_track10, 4),
            )),
            Album(5, "в моих легких выросли цветы", "2025", R.drawable.album_5, listOf(
                Track(401, "107.1", "1:58", R.raw.album5_track1, 5),
                Track(402, "печаль", "2:10", R.raw.album5_track2, 5),
                Track(403, "минус,плюс", "3:44", R.raw.album5_track3, 5),
                Track(404, "переломай мои кости", "2:48", R.raw.album5_track4, 5),
                Track(405, "давай увидимся", "4:00", R.raw.album5_track5, 5),
                Track(406, "твои поцелуи", "2:32", R.raw.album5_track6, 5),
                Track(407, "кислород", "2:22", R.raw.album5_track7, 5),
                Track(408, "или хотя бы завтра...", "1:45", R.raw.album5_track8, 5),
                Track(409, "самокрутки", "2:34", R.raw.album5_track9, 5),
                Track(410, "улыбнись", "4:29", R.raw.album5_track10, 5),
            )),
            Album(6, "неуравновешеннолетниепесни pt.1", "2025", R.drawable.album_6, listOf(
                Track(501, "дьявол!", "3:28", R.raw.album6_track1, 6),
                Track(502, "оригами", "2:24", R.raw.album6_track2, 6),
                Track(503, "шАхАшАхА", "2:54", R.raw.album6_track3, 6),
                Track(504, "песня про спид", "2:50", R.raw.album6_track4, 6),
                Track(505, "конъюктивит", "3:28", R.raw.album6_track5, 6),
                Track(506, "тварьтварьтварьтварь...", "3:06", R.raw.album6_track6, 6),
                Track(507, "злой отчим", "3:45", R.raw.album6_track7, 6),
            )),
            Album(7, "прыгайдуравишлист!", "2025", R.drawable.album_7, listOf(
                Track(601, "прыгай, дура!", "1:59", R.raw.album7_track1, 7),
                Track(602, "вишлист", "2:07", R.raw.album7_track2, 7),
            )),
            Album(8, "Совместные релизы", "2030", R.drawable.album_8, listOf(
                Track(701, "Сколько мы не спали", "1:51", R.raw.album8_track1, 8, featArtist = "Рэйчи"),
                Track(702, "1 мая", "2:07", R.raw.album8_track2, 8, featArtist = "madk1d"),
                Track(703, "Круче чем вы", "1:40", R.raw.album8_track3, 8, featArtist = "madk1d"),
                Track(704, "Виолетта", "1:45", R.raw.album8_track4, 8, featArtist = "Рэйчи"),
                Track(705, "Бардак", "1:53", R.raw.album8_track5, 8, featArtist = "17 SEVENTEEN"),
                Track(706, "ВШБ", "1:49", R.raw.album8_track6, 8, featArtist = "GRILLYAZH"),
                Track(707, "НЕ ПО СЕБЕ", "3:01", R.raw.album8_track7, 8, featArtist = "источник,Niño"),
            )),
            Album(9, "UNRELEASE", "2030", R.drawable.album_9, listOf(
                Track(801, "Трамадол", "1:22", R.raw.album9_track1, 9),
                Track(802, "Компромат", "2:78", R.raw.album9_track2, 9),
                Track(803, "Я стану популярным в интернете", "1:15", R.raw.album9_track3, 9),
                Track(804, "Я проститутка", "1:45", R.raw.album9_track4, 9),
                Track(805, "Откуда ты взялась", "3:41", R.raw.album9_track5, 9),
                Track(806, "тогда мы не были вдвоем", "3:59", R.raw.album9_track6, 9),
                Track(807, "забываю", "2:03", R.raw.album9_track7, 9),
            )),
        )
    }
    val allTracks = remember(albums) { albums.flatMap { it.tracks } }

    LaunchedEffect(Unit) {
        val token = prefs.getString("auth_token", null)

        if (token.isNullOrBlank() || token == "guest") {
            username = "Гость"
            loading = false
            return@LaunchedEffect
        }

        try {
            val conn = withContext(Dispatchers.IO) {
                (URL("https://cupsize-api.usvidelsvet.workers.dev/me").openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    setRequestProperty("Authorization", "Bearer $token")
                }
            }
            val text = withContext(Dispatchers.IO) { conn.inputStream.bufferedReader().readText() }
            val json = JSONObject(text)
            if (json.optBoolean("ok")) {
                username = json.getJSONObject("user").getString("username")
            }
        } catch (_: Exception) {
            username = "Недоступно"
        }

        stats = ListenTracker.fetchStats(token)
        loading = false
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
                                Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE8D5FF).copy(alpha = 0.15f))
                                    .border(1.dp, Color(0xFFE8D5FF).copy(alpha = 0.25f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    (username ?: "?").take(1).uppercase(),
                                    color = Color(0xFFE8D5FF),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 20.sp
                                )
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
                        ?.let { top -> allTracks.firstOrNull { it.id == top.trackId } }

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
                            if (favoriteAlbum != null) {
                                Image(
                                    painterResource(favoriteAlbum.coverRes),
                                    null,
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

                        stats!!.topTracks.forEachIndexed { index, topTrack ->
                            val track = allTracks.firstOrNull { it.id == topTrack.trackId }
                                ?: return@forEachIndexed
                            val album = albums.firstOrNull { it.id == track.albumId }

                            TopTrackRow(
                                position = index + 1,
                                track = track,
                                coverRes = album?.coverRes,
                                playCount = topTrack.playCount
                            )
                            if (index < stats!!.topTracks.lastIndex) Spacer(Modifier.height(8.dp))
                        }

                        Spacer(Modifier.height(16.dp))
                    }
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

                val supporters = listOf(
                    "Sergey" to "@sparklesparky",
                    "mngl" to "@mngl15",
                    "Ya_kro" to "@Ya_kro",
                    "zzedqq" to "@zzedqq7",
                    "vers #lisoff!" to "@versupp"
                )

                supporters.forEach { (name, handle) ->
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
                                    .background(Color(0xFFE8D5FF).copy(alpha = 0.10f))
                                    .border(1.dp, Color(0xFFE8D5FF).copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    name.take(1).uppercase(),
                                    color = Color(0xFFE8D5FF).copy(alpha = 0.70f),
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
private fun TopTrackRow(position: Int, track: Track, coverRes: Int?, playCount: Int) {
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
        if (coverRes != null) {
            Image(
                painterResource(coverRes),
                null,
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
                Text("КОДЕР", color = Color.White.copy(alpha = 0.45f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.5.sp, modifier = Modifier.align(Alignment.Start))
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SocialButton("Telegram", "✈️", "https://t.me/videlsvet", Color(0xFF1A73C8), Modifier.weight(1f))
                    SocialButton("TikTok", "🎵", "https://www.tiktok.com/@wave66181?_r=1&_t=ZS-94DvxyuzLYi", Color(0xFF1A1A1A), Modifier.weight(1f))
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
                    "CUPSIZE — российская группа из Ярославля, играющая смесь гранжа, гаражного рока и абсурдной иронии. Название переводится как «Размер чашечки лифчика». Свой стиль музыканты называют «гаражной залупой», «ПМС-гранжем» или «музыкой сюра».",
                    color = Color.White.copy(alpha = 0.78f),
                    fontSize = 14.sp,
                    lineHeight = 21.sp
                )

                Spacer(Modifier.height(18.dp))
                InfoSectionTitle("Состав:")

                InfoBullet("Николай Мамаев (род. 16 июля 2003) — фронтмен, вокалист, основатель.")
                InfoBullet("Ярик — басист.")
                InfoBullet("Серега КПЗ (Сергей Крылов) — барабанщик (с 2023 года).")
                InfoBullet("Птаха — звукорежиссёр.")

                Spacer(Modifier.height(16.dp))
                InfoSectionTitle("Дискография:")

                InfoBullet("2023 — «дели на два», «Как испортить вечеринку?»")
                InfoBullet("2024 — «кажется, в аду прикольно, но меня выгнали б утром…»")
                InfoBullet("2025 — «в моих легких выросли цветы», «неуравновешеннолетниепесни pt.1», «прыгайдуравишлист!»")

                Spacer(Modifier.height(16.dp))
                InfoSectionTitle("Факты:")

                InfoBullet("Раньше CUPSIZE был сольным проектом Николая, но с 2024 года превратился в группу — старые фиты убрали со стримингов.")
                InfoBullet("В январе 2024 года Николая оштрафовали на 5000 рублей за пропаганду наркотиков в текстах, после чего альбом «Еби меня, малышка» удалили с площадок.")
                InfoBullet("Коля и Птаха лежали в одном психоневрологическом стационаре в Ярославле с разницей в год — оба с тревожным расстройством.")
                InfoBullet("В 2025 году группа выступала на фестивале «МТС Live Лето» и собрала 3000 зрителей в Москве.")

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
        Image(
            painter = painterResource(album.coverRes),
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
                            coverRes = alb.coverRes,
                            isActive = currentTrack?.id == track.id,
                            isPlaying = isPlaying
                        ) { onTrackClick(track) }
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
                    FlatTrackRow(track, alb.coverRes, currentTrack?.id == track.id, isPlaying) {
                        onTrackClick(track)
                    }
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
                Image(
                    painter = painterResource(album.coverRes),
                    contentDescription = album.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .shadow(16.dp, RoundedCornerShape(14.dp))
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
private fun FlatTrackRow(track: Track, coverRes: Int, isActive: Boolean, isPlaying: Boolean, onClick: () -> Unit) {
    val bg by animateFloatAsState(if (isActive) 0.12f else 0f, tween(200), label = "fb")
    Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 3.dp).clip(RoundedCornerShape(14.dp)).background(Color.White.copy(alpha = bg)).clickable { onClick() }.padding(horizontal = 10.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
        Image(painterResource(coverRes), null, modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(track.title, color = if (isActive) Color.White else Color.White.copy(alpha = 0.88f), fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal, fontSize = 13.sp)
            Text(track.displayArtist, color = Color.White.copy(alpha = 0.45f), fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        if (isActive) { PlayingIndicatorDots(isPlaying = isPlaying); Spacer(Modifier.width(8.dp)) }
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
    onToggleLike: (Int) -> Unit
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
                Image(
                    painter = painterResource(album.coverRes),
                    contentDescription = null,
                    modifier = Modifier
                        .size(300.dp)
                        .shadow(28.dp, RoundedCornerShape(28.dp))
                        .clip(RoundedCornerShape(28.dp))
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
                onClick = { onTrackClick(track) },
                onToggleLike = { onToggleLike(track.id) }
            )
        }
    }
}

@Composable
private fun AlbumTrackRow(track: Track, isActive: Boolean, isPlaying: Boolean, isLiked: Boolean, onClick: () -> Unit, onToggleLike: () -> Unit) {
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
        IconButton(onToggleLike, Modifier.size(32.dp)) {
            val ha by animateFloatAsState(if (isLiked) 1f else 0.35f, spring(stiffness = Spring.StiffnessMediumLow), label = "ha")
            Icon(if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder, null, tint = if (isLiked) Color(0xFFFF6B8A) else Color.White.copy(alpha = ha), modifier = Modifier.size(16.dp))
        }
        Text(track.duration, color = Color.White.copy(alpha = 0.40f), fontSize = 12.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
    }
}

@Composable
private fun MiniPlayerBar(
    track: Track, coverRes: Int, isPlaying: Boolean, positionMs: Long, durationMs: Long,
    accentColor: Color, isLiked: Boolean, onPlayPause: () -> Unit, onExpand: () -> Unit,
    prevTrack: Track?, nextTrack: Track?, prevCoverRes: Int, nextCoverRes: Int,
    onPrevious: () -> Unit, onNext: () -> Unit, onToggleLike: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val cc = remember { mutableStateMapOf<Int, Color>() }
    LaunchedEffect(coverRes, prevCoverRes, nextCoverRes) {
        listOf(coverRes, prevCoverRes, nextCoverRes).distinct().forEach { r ->
            if (!cc.containsKey(r)) {
                cc[r] = computeDominantColorFromRes(ctx, r).ensureNotTooBright().darken(0.15f)
            }
        }
    }
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
        fun MiniCard(t: Track, res: Int, active: Boolean, bx: Float, onClick: () -> Unit) {
            val bg = (cc[res] ?: accentColor).darken(0.10f)
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
                    Image(
                        painterResource(res),
                        null,
                        modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp))
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
                MiniCard(prevTrack, prevCoverRes, false, prevX) { scope.launch { goPrev() } }
            }
            if (nextTrack != null) {
                MiniCard(nextTrack, nextCoverRes, false, nextX) { scope.launch { goNext() } }
            }
            MiniCard(track, coverRes, true, curX, safeE)
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
    onClose: () -> Unit,
    onPlayPause: () -> Unit,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    onToggleLike: (Int) -> Unit,
    onOpenAlbum: (Album) -> Unit,
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
    val realProg =
        if (durationMs > 0) (positionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f
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
            .then(
                if (showLyrics && track.id == 405) {
                    Modifier.background(Color.Transparent)
                } else {
                    Modifier.background(bgBrush)
                }
            )
            .graphicsLayer { alpha = screenAlpha }
    ) {
        val showVideoBg = showLyrics && track.id == 405
        if (showVideoBg) {
            val context = LocalContext.current

            val videoPlayer = remember(track.id) {
                ExoPlayer.Builder(context).build().apply {
                    repeatMode = Player.REPEAT_MODE_ONE
                    volume = 0f
                    setMediaItem(
                        MediaItem.fromUri(
                            Uri.parse("android.resource://${context.packageName}/${R.raw.bg_track_405}")
                        )
                    )
                    prepare()
                    playWhenReady = true
                }
            }

            DisposableEffect(track.id) {
                onDispose { videoPlayer.release() }
            }

            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = videoPlayer
                        useController = false
                        resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                        setShutterBackgroundColor(android.graphics.Color.TRANSPARENT)
                        setBackgroundColor(android.graphics.Color.TRANSPARENT)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.20f))
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(Modifier.height(12.dp))

                Row(
                    Modifier.fillMaxWidth(),
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
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .scale(coverScale)
                                .shadow(
                                    elevation = (40 * coverElevation).dp,
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .clip(RoundedCornerShape(20.dp))
                        ) {
                            Image(
                                painter = painterResource(album.coverRes),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    } else {
                        LyricsView(
                            viewModel = lyricsVm,
                            modifier = Modifier
                                .fillMaxSize(),
                            onLineClick = { ms -> onSeekTo(ms) }
                        )
                    }
                }

                if (!showLyrics) {
                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
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
                                val next = !showLyrics
                                if (next) lyricsVm.updatePosition(positionMs)
                                showLyrics = next
                            }
                        )
                    }
                } else {
                    Spacer(Modifier.height(10.dp))
                }
            }

            if (!showLyrics) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = controlsLift)
                ) {
                    MinimalProgressBar(
                        progress = displayProg,
                        accentColor = accent,
                        modifier = Modifier.fillMaxWidth(),
                        onDragStart = { isDragging = true; dragProg = it },
                        onDragEnd = {
                            onSeekTo((it * max(1L, durationMs)).toLong())
                            isDragging = false
                        },
                        onTapSeek = {
                            onSeekTo((it * max(1L, durationMs)).toLong())
                            isDragging = false
                        }
                    )

                    Spacer(Modifier.height(4.dp))

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
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
                                tint = if (prev != null) Color.White.copy(alpha = 0.9f)
                                else Color.White.copy(alpha = 0.25f)
                            )
                        }

                        MainPlayButton(isPlaying = isPlaying, onClick = onPlayPause)

                        IconButton(onClick = { if (next != null) onNext() }) {
                            Icon(
                                Icons.Filled.SkipNext,
                                null,
                                tint = if (next != null) Color.White.copy(alpha = 0.9f)
                                else Color.White.copy(alpha = 0.25f)
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
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
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
                indication        = null,
                interactionSource = remember { MutableInteractionSource() },
                enabled           = isEnabled
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
                    is LyricsViewModel.State.Loading  -> "…"
                    is LyricsViewModel.State.NotFound -> "Нет текста"
                    else                              -> "Текст"
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
    val sc by animateFloatAsState(if (isPlaying) 1f else 0.93f, spring(0.55f, Spring.StiffnessMedium), label = "psc")
    Box(
        Modifier
            .size(62.dp)
            .scale(sc)
            .shadow(if (isPlaying) 28.dp else 12.dp, CircleShape, ambientColor = Color.White.copy(alpha = 0.15f))
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
                .clickable(remember { MutableInteractionSource() }, null, enabled = enabled) { onClick() },
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
                    .border(1.dp, Color(0xFFFF6B8A).copy(alpha = 0.18f), RoundedCornerShape(24.dp))
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

private suspend fun computeDominantColorFromRes(context: android.content.Context, resId: Int): Color =
    withContext(Dispatchers.Default) {
        try { Color(Palette.Builder(BitmapFactory.decodeResource(context.resources, resId)).generate().getDominantColor(0xFF111111.toInt())) }
        catch (_: Exception) { Color(0xFF111111) }
    }

private fun Color.darken(a: Float): Color = Color(red * (1f - a), green * (1f - a), blue * (1f - a), alpha)
private fun Color.ensureNotTooBright(): Color { val l = 0.2126 * red + 0.7152 * green + 0.0722 * blue; return if (l > 0.72f) darken(0.38f) else this }