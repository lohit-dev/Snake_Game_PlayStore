package com.king_grey.retro_snake.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.king_grey.retro_snake.data.ControlMode
import com.king_grey.retro_snake.data.StatsCalculator
import com.king_grey.retro_snake.game.BOARD_H
import com.king_grey.retro_snake.game.BOARD_W
import com.king_grey.retro_snake.game.Direction
import com.king_grey.retro_snake.game.GameStatus
import com.king_grey.retro_snake.sound.Tone
import com.king_grey.retro_snake.ui.GameEvent
import com.king_grey.retro_snake.ui.GameViewModel
import com.king_grey.retro_snake.ui.RunResult
import com.king_grey.retro_snake.ui.components.ExtrudedText
import com.king_grey.retro_snake.ui.components.LcdDigits
import com.king_grey.retro_snake.ui.components.MonoText
import com.king_grey.retro_snake.ui.components.PauseIcon
import com.king_grey.retro_snake.ui.components.PixelButton
import com.king_grey.retro_snake.ui.components.PixelKey
import com.king_grey.retro_snake.ui.components.PixelPanel
import com.king_grey.retro_snake.ui.components.PixelText
import com.king_grey.retro_snake.ui.components.RetroScreen
import com.king_grey.retro_snake.ui.components.TriangleIcon
import com.king_grey.retro_snake.ui.components.Notch3
import com.king_grey.retro_snake.ui.components.Notch6
import com.king_grey.retro_snake.ui.components.blinking
import com.king_grey.retro_snake.ui.components.rememberTick
import com.king_grey.retro_snake.ui.theme.LocalReduceMotion
import com.king_grey.retro_snake.ui.theme.LocalRetro
import com.king_grey.retro_snake.ui.theme.LocalSettings
import com.king_grey.retro_snake.ui.theme.LocalSfx
import com.king_grey.retro_snake.ui.theme.PixelFont
import kotlin.math.abs

@Composable
fun GameScreen(vm: GameViewModel, onExit: () -> Unit, onStats: () -> Unit) {
    val settings = LocalSettings.current
    val sfx = LocalSfx.current
    val haptic = LocalHapticFeedback.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    // Sound and haptics for game events.
    LaunchedEffect(vm) {
        vm.events.collect { e ->
            val s = vm.settings
            if (s.sound) {
                when (e) {
                    GameEvent.EAT -> sfx?.play(Tone.EAT)
                    GameEvent.CRASH -> sfx?.play(Tone.CRASH)
                    GameEvent.NEW_BEST, GameEvent.WIN -> sfx?.play(Tone.FANFARE)
                }
            }
            if (s.haptics) {
                when (e) {
                    GameEvent.EAT -> haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    GameEvent.CRASH -> haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    else -> {}
                }
            }
        }
    }

    // Auto-pause when the app goes to the background.
    DisposableEffect(lifecycle, vm) {
        val obs = LifecycleEventObserver { _, ev -> if (ev == Lifecycle.Event.ON_PAUSE) vm.pause() }
        lifecycle.addObserver(obs)
        onDispose { lifecycle.removeObserver(obs) }
    }

    val result = vm.result
    val status = vm.state.status
    BackHandler {
        when {
            result != null -> onExit()
            status == GameStatus.PAUSED -> vm.resume()
            else -> vm.pause()
        }
    }

    RetroScreen(maxContentWidth = Dp.Infinity) {
        if (result != null) {
            GameOverContent(result, vm, onAgain = { vm.startNewGame() }, onMenu = onExit, onStats = onStats)
        } else {
            PlayContent(vm, settings.controls)
            if (status == GameStatus.PAUSED && vm.countdown == 0) {
                PauseOverlay(vm, onResume = { vm.resume() }, onRestart = { vm.startNewGame() }, onQuit = onExit)
            }
        }
    }
}

@Composable
private fun PlayContent(vm: GameViewModel, controls: ControlMode) {
    val c = LocalRetro.current
    val showPad = controls != ControlMode.SWIPE
    val swipe = controls != ControlMode.DPAD
    val density = LocalDensity.current
    val threshold = with(density) { 24.dp.toPx() }

    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .then(
                if (swipe) {
                    Modifier.pointerInput(vm, threshold) {
                        var dx = 0f
                        var dy = 0f
                        detectDragGestures(
                            onDragStart = { dx = 0f; dy = 0f },
                            onDrag = { change, amount ->
                                change.consume()
                                dx += amount.x
                                dy += amount.y
                                if (maxOf(abs(dx), abs(dy)) > threshold) {
                                    val dir = if (abs(dx) > abs(dy)) {
                                        if (dx > 0) Direction.RIGHT else Direction.LEFT
                                    } else {
                                        if (dy > 0) Direction.DOWN else Direction.UP
                                    }
                                    vm.onDirection(dir)
                                    dx = 0f
                                    dy = 0f
                                }
                            },
                        )
                    }
                } else {
                    Modifier
                },
            ),
    ) {
        // Fit the board to whatever space is left after the HUD and the D-pad.
        val hud = 84.dp
        val pad = if (showPad) 216.dp else 56.dp
        val frame = 14.dp
        val byWidth = ((maxWidth - 40.dp - frame) / BOARD_W).value
        val byHeight = ((maxHeight - hud - pad - frame) / BOARD_H).value
        val cell = minOf(byWidth, byHeight).toInt().coerceAtLeast(8)

        if (maxWidth > maxHeight) {
            // Landscape (tablets, foldables, split screen): board on the left, controls on the right.
            val cellL = ((maxHeight - 28.dp - frame) / BOARD_H).value.toInt().coerceAtLeast(8)
            Row(
                Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(28.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BoardWithCountdown(vm, cellL)
                Column(
                    Modifier.width(272.dp).verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    HudStacked(vm)
                    if (showPad) DPad(onPress = vm::onDirection)
                    else MonoText("Swipe anywhere to turn", 13, c.muted)
                }
            }
        } else {
            Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                Hud(vm)
                BoardWithCountdown(vm, cell)
                Spacer(Modifier.weight(1f))
                if (showPad) {
                    DPad(onPress = vm::onDirection)
                    Spacer(Modifier.height(12.dp))
                } else {
                    MonoText("Swipe anywhere to turn", 13, c.muted, Modifier.padding(bottom = 20.dp))
                }
            }
        }
    }
}

@Composable
private fun BoardWithCountdown(vm: GameViewModel, cell: Int) {
    val c = LocalRetro.current
    Box(contentAlignment = Alignment.Center) {
        PixelPanel(fill = c.bg) {
            Box(Modifier.padding(4.dp)) { Board(vm, cell) }
        }
        if (vm.countdown > 0) {
            ExtrudedText(vm.countdown.toString(), 72, c.accent, c.accentDark, c.shadow, step = 4.dp)
        }
    }
}

@Composable
private fun ScorePanel(vm: GameViewModel) {
    val c = LocalRetro.current
    PixelPanel(fill = c.deep) {
        Column(Modifier.padding(start = 14.dp, end = 14.dp, top = 6.dp, bottom = 8.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            MonoText("SCORE", 11, c.muted, letterSpacing = 1.8f, lineHeight = 14)
            LcdDigits(vm.state.apples, 22, c.data)
        }
    }
}

@Composable
private fun HudStacked(vm: GameViewModel) {
    val c = LocalRetro.current
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            PixelButton(onClick = { vm.pause() }, modifier = Modifier.width(56.dp), height = 56.dp, description = "Pause") { PauseIcon(c.text) }
            ScorePanel(vm)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            HudStat("HI ", maxOf(vm.currentBest, vm.state.apples).toString().padStart(3, '0'), c.accent)
            HudStat("SPD ", vm.activeDifficultyValue.levelFor(vm.state.apples).toString(), c.text)
        }
    }
}

@Composable
private fun Hud(vm: GameViewModel) {
    val c = LocalRetro.current
    Row(
        Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Box(Modifier.width(84.dp)) {
            PixelButton(
                onClick = { vm.pause() },
                modifier = Modifier.width(56.dp),
                height = 56.dp,
                description = "Pause",
            ) { PauseIcon(c.text) }
        }
        ScorePanel(vm)
        Column(Modifier.width(84.dp), horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            HudStat("HI ", maxOf(vm.currentBest, vm.state.apples).toString().padStart(3, '0'), c.accent)
            HudStat("SPD ", vm.activeDifficultyValue.levelFor(vm.state.apples).toString(), c.text)
        }
    }
}

@Composable
private fun HudStat(label: String, value: String, valueColor: Color) {
    val c = LocalRetro.current
    Text(
        buildAnnotatedString {
            withStyle(SpanStyle(color = c.muted)) { append(label) }
            withStyle(SpanStyle(color = valueColor)) { append(value) }
        },
        fontFamily = PixelFont,
        fontSize = 10.sp,
        lineHeight = 12.sp,
        maxLines = 1,
        softWrap = false,
    )
}

@Composable
private fun Board(vm: GameViewModel, cellDp: Int) {
    val c = LocalRetro.current
    val settings = LocalSettings.current
    val reduce = LocalReduceMotion.current
    val tick = rememberTick(400)
    val w = (BOARD_W * cellDp).dp
    val h = (BOARD_H * cellDp).dp
    Canvas(Modifier.size(w, h).background(c.deep)) {
        val cell = cellDp.dp.toPx()
        val state = vm.state
        if (settings.grid) {
            val r = 2.dp.toPx()
            for (y in 0 until BOARD_H) for (x in 0 until BOARD_W) {
                drawRect(c.dots, Offset(x * cell + cell / 2 - r / 2, y * cell + cell / 2 - r / 2), Size(r, r))
            }
        }
        // food: pulses between full and dim
        state.food?.let { f ->
            val on = reduce || tick.value % 2 == 0
            val sz = cell * 0.62f
            val cx = f.x * cell + cell / 2
            val cy = f.y * cell + cell / 2
            rotate(45f, pivot = Offset(cx, cy)) {
                drawRect(c.food.copy(alpha = if (on) 1f else 0.4f), Offset(cx - sz / 2, cy - sz / 2), Size(sz, sz))
            }
        }
        val n = state.snake.size
        val inset = 1.dp.toPx()
        for (i in n - 1 downTo 0) {
            val p = state.snake[i]
            val color = when {
                i == 0 && state.crashed -> c.danger
                i == 0 -> c.snakeHead
                else -> lerp(c.snakeBody, c.snakeTail, if (n <= 2) 0f else (i - 1).toFloat() / (n - 2))
            }
            val x = p.x * cell + inset
            val y = p.y * cell + inset
            val s = cell - inset * 2
            drawRect(color, Offset(x, y), Size(s, s))
            if (i == 0) drawEyes(state.direction, x, y, s, c.deep)
        }
    }
}

private fun DrawScope.drawEyes(dir: Direction, x: Float, y: Float, s: Float, color: Color) {
    val e = (s * 0.2f).coerceAtLeast(2.dp.toPx())
    val near = s * 0.18f
    val far = s - near - e
    val (a, b) = when (dir) {
        Direction.RIGHT -> Offset(far, near) to Offset(far, far)
        Direction.LEFT -> Offset(near, near) to Offset(near, far)
        Direction.UP -> Offset(near, near) to Offset(far, near)
        Direction.DOWN -> Offset(near, far) to Offset(far, far)
    }
    drawRect(color, Offset(x + a.x, y + a.y), Size(e, e))
    drawRect(color, Offset(x + b.x, y + b.y), Size(e, e))
}

@Composable
private fun DPad(onPress: (Direction) -> Unit) {
    val c = LocalRetro.current
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Spacer(Modifier.width(64.dp))
            PixelKey(Direction.UP, onPress, Modifier.width(64.dp))
            Spacer(Modifier.width(64.dp))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PixelKey(Direction.LEFT, onPress, Modifier.width(64.dp))
            Box(Modifier.width(64.dp).height(70.dp)) {
                Box(Modifier.size(64.dp).clip(Notch6).background(c.border.copy(alpha = 0.5f)).padding(3.dp).clip(Notch3).background(c.bg))
            }
            PixelKey(Direction.RIGHT, onPress, Modifier.width(64.dp))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Spacer(Modifier.width(64.dp))
            PixelKey(Direction.DOWN, onPress, Modifier.width(64.dp))
            Spacer(Modifier.width(64.dp))
        }
    }
}


@Composable
private fun PauseOverlay(vm: GameViewModel, onResume: () -> Unit, onRestart: () -> Unit, onQuit: () -> Unit) {
    val c = LocalRetro.current
    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xDB08070C))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        PixelPanel(Modifier.widthIn(max = 440.dp).fillMaxWidth().semantics { paneTitle = "Paused" }) {
            Column(Modifier.padding(start = 22.dp, end = 22.dp, top = 28.dp, bottom = 26.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.blinking()) {
                    ExtrudedText("PAUSED", 26, c.text, c.accentDark, null, step = 3.dp)
                }
                Spacer(Modifier.height(10.dp))
                val s = vm.state.apples.toString().padStart(3, '0')
                val hi = maxOf(vm.currentBest, vm.state.apples).toString().padStart(3, '0')
                MonoText("Score $s · Hi $hi · ${vm.activeDifficultyValue.label.lowercase().replaceFirstChar { it.uppercase() }}", 13, c.muted)
                Spacer(Modifier.height(22.dp))
                PixelButton(onClick = onResume, modifier = Modifier.fillMaxWidth(), height = 68.dp, border = c.accentDark, face = c.accent) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        TriangleIcon(true, c.onAccent, 16.dp, 20.dp)
                        PixelText("RESUME", 18, c.onAccent)
                    }
                }
                Spacer(Modifier.height(14.dp))
                PixelButton(onClick = onRestart, modifier = Modifier.fillMaxWidth(), height = 56.dp, face = c.bg) {
                    PixelText("RESTART", 14, c.text)
                }
                Spacer(Modifier.height(14.dp))
                PixelButton(onClick = onQuit, modifier = Modifier.fillMaxWidth(), height = 56.dp, face = c.bg) {
                    PixelText("QUIT TO MENU", 14, c.text)
                }
            }
        }
        }
    }
}

@Composable
private fun GameOverContent(r: RunResult, vm: GameViewModel, onAgain: () -> Unit, onMenu: () -> Unit, onStats: () -> Unit) {
    val c = LocalRetro.current
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
    BoxWithConstraints(Modifier.fillMaxHeight().widthIn(max = 520.dp).fillMaxWidth()) {
    // Scrolls on short windows; otherwise the buttons stay pinned to the bottom.
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
    Column(
        Modifier.fillMaxWidth().defaultMinSize(minHeight = maxHeight).padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            if (r.cleared) {
                ExtrudedText("YOU WIN!", 32, c.accent, c.accentDark, c.shadow, step = 3.dp)
            } else {
                ExtrudedText("GAME OVER", 32, c.danger, c.dangerDark, c.shadow, step = 3.dp)
            }
            if (r.newBest) {
                Box(Modifier.blinking().background(c.accent).padding(horizontal = 14.dp, vertical = 8.dp)) {
                    PixelText("NEW HIGH SCORE", 11, c.onAccent)
                }
            }
            PixelPanel(Modifier.fillMaxWidth(), fill = c.deep) {
                Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 22.dp, bottom = 20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    MonoText("SCORE", 12, c.muted, letterSpacing = 1.9f)
                    LcdDigits(r.record.score, 64, c.data)
                    val hi = if (r.newBest) r.prevBest else maxOf(r.prevBest, r.record.score)
                    MonoText(if (r.newBest) "Previous hi-score ${hi.toString().padStart(3, '0')}" else "Hi-score ${hi.toString().padStart(3, '0')}", 13, c.muted)
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatTile("LENGTH", r.length.toString(), Modifier.weight(1f))
                StatTile("TIME", StatsCalculator.formatDuration(r.record.durationMs), Modifier.weight(1f))
                StatTile("TOP SPEED", r.record.topSpeed.toString(), Modifier.weight(1f))
            }
        }
        Column(Modifier.padding(top = 20.dp), verticalArrangement = Arrangement.spacedBy(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            PixelButton(onClick = onAgain, modifier = Modifier.fillMaxWidth(), height = 72.dp, border = c.accentDark, face = c.accent) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    TriangleIcon(true, c.onAccent, 16.dp, 20.dp)
                    PixelText("PLAY AGAIN", 18, c.onAccent)
                }
            }
            PixelButton(onClick = onMenu, modifier = Modifier.fillMaxWidth(), height = 56.dp) {
                PixelText("MENU", 16, c.text)
            }
            Box(
                Modifier.height(44.dp).clickable(onClick = onStats).padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                androidx.compose.material3.Text(
                    "See all stats",
                    color = c.muted,
                    fontSize = 14.sp,
                    fontFamily = com.king_grey.retro_snake.ui.theme.MonoFont,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                    textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline,
                )
            }
        }
    }
    }
    }
    }
}

@Composable
fun StatTile(label: String, value: String, modifier: Modifier = Modifier) {
    val c = LocalRetro.current
    PixelPanel(modifier) {
        Column(Modifier.padding(10.dp)) {
            MonoText(label, 12, c.muted, letterSpacing = 0.96f, lineHeight = 16)
            Spacer(Modifier.height(8.dp))
            PixelText(value, 15, c.text)
        }
    }
}
