package com.king_grey.retro_snake.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.king_grey.retro_snake.data.Repository
import com.king_grey.retro_snake.data.RunRecord
import com.king_grey.retro_snake.data.Settings
import com.king_grey.retro_snake.data.StatsCalculator
import com.king_grey.retro_snake.data.Summary
import com.king_grey.retro_snake.game.Difficulty
import com.king_grey.retro_snake.game.Direction
import com.king_grey.retro_snake.game.GameState
import com.king_grey.retro_snake.game.GameStatus
import com.king_grey.retro_snake.game.InputBuffer
import com.king_grey.retro_snake.game.SnakeEngine
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

enum class GameEvent { EAT, CRASH, NEW_BEST, WIN }

data class RunResult(
    val record: RunRecord,
    val length: Int,
    val newBest: Boolean,
    val prevBest: Int,
    val cleared: Boolean,
)

class GameViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = Repository(app)

    var settings by mutableStateOf(repo.loadSettings()); private set
    var bests by mutableStateOf(repo.bests()); private set
    var state by mutableStateOf(SnakeEngine.newGame()); private set
    /** 3..1 while a countdown is showing, otherwise 0. */
    var countdown by mutableIntStateOf(0); private set
    var elapsedMs by mutableLongStateOf(0L); private set
    var topSpeed by mutableIntStateOf(1); private set
    var result by mutableStateOf<RunResult?>(null); private set
    /** Bumped whenever stored stats change so screens can re-read them. */
    var statsVersion by mutableIntStateOf(0); private set

    private val _events = MutableSharedFlow<GameEvent>(extraBufferCapacity = 8)
    val events: SharedFlow<GameEvent> = _events

    private val input = InputBuffer()
    private var loop: Job? = null
    private var activeDifficulty = settings.difficulty
    private var activeWalls = settings.walls

    fun update(change: (Settings) -> Settings) {
        settings = change(settings)
        repo.saveSettings(settings)
    }

    fun summary(): Summary = StatsCalculator.summarize(
        repo.runs(), repo.totalGames(), repo.totalApples(), repo.bests(), System.currentTimeMillis(),
    )

    fun resetStats() {
        repo.resetStats()
        bests = repo.bests()
        statsVersion++
    }

    // ---- game flow ----

    fun startNewGame() {
        loop?.cancel()
        activeDifficulty = settings.difficulty
        activeWalls = settings.walls
        state = SnakeEngine.newGame()
        input.clear()
        elapsedMs = 0
        topSpeed = 1
        result = null
        beginCountdown()
    }

    private fun beginCountdown() {
        loop?.cancel()
        loop = viewModelScope.launch {
            for (n in 3 downTo 1) {
                countdown = n
                delay(700)
            }
            countdown = 0
            state = state.copy(status = GameStatus.RUNNING)
            runLoop()
        }
    }

    private suspend fun runLoop() {
        while (state.status == GameStatus.RUNNING) {
            val d = activeDifficulty.delayFor(state.apples)
            delay(d)
            val dir = input.next(state.direction)
            val r = SnakeEngine.step(state, dir, activeWalls)
            elapsedMs += d
            state = r.state
            topSpeed = maxOf(topSpeed, activeDifficulty.levelFor(state.apples))
            if (r.ate) _events.tryEmit(GameEvent.EAT)
            if (state.status == GameStatus.OVER) {
                finish()
                return
            }
        }
    }

    private suspend fun finish() {
        val s = state
        val prev = bests[activeDifficulty] ?: 0
        val record = RunRecord(s.apples, activeDifficulty, elapsedMs, System.currentTimeMillis(), topSpeed)
        val newBest = repo.recordRun(record) && s.apples > 0
        bests = repo.bests()
        statsVersion++
        _events.tryEmit(if (s.cleared) GameEvent.WIN else GameEvent.CRASH)
        delay(if (s.crashed) 900 else 300)
        if (newBest) _events.tryEmit(GameEvent.NEW_BEST)
        result = RunResult(record, s.length, newBest, prev, s.cleared)
    }

    fun onDirection(dir: Direction) {
        if (state.status == GameStatus.RUNNING) input.offer(dir, state.direction)
    }

    fun pause() {
        if (state.status == GameStatus.RUNNING || (state.status == GameStatus.READY && countdown > 0)) {
            loop?.cancel()
            countdown = 0
            state = state.copy(status = GameStatus.PAUSED)
        }
    }

    fun resume() {
        if (state.status == GameStatus.PAUSED) {
            state = state.copy(status = GameStatus.READY)
            beginCountdown()
        }
    }

    /** Leave the game without recording it (only finished runs count in stats). */
    fun quit() {
        loop?.cancel()
        countdown = 0
        result = null
        state = SnakeEngine.newGame()
    }

    val activeLevelName: String get() = activeDifficulty.label
    val currentBest: Int get() = bests[activeDifficulty] ?: 0
    val activeDifficultyValue: Difficulty get() = activeDifficulty
}
