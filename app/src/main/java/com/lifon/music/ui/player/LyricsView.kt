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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LyricsView(
    viewModel: LyricsViewModel,
    modifier: Modifier = Modifier,
    onLineClick: ((Long) -> Unit)? = null
) {
    val state      by viewModel.state.collectAsState()
    val currentIdx by viewModel.currentLineIndex.collectAsState()
    val listState  = rememberLazyListState()
    val scope      = rememberCoroutineScope()
    val density    = LocalDensity.current

    var didInitialScroll by remember(state) { mutableStateOf(false) }

    val isUserScrolling = listState.isScrollInProgress

    var userTookControl by remember(state) { mutableStateOf(false) }

    LaunchedEffect(isUserScrolling) {
        if (isUserScrolling) {
            userTookControl = true
        }
    }

    LaunchedEffect(isUserScrolling, userTookControl) {
        if (!isUserScrolling && userTookControl) {
            delay(5_000L)
            userTookControl = false
        }
    }

    val inactiveStyle = MaterialTheme.typography.headlineMedium.copy(
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 30.sp,
        textAlign = TextAlign.Center
    )

    val activeStyle = MaterialTheme.typography.headlineMedium.copy(
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        textAlign = TextAlign.Center
    )

    val itemHeightPx = with(density) { 54.dp.toPx() }

    LaunchedEffect(state, currentIdx) {
        val s = state as? LyricsViewModel.State.Ready ?: return@LaunchedEffect
        if (currentIdx < 0 || currentIdx >= s.lines.size) return@LaunchedEffect

        if (!didInitialScroll) {
            val viewportHeight = listState.layoutInfo.viewportEndOffset - listState.layoutInfo.viewportStartOffset
            val offset = (viewportHeight / 2 - itemHeightPx / 2).toInt()
            listState.scrollToItem(currentIdx, -offset)
            didInitialScroll = true
        } else if (!userTookControl) {
            val viewportHeight = listState.layoutInfo.viewportEndOffset - listState.layoutInfo.viewportStartOffset
            val offset = (viewportHeight / 2 - itemHeightPx / 2).toInt()
            scope.launch {
                listState.animateScrollToItem(currentIdx, -offset)
            }
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

                        Text(
                            text = line.text,
                            style = if (isActive) activeStyle else inactiveStyle,
                            textAlign = TextAlign.Center,
                            color = Color.White.copy(alpha = alpha),
                            modifier = Modifier
                                .fillMaxWidth()
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
