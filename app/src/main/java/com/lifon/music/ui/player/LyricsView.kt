package com.lifon.music.lyrics

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LyricsView(
    viewModel: LyricsViewModel,
    modifier: Modifier = Modifier,
    onLineClick: ((Long) -> Unit)? = null
) {
    val state      by viewModel.state.collectAsState()
    val currentIdx by viewModel.currentLineIndex.collectAsState()
    val listState  = rememberLazyListState()

    // ✅ Сбрасываем "первичную позицию" при смене трека/контента
    var didInitialScroll by remember(state) { mutableStateOf(false) }

    // ❗️Один и тот же стиль для всех строк — без прыжков разметки
    val baseStyle = MaterialTheme.typography.headlineMedium.copy(
        fontWeight = FontWeight.SemiBold,
        fontSize = 26.sp,
        lineHeight = 34.sp
    )

    // ✅ Первый скролл — мгновенно, дальше — плавно
    LaunchedEffect(state, currentIdx) {
        val s = state as? LyricsViewModel.State.Ready ?: return@LaunchedEffect
        if (currentIdx < 0 || currentIdx >= s.lines.size) return@LaunchedEffect

        val target = (currentIdx - 1).coerceAtLeast(0)

        if (!didInitialScroll) {
            // моментально ставим на нужное место — без "скачка"
            listState.scrollToItem(target)
            didInitialScroll = true
        } else {
            // дальше можно красиво анимировать
            listState.animateScrollToItem(target)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        when (val s = state) {
            is LyricsViewModel.State.Idle -> Unit

            is LyricsViewModel.State.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.White
                )
            }

            is LyricsViewModel.State.NotFound -> {
                Text(
                    text = "Текст не найден",
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.White.copy(alpha = 0.4f),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            is LyricsViewModel.State.Ready -> {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        top = 100.dp,
                        bottom = 140.dp,
                        start = 28.dp,
                        end = 28.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    itemsIndexed(items = s.lines, key = { i, _ -> i }) { idx, line ->
                        val isActive = idx == currentIdx
                        val isPast = idx < currentIdx

                        val alpha by animateFloatAsState(
                            targetValue = when {
                                isActive -> 1f
                                isPast -> 0.22f
                                else -> 0.42f
                            },
                            animationSpec = tween(220),
                            label = "lyricsAlpha"
                        )

                        val scale by animateFloatAsState(
                            targetValue = if (isActive) 1f else 0.96f,
                            animationSpec = spring(dampingRatio = 0.85f, stiffness = 420f),
                            label = "lyricsScale"
                        )

                        Text(
                            text = line.text,
                            style = baseStyle,
                            color = Color.White.copy(alpha = alpha),
                            modifier = Modifier
                                .fillMaxWidth()
                                .graphicsLayer {
                                    scaleX = scale
                                    scaleY = scale
                                    transformOrigin = TransformOrigin(0f, 0.5f)
                                }
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() },
                                    enabled = s.isSynced && onLineClick != null
                                ) { onLineClick?.invoke(line.timeMs) }
                        )
                    }
                }
            }
        }
    }
}