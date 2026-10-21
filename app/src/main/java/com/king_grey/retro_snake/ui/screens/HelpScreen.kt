package com.king_grey.retro_snake.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.king_grey.retro_snake.ui.GameViewModel
import com.king_grey.retro_snake.ui.components.CloseIcon
import com.king_grey.retro_snake.ui.components.MonoText
import com.king_grey.retro_snake.ui.components.PixelButton
import com.king_grey.retro_snake.ui.components.PixelPanel
import com.king_grey.retro_snake.ui.components.PixelText
import com.king_grey.retro_snake.ui.components.RetroScreen
import com.king_grey.retro_snake.ui.theme.LocalRetro

@Composable
fun HelpScreen(vm: GameViewModel, onBack: () -> Unit) {
    val c = LocalRetro.current
    val walls = vm.settings.walls
    RetroScreen {
        Column(Modifier.fillMaxSize().padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 20.dp)) {
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                ScreenTitle("HOW TO PLAY", size = 18) {
                    PixelButton(onClick = onBack, modifier = Modifier.size(48.dp, 54.dp), height = 48.dp, description = "Close") {
                        CloseIcon(c.text)
                    }
                }
                Spacer(Modifier.height(16.dp))
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    HelpCard("STEER", "Swipe or tap the D-pad to turn. You can't U-turn into yourself.", "A snake heading right with an arrow showing it turning up") { steerArt(it) }
                    HelpCard("EAT", "Every apple adds a point, a longer tail and a little more speed.", "A snake next to an apple marked plus one") { eatArt(it) }
                    HelpCard(
                        "SURVIVE",
                        if (walls) "Bump into your own body or a wall and the run ends." else "Bump into your own body and the run ends. Edges wrap around.",
                        "A snake whose head has run into its own body",
                    ) { surviveArt(it) }
                }
                Row(Modifier.padding(top = 18.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PixelText("TIP", 10, c.accent, letterSpacing = 1f)
                    MonoText("Plan two turns ahead. The longer you get, the less room to dodge.", 13, c.muted, Modifier.weight(1f), lineHeight = 19)
                }
                Spacer(Modifier.height(10.dp))
            }
            Spacer(Modifier.height(14.dp))
            PixelButton(onClick = onBack, modifier = Modifier.fillMaxWidth(), height = 72.dp, border = c.accentDark, face = c.accent) {
                PixelText("GOT IT", 20, c.onAccent)
            }
        }
    }
}

@Composable
private fun HelpCard(title: String, body: String, art: String, draw: DrawScope.(Palette) -> Unit) {
    val c = LocalRetro.current
    val p = Palette(c.snakeTail, c.snakeBody, c.snakeHead, c.food, c.danger, c.accent, c.deep, c.border)
    PixelPanel(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(Modifier.size(140.dp, 100.dp).semantics { contentDescription = art }) {
                Canvas(Modifier.size(140.dp, 100.dp)) {
                    drawRect(p.deep)
                    // dot grid, 20dp pitch, offset 10dp
                    var y = 10.dp.toPx()
                    while (y < size.height) {
                        var x = 10.dp.toPx()
                        while (x < size.width) {
                            drawRect(p.border.copy(alpha = 0.5f), Offset(x - 1.dp.toPx(), y - 1.dp.toPx()), Size(2.dp.toPx(), 2.dp.toPx()))
                            x += 20.dp.toPx()
                        }
                        y += 20.dp.toPx()
                    }
                    draw(p)
                    // inset frame
                    val t = 2.dp.toPx()
                    drawRect(p.border, Offset(0f, 0f), Size(size.width, t))
                    drawRect(p.border, Offset(0f, size.height - t), Size(size.width, t))
                    drawRect(p.border, Offset(0f, 0f), Size(t, size.height))
                    drawRect(p.border, Offset(size.width - t, 0f), Size(t, size.height))
                }
                if (title == "EAT") {
                    PixelText("+1", 12, p.accent, Modifier.offset(96.dp, 14.dp))
                }
            }
            Column(Modifier.weight(1f)) {
                PixelText(title, 12, c.accent)
                Spacer(Modifier.height(8.dp))
                MonoText(body, 13, c.text, lineHeight = 19)
            }
        }
    }
}

class Palette(
    val tail: Color, val body: Color, val head: Color, val food: Color,
    val danger: Color, val accent: Color, val deep: Color, val border: Color,
)

private fun DrawScope.seg(x: Float, y: Float, color: Color, s: Float = 18f) {
    drawRect(color, Offset(x.dp.toPx(), y.dp.toPx()), Size(s.dp.toPx(), s.dp.toPx()))
}

private fun DrawScope.steerArt(p: Palette) {
    seg(1f, 61f, p.tail)
    seg(21f, 61f, lerp(p.tail, p.body, 0.33f))
    seg(41f, 61f, p.body)
    seg(61f, 61f, p.head)
    val k = 1.dp.toPx()
    val path = Path().apply {
        moveTo(70f * k, 52f * k); lineTo(70f * k, 24f * k)
        moveTo(61f * k, 33f * k); lineTo(70f * k, 24f * k); lineTo(79f * k, 33f * k)
    }
    drawPath(path, p.accent, style = Stroke(width = 4f * k, cap = StrokeCap.Square, join = StrokeJoin.Miter))
}

private fun DrawScope.eatArt(p: Palette) {
    seg(1f, 41f, p.tail)
    seg(21f, 41f, p.body)
    seg(41f, 41f, p.head)
    val s = 14.dp.toPx()
    val cx = 103.dp.toPx() + s / 2
    val cy = 43.dp.toPx() + s / 2
    rotate(45f, pivot = Offset(cx, cy)) {
        drawRect(p.food, Offset(cx - s / 2, cy - s / 2), Size(s, s))
    }
}

private fun DrawScope.surviveArt(p: Palette) {
    seg(21f, 21f, p.tail)
    seg(41f, 21f, lerp(p.tail, p.body, 0.2f))
    // red outline around the segment the head runs into
    val o = 2.dp.toPx()
    drawRect(p.danger, Offset(41.dp.toPx() - 1.dp.toPx() - o, 21.dp.toPx() - 1.dp.toPx() - o), Size(20.dp.toPx() + 2 * o, 20.dp.toPx() + 2 * o))
    seg(41f, 21f, lerp(p.tail, p.body, 0.2f))
    seg(61f, 21f, lerp(p.tail, p.body, 0.4f))
    seg(81f, 21f, lerp(p.tail, p.body, 0.6f))
    seg(81f, 41f, lerp(p.tail, p.body, 0.8f))
    seg(81f, 61f, p.body)
    seg(61f, 61f, p.body)
    seg(41f, 61f, p.body)
    seg(41f, 41f, p.danger)
}
