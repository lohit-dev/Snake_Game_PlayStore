package com.king_grey.retro_snake.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.king_grey.retro_snake.game.Direction
import com.king_grey.retro_snake.ui.theme.FocusCream
import com.king_grey.retro_snake.ui.theme.LocalClick
import com.king_grey.retro_snake.ui.theme.LocalReduceMotion
import com.king_grey.retro_snake.ui.theme.LocalRetro
import com.king_grey.retro_snake.ui.theme.LocalSettings
import com.king_grey.retro_snake.ui.theme.MonoFont
import com.king_grey.retro_snake.ui.theme.PixelFont
import kotlin.math.hypot
import kotlinx.coroutines.delay

/** Rectangle with stair-stepped corners, the pixel-art "notch". */
class NotchedShape(private val step: Dp) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val s = with(density) { step.toPx() }
        val w = size.width
        val h = size.height
        val p = Path().apply {
            moveTo(0f, s); lineTo(s, s); lineTo(s, 0f)
            lineTo(w - s, 0f); lineTo(w - s, s); lineTo(w, s)
            lineTo(w, h - s); lineTo(w - s, h - s); lineTo(w - s, h)
            lineTo(s, h); lineTo(s, h - s); lineTo(0f, h - s)
            close()
        }
        return Outline.Generic(p)
    }
}

val Notch6 = NotchedShape(6.dp)
val Notch3 = NotchedShape(3.dp)

// ---------- text ----------

@Composable
fun PixelText(
    text: String,
    size: Int,
    color: Color,
    modifier: Modifier = Modifier,
    align: TextAlign? = null,
    letterSpacing: Float = 0f,
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        fontFamily = PixelFont,
        fontSize = size.sp,
        lineHeight = (size + 4).sp,
        letterSpacing = letterSpacing.sp,
        textAlign = align,
        maxLines = 1,
        softWrap = false,
    )
}

@Composable
fun MonoText(
    text: String,
    size: Int,
    color: Color,
    modifier: Modifier = Modifier,
    weight: FontWeight = FontWeight.Normal,
    align: TextAlign? = null,
    letterSpacing: Float = 0f,
    lineHeight: Int = size + 5,
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        fontFamily = MonoFont,
        fontWeight = weight,
        fontSize = size.sp,
        lineHeight = lineHeight.sp,
        letterSpacing = letterSpacing.sp,
        textAlign = align,
    )
}

/** Pixel text with a hard stacked drop-shadow, like the SNAKE title. */
@Composable
fun ExtrudedText(
    text: String,
    size: Int,
    color: Color,
    mid: Color,
    back: Color?,
    modifier: Modifier = Modifier,
    step: Dp = 3.dp,
) {
    Box(modifier.padding(end = step * 2, bottom = step * 2)) {
        if (back != null) PixelText(text, size, back, Modifier.offset(step * 2, step * 2))
        PixelText(text, size, mid, Modifier.offset(step, step))
        PixelText(text, size, color)
    }
}

// ---------- background / overlays ----------

fun Modifier.dotGrid(color: Color, enabled: Boolean = true, spacing: Dp = 16.dp, start: Dp = 8.dp, dot: Dp = 2.dp): Modifier =
    if (!enabled) this else this.drawWithCache {
        val sp = spacing.toPx()
        val off = start.toPx()
        val pts = ArrayList<Offset>()
        var y = off
        while (y < size.height) {
            var x = off
            while (x < size.width) {
                pts.add(Offset(x, y))
                x += sp
            }
            y += sp
        }
        onDrawBehind {
            drawPoints(pts, PointMode.Points, color, strokeWidth = dot.toPx(), cap = StrokeCap.Square)
        }
    }

@Composable
fun CrtOverlay() {
    Canvas(Modifier.fillMaxSize()) {
        val line = Color.Black.copy(alpha = 0.16f)
        val h = 1.dp.toPx()
        val stepPx = 3.dp.toPx()
        var y = 0f
        while (y < size.height) {
            drawRect(line, topLeft = Offset(0f, y), size = Size(size.width, h))
            y += stepPx
        }
        drawRect(
            brush = Brush.radialGradient(
                colorStops = arrayOf(0f to Color.Transparent, 0.62f to Color.Transparent, 1f to Color.Black.copy(alpha = 0.38f)),
                center = center,
                radius = hypot(size.width / 2f, size.height / 2f),
            ),
        )
    }
}

/** Full-screen retro backdrop: palette colour, dot grid, safe-area padding and optional CRT overlay. */
@Composable
fun RetroScreen(maxContentWidth: Dp = 520.dp, content: @Composable BoxScope.() -> Unit) {
    val c = LocalRetro.current
    val s = LocalSettings.current
    Box(Modifier.fillMaxSize().background(c.bg).dotGrid(c.dots, s.grid), contentAlignment = Alignment.TopCenter) {
        Box(
            Modifier.fillMaxHeight().widthIn(max = maxContentWidth).fillMaxWidth().windowInsetsPadding(WindowInsets.safeDrawing),
            content = content,
        )
        if (s.crt) CrtOverlay()
    }
}

// ---------- animation helpers ----------

/** A counter that increments every [intervalMs]; frozen at 0 when the user reduces motion. Read it in draw/graphicsLayer. */
@Composable
fun rememberTick(intervalMs: Long): State<Int> {
    val reduce = LocalReduceMotion.current
    val tick = remember { mutableIntStateOf(0) }
    LaunchedEffect(reduce, intervalMs) {
        if (!reduce) {
            while (true) {
                delay(intervalMs)
                tick.intValue = tick.intValue + 1
            }
        }
    }
    return tick
}

/** Hard on/off blink (no fade), like a console cursor. */
@Composable
fun Modifier.blinking(periodMs: Long = 1000): Modifier {
    val reduce = LocalReduceMotion.current
    if (reduce) return this
    val tick = rememberTick(periodMs / 2)
    return this.graphicsLayer { alpha = if (tick.value % 2 == 0) 1f else 0f }
}

// ---------- panels & buttons ----------

@Composable
fun PixelPanel(
    modifier: Modifier = Modifier,
    border: Color = LocalRetro.current.border,
    fill: Color = LocalRetro.current.panel,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content: @Composable BoxScope.() -> Unit,
) {
    Box(modifier.clip(Notch6).background(border).padding(3.dp), propagateMinConstraints = true) {
        Box(Modifier.clip(Notch3).background(fill).padding(contentPadding), content = content)
    }
}

@Composable
private fun PixelFace(
    modifier: Modifier,
    height: Dp,
    pressed: Boolean,
    border: Color,
    face: Color,
    interaction: Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val c = LocalRetro.current
    Box(modifier.height(height + 6.dp)) {
        Box(Modifier.fillMaxWidth().height(height).offset(y = 6.dp).clip(Notch6).background(c.shadow))
        Box(
            Modifier
                .fillMaxWidth()
                .height(height)
                .offset(y = if (pressed) 4.dp else 0.dp)
                .clip(Notch6)
                .background(border)
                .then(interaction)
                .padding(3.dp)
                .clip(Notch3)
                .background(face),
            contentAlignment = Alignment.Center,
            content = content,
        )
    }
}

@Composable
fun PixelButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 56.dp,
    border: Color = LocalRetro.current.border,
    face: Color = LocalRetro.current.panel,
    description: String? = null,
    onActive: () -> Unit = {},
    content: @Composable BoxScope.() -> Unit,
) {
    val src = remember { MutableInteractionSource() }
    val pressed by src.collectIsPressedAsState()
    val focused by src.collectIsFocusedAsState()
    val click = LocalClick.current
    LaunchedEffect(pressed, focused) { if (pressed || focused) onActive() }
    val interaction = Modifier
        .then(if (description != null) Modifier.semantics { contentDescription = description } else Modifier)
        .clickable(interactionSource = src, indication = null, role = Role.Button) {
            click()
            onClick()
        }
    PixelFace(modifier, height, pressed, if (focused) FocusCream else border, face, interaction, content)
}

/** D-pad key: fires on touch-down (not release) so turns feel instant. */
@Composable
fun PixelKey(dir: Direction, onPress: (Direction) -> Unit, modifier: Modifier = Modifier) {
    val c = LocalRetro.current
    var pressed by remember { mutableStateOf(false) }
    val name = dir.name.lowercase().replaceFirstChar { it.uppercase() }
    val interaction = Modifier
        .semantics(mergeDescendants = true) {
            contentDescription = name
            role = Role.Button
            onClick { onPress(dir); true }
        }
        .pointerInput(dir) {
            detectTapGestures(onPress = {
                pressed = true
                onPress(dir)
                try {
                    tryAwaitRelease()
                } finally {
                    pressed = false
                }
            })
        }
    PixelFace(modifier, 64.dp, pressed, c.border, c.panel, interaction) {
        ArrowIcon(dir, c.text, 26.dp)
    }
}

// ---------- icons ----------

@Composable
fun ArrowIcon(dir: Direction, color: Color, size: Dp) {
    Canvas(Modifier.size(size)) {
        val k = this.size.width / 24f
        val pts = when (dir) {
            Direction.UP -> listOf(5f to 15f, 12f to 8f, 19f to 15f)
            Direction.DOWN -> listOf(5f to 9f, 12f to 16f, 19f to 9f)
            Direction.LEFT -> listOf(15f to 5f, 8f to 12f, 15f to 19f)
            Direction.RIGHT -> listOf(9f to 5f, 16f to 12f, 9f to 19f)
        }
        val path = Path().apply {
            moveTo(pts[0].first * k, pts[0].second * k)
            lineTo(pts[1].first * k, pts[1].second * k)
            lineTo(pts[2].first * k, pts[2].second * k)
        }
        drawPath(path, color, style = Stroke(width = 3.5f * k, cap = StrokeCap.Square, join = StrokeJoin.Miter))
    }
}

@Composable
fun TriangleIcon(pointRight: Boolean, color: Color, width: Dp = 14.dp, height: Dp = 18.dp) {
    Canvas(Modifier.size(width, height)) {
        val w = size.width
        val h = size.height
        val p = Path().apply {
            if (pointRight) { moveTo(0f, 0f); lineTo(w, h / 2f); lineTo(0f, h) } else { moveTo(w, 0f); lineTo(0f, h / 2f); lineTo(w, h) }
            close()
        }
        drawPath(p, color)
    }
}

@Composable
fun CloseIcon(color: Color, size: Dp = 20.dp) {
    Canvas(Modifier.size(size)) {
        val k = this.size.width / 24f
        val st = Stroke(width = 3.5f * k, cap = StrokeCap.Square)
        drawLine(color, Offset(5f * k, 5f * k), Offset(19f * k, 19f * k), strokeWidth = st.width, cap = StrokeCap.Square)
        drawLine(color, Offset(19f * k, 5f * k), Offset(5f * k, 19f * k), strokeWidth = st.width, cap = StrokeCap.Square)
    }
}

@Composable
fun PauseIcon(color: Color, size: Dp = 20.dp) {
    Canvas(Modifier.size(size)) {
        val k = this.size.width / 20f
        drawRect(color, Offset(4f * k, 3f * k), Size(4f * k, 14f * k))
        drawRect(color, Offset(12f * k, 3f * k), Size(4f * k, 14f * k))
    }
}

// ---------- readouts ----------

/** Seven-segment style readout: faint "888" ghost under the live value. */
@Composable
fun LcdDigits(value: Int, size: Int, color: Color, modifier: Modifier = Modifier) {
    val shown = value.coerceIn(0, 999).toString().padStart(3, '0')
    Box(modifier) {
        PixelText("888", size, color.copy(alpha = 0.1f))
        PixelText(shown, size, color)
    }
}

/** Horizontal or vertical bar made of little segments (6dp on, 2dp off). */
@Composable
fun SegBar(fraction: Float, color: Color, modifier: Modifier = Modifier, vertical: Boolean = false) {
    Canvas(modifier) {
        val on = 6.dp.toPx()
        val gap = 2.dp.toPx()
        val total = if (vertical) size.height else size.width
        val filled = total * fraction.coerceIn(0f, 1f)
        var p = 0f
        while (p < filled) {
            val len = minOf(on, filled - p)
            if (vertical) {
                drawRect(color, Offset(0f, size.height - p - len), Size(size.width, len))
            } else {
                drawRect(color, Offset(p, 0f), Size(len, size.height))
            }
            p += on + gap
        }
    }
}

/** The little snake crawling toward an apple on the menu. */
@Composable
fun CrawlLaneCanvas(modifier: Modifier = Modifier) {
    val c = LocalRetro.current
    val reduce = LocalReduceMotion.current
    val tick = rememberTick(250)
    Canvas(modifier) {
        val cell = 16.dp.toPx()
        val seg = 14.dp.toPx()
        val cols = (size.width / cell).toInt() + 1
        val cycle = cols + 12
        val head = if (reduce) cols / 2 else tick.value % cycle
        val appleCol = cols - 5
        val y = (size.height - seg) / 2f
        // dots
        var dy = 8.dp.toPx()
        while (dy < size.height) {
            var dx = 8.dp.toPx()
            while (dx < size.width) {
                drawRect(c.border.copy(alpha = 0.35f), Offset(dx, dy), Size(2.dp.toPx(), 2.dp.toPx()))
                dx += cell
            }
            dy += cell
        }
        if (head < appleCol) {
            val ax = appleCol * cell + 1.dp.toPx() + 2.dp.toPx()
            rotate(45f, pivot = Offset(ax + 5.dp.toPx(), y + 7.dp.toPx())) {
                drawRect(c.food, Offset(ax, y + 2.dp.toPx()), Size(10.dp.toPx(), 10.dp.toPx()))
            }
        }
        for (i in 9 downTo 0) {
            val col = head - i
            if (col < -1 || col > cols) continue
            val x = col * cell + 1.dp.toPx()
            val color = if (i == 0) c.snakeHead else androidx.compose.ui.graphics.lerp(c.snakeBody, c.snakeTail, i / 9f)
            drawRect(color, Offset(x, y), Size(seg, seg))
            if (i == 0) {
                drawRect(c.deep, Offset(x + 8.dp.toPx(), y + 3.dp.toPx()), Size(3.dp.toPx(), 3.dp.toPx()))
                drawRect(c.deep, Offset(x + 8.dp.toPx(), y + 8.dp.toPx()), Size(3.dp.toPx(), 3.dp.toPx()))
            }
        }
    }
}
