package com.king_grey.retro_snake.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.king_grey.retro_snake.data.ThemeId

@Immutable
data class RetroColors(
    val bg: Color,
    val deep: Color,
    val shadow: Color,
    val panel: Color,
    val border: Color,
    val dots: Color,
    val text: Color,
    val muted: Color,
    val accent: Color,
    val accentDark: Color,
    val data: Color,
    val danger: Color,
    val dangerDark: Color,
    val snakeTail: Color,
    val snakeHead: Color,
    val snakeBody: Color,
    val food: Color,
    val onAccent: Color,
)

object Palettes {
    val Night = RetroColors(
        bg = Color(0xFF1A1822), deep = Color(0xFF100F16), shadow = Color(0xFF08070C),
        panel = Color(0xFF2A2735), border = Color(0xFF4A4660), dots = Color(0xFF24212F),
        text = Color(0xFFF6EFDF), muted = Color(0xFFA9A4BC),
        accent = Color(0xFFF2B544), accentDark = Color(0xFFB8802A),
        data = Color(0xFF7CC4FF), danger = Color(0xFFF07F66), dangerDark = Color(0xFF7A3A2C),
        snakeTail = Color(0xFF6F8444), snakeBody = Color(0xFFA8C26A), snakeHead = Color(0xFFCFE48F),
        food = Color(0xFFF2B544), onAccent = Color(0xFF1A1822),
    )
    val Amber = Night.copy(
        bg = Color(0xFF1B1206), deep = Color(0xFF110B03), shadow = Color(0xFF080501),
        panel = Color(0xFF2C1F0D), border = Color(0xFF5C4420), dots = Color(0xFF2A1D0B),
        text = Color(0xFFFFE9B8), muted = Color(0xFFC2A56A),
        accent = Color(0xFFFFB000), accentDark = Color(0xFFA86F00),
        data = Color(0xFFFFD36B), danger = Color(0xFFF07F66),
        snakeTail = Color(0xFF9A6A00), snakeBody = Color(0xFFF2B544), snakeHead = Color(0xFFFFE08A),
        food = Color(0xFFF07F66), onAccent = Color(0xFF1B1206),
    )
    val Mono = Night.copy(
        bg = Color(0xFF111111), deep = Color(0xFF0A0A0A), shadow = Color(0xFF000000),
        panel = Color(0xFF222222), border = Color(0xFF555555), dots = Color(0xFF1E1E1E),
        text = Color(0xFFF2F2F2), muted = Color(0xFF9A9A9A),
        accent = Color(0xFFE8E8E8), accentDark = Color(0xFF8C8C8C),
        data = Color(0xFFCFCFCF), danger = Color(0xFFD9D9D9), dangerDark = Color(0xFF666666),
        snakeTail = Color(0xFF8A8A8A), snakeBody = Color(0xFFE8E8E8), snakeHead = Color(0xFFFFFFFF),
        food = Color(0xFF8C8C8C), onAccent = Color(0xFF111111),
    )
    val Ice = Night.copy(
        bg = Color(0xFF14232E), deep = Color(0xFF0C1720), shadow = Color(0xFF050B10),
        panel = Color(0xFF1F3443), border = Color(0xFF3F6075), dots = Color(0xFF1B2F3D),
        text = Color(0xFFEAF6FC), muted = Color(0xFF9DB8C6),
        accent = Color(0xFF8FD3F0), accentDark = Color(0xFF4A8BA6),
        data = Color(0xFFBFE8F8), danger = Color(0xFFF07F66),
        snakeTail = Color(0xFF3E8CAA), snakeBody = Color(0xFF8FD3F0), snakeHead = Color(0xFFD6F2FC),
        food = Color(0xFFF2B544), onAccent = Color(0xFF0C1720),
    )

    fun of(id: ThemeId): RetroColors = when (id) {
        ThemeId.NIGHT -> Night
        ThemeId.AMBER -> Amber
        ThemeId.MONO -> Mono
        ThemeId.ICE -> Ice
    }
}

/** Cream used for focus rings and selected borders; kept constant across themes. */
val FocusCream = Color(0xFFF6EFDF)
