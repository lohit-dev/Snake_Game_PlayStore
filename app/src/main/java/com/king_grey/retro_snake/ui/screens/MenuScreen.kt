package com.king_grey.retro_snake.ui.screens

import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.king_grey.retro_snake.game.Difficulty
import com.king_grey.retro_snake.ui.GameViewModel
import com.king_grey.retro_snake.ui.components.CrawlLaneCanvas
import com.king_grey.retro_snake.ui.components.ExtrudedText
import com.king_grey.retro_snake.ui.components.MonoText
import com.king_grey.retro_snake.ui.components.Notch3
import com.king_grey.retro_snake.ui.components.Notch6
import com.king_grey.retro_snake.ui.components.PixelButton
import com.king_grey.retro_snake.ui.components.PixelPanel
import com.king_grey.retro_snake.ui.components.PixelText
import com.king_grey.retro_snake.ui.components.RetroScreen
import com.king_grey.retro_snake.ui.components.ArrowIcon
import com.king_grey.retro_snake.ui.components.TriangleIcon
import com.king_grey.retro_snake.ui.components.blinking
import com.king_grey.retro_snake.ui.components.rememberTick
import com.king_grey.retro_snake.ui.theme.FocusCream
import com.king_grey.retro_snake.ui.theme.LocalReduceMotion
import com.king_grey.retro_snake.ui.theme.LocalRetro
import com.king_grey.retro_snake.game.Direction

private val MASCOT = listOf(
    "....r..r....",
    ".....rr.....",
    ".....rr.....",
    "..oooooooo..",
    ".oggggggggo.",
    ".ogwwggwwgo.",
    ".ogwkggkwgo.",
    ".oggggggggo.",
    ".oggdggdggo.",
    "..oggggggo..",
    "...oooooo...",
)

fun appVersion(ctx: Context): String = try {
    ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionName ?: ""
} catch (e: Exception) {
    ""
}

@Composable
fun MenuScreen(
    vm: GameViewModel,
    onPlay: () -> Unit,
    onStats: () -> Unit,
    onSettings: () -> Unit,
    onHelp: () -> Unit,
) {
    val c = LocalRetro.current
    val settings = vm.settings
    val version = appVersion(LocalContext.current)
    var sel by remember { mutableIntStateOf(0) }
    val levels = Difficulty.values()
    val best = vm.bests[settings.difficulty] ?: 0

    RetroScreen {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val compact = maxHeight < 720.dp
            Column(
                Modifier.fillMaxSize().padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 20.dp),
            ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        PixelText("HI-SCORE", 10, c.muted, letterSpacing = 1.2f)
                        PixelText(best.coerceAtMost(999).toString().padStart(3, '0'), 14, c.accent)
                    }
                    if (version.isNotEmpty()) MonoText("v$version", 12, c.muted)
                }

                Column(Modifier.fillMaxWidth().padding(top = if (compact) 12.dp else 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    if (!compact) {
                        Mascot()
                        Spacer(Modifier.height(14.dp))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(Modifier.size(28.dp, 4.dp).background(c.accent))
                        PixelText("RETRO", 12, c.accent, letterSpacing = 3.6f)
                        Box(Modifier.size(28.dp, 4.dp).background(c.accent))
                    }
                    Spacer(Modifier.height(10.dp))
                    ExtrudedText("SNAKE", 54, c.text, c.accentDark, c.shadow)
                }

                if (!compact) {
                    PixelPanel(Modifier.fillMaxWidth().padding(top = 16.dp), fill = c.deep) {
                        CrawlLaneCanvas(Modifier.fillMaxWidth().height(38.dp))
                    }
                }

                Row(
                    Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    PixelButton(
                        onClick = {
                            val i = (settings.difficulty.ordinal + levels.size - 1) % levels.size
                            vm.update { it.copy(difficulty = levels[i]) }
                        },
                        modifier = Modifier.width(56.dp),
                        height = 64.dp,
                        description = "Easier difficulty",
                    ) { ArrowIcon(Direction.LEFT, c.text, 20.dp) }
                    PixelPanel(
                        Modifier.weight(1f).height(64.dp).semantics { liveRegion = LiveRegionMode.Polite },
                        fill = c.deep,
                    ) {
                        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically), horizontalAlignment = Alignment.CenterHorizontally) {
                            MonoText("DIFFICULTY", 11, c.muted, letterSpacing = 1.8f, lineHeight = 11)
                            PixelText(settings.difficulty.label, 14, c.data)
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                for (i in levels.indices) {
                                    Box(Modifier.size(8.dp).background(if (i <= settings.difficulty.ordinal) c.data else c.border.copy(alpha = 0.5f)))
                                }
                            }
                        }
                    }
                    PixelButton(
                        onClick = {
                            val i = (settings.difficulty.ordinal + 1) % levels.size
                            vm.update { it.copy(difficulty = levels[i]) }
                        },
                        modifier = Modifier.width(56.dp),
                        height = 64.dp,
                        description = "Harder difficulty",
                    ) { ArrowIcon(Direction.RIGHT, c.text, 20.dp) }
                }

                Spacer(Modifier.weight(1f))

                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    MenuButton("PLAY", 72.dp, 22, primary = true, selected = sel == 0, onActive = { sel = 0 }, onClick = onPlay)
                    MenuButton("STATS", 56.dp, 16, primary = false, selected = sel == 1, onActive = { sel = 1 }, onClick = onStats)
                    MenuButton("SETTINGS", 56.dp, 16, primary = false, selected = sel == 2, onActive = { sel = 2 }, onClick = onSettings)
                    MenuButton("HOW TO PLAY", 56.dp, 16, primary = false, selected = sel == 3, onActive = { sel = 3 }, onClick = onHelp)
                }
            }
        }
    }
}

@Composable
private fun MenuButton(
    label: String,
    height: androidx.compose.ui.unit.Dp,
    fontSize: Int,
    primary: Boolean,
    selected: Boolean,
    onActive: () -> Unit,
    onClick: () -> Unit,
) {
    val c = LocalRetro.current
    val border = when {
        primary && selected -> Color(0xFFFFF1C9)
        primary -> c.accentDark
        selected -> FocusCream
        else -> c.border
    }
    PixelButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        height = height,
        border = border,
        face = if (primary) c.accent else c.panel,
        onActive = onActive,
    ) {
        val fg = if (primary) c.onAccent else c.text
        if (selected) {
            Box(Modifier.align(Alignment.CenterStart).padding(start = 17.dp).blinking()) {
                TriangleIcon(true, if (primary) c.onAccent else c.accent)
            }
        }
        PixelText(label, fontSize, fg)
    }
}

@Composable
private fun Mascot() {
    val c = LocalRetro.current
    val reduce = LocalReduceMotion.current
    val tick = rememberTick(250)
    val tongue = c.danger
    val dark = lerp(c.snakeTail, c.deep, 0.45f)
    val palette = mapOf(
        'o' to c.snakeTail, 'g' to c.snakeBody, 'w' to c.text,
        'k' to c.deep, 'd' to dark, 'r' to tongue,
    )
    Canvas(Modifier.size(120.dp, 110.dp).semantics { contentDescription = "" }) {
        val u = 10.dp.toPx()
        val t = tick.value
        val bob = if (!reduce && (t / 2) % 2 == 1) -6.dp.toPx() else 0f
        val flick = reduce || (t % 8) >= 5
        MASCOT.forEachIndexed { y, row ->
            row.forEachIndexed { x, ch ->
                if (ch == '.') return@forEachIndexed
                if (ch == 'r' && !flick) return@forEachIndexed
                drawRect(palette.getValue(ch), Offset(x * u, y * u + bob), Size(u, u))
            }
        }
    }
}
