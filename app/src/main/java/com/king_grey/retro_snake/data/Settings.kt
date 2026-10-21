package com.king_grey.retro_snake.data

import com.king_grey.retro_snake.game.Difficulty

enum class ControlMode(val label: String) { DPAD("D-PAD"), SWIPE("SWIPE"), BOTH("BOTH") }

enum class ThemeId(val label: String) { NIGHT("Night"), AMBER("Amber"), MONO("Mono"), ICE("Ice") }

data class Settings(
    val difficulty: Difficulty = Difficulty.NORMAL,
    val controls: ControlMode = ControlMode.BOTH,
    val sound: Boolean = true,
    val haptics: Boolean = true,
    val grid: Boolean = true,
    val crt: Boolean = true,
    val walls: Boolean = false,
    val theme: ThemeId = ThemeId.NIGHT,
)
