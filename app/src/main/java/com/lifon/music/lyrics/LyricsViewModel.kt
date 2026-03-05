package com.lifon.music.lyrics

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

// БЕЗ Hilt — никаких @HiltViewModel / @Inject / dagger импортов

class LyricsViewModel : ViewModel() {

    sealed class State {
        object Idle : State()
        object Loading : State()
        data class Ready(val lines: List<LyricsLine>, val isSynced: Boolean) : State()
        object NotFound : State()
    }

    private val _state = MutableStateFlow<State>(State.Idle)
    val state: StateFlow<State> = _state.asStateFlow()

    private val _currentLineIndex = MutableStateFlow(-1)
    val currentLineIndex: StateFlow<Int> = _currentLineIndex.asStateFlow()

    private var trackKey = -1
    private var positionJob: Job? = null

    fun load(context: Context, trackId: Int) {
        if (trackId == trackKey) return
        trackKey = trackId

        _state.value = State.Loading
        _currentLineIndex.value = -1

        viewModelScope.launch {
            val lines = LocalLyricsRepository.getLyrics(context, trackId)
            _state.value = when {
                lines.isNullOrEmpty() -> State.NotFound
                else -> State.Ready(
                    lines = lines,
                    isSynced = lines.any { it.timeMs > 0L } // если есть нормальные таймкоды
                )
            }
        }
    }

    fun updatePosition(positionMs: Long) {
        val ready = _state.value as? State.Ready ?: return
        val idx = ready.lines.indexOfLast { it.timeMs <= positionMs }
        if (idx != _currentLineIndex.value) _currentLineIndex.value = idx
    }

    fun startPositionTicker(positionProvider: () -> Long) {
        positionJob?.cancel()
        positionJob = viewModelScope.launch {
            while (isActive) {
                updatePosition(positionProvider())
                delay(200L)
            }
        }
    }

    fun stopPositionTicker() { positionJob?.cancel() }

    override fun onCleared() {
        positionJob?.cancel()
        super.onCleared()
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = LyricsViewModel() as T
        }
    }
}