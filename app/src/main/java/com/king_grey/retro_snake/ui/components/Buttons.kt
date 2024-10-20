package com.king_grey.retro_snake.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.king_grey.retro_snake.ui.theme.GameBoyLightGreen


@Composable
fun Buttons(onDirectionChange: (Pair<Int, Int>) -> Unit) {
    val buttonSize = 72.dp

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
        Button(
            colors = ButtonDefaults.buttonColors()
                .copy(containerColor = GameBoyLightGreen.copy(0.8f)),
            onClick = { onDirectionChange(Pair(0, -1)) },
            modifier = Modifier.size(buttonSize),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.KeyboardArrowUp, "Arrow Up Icon")
        }
        Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.padding(12.dp)) {
            Button(
                colors = ButtonDefaults.buttonColors()
                    .copy(containerColor = GameBoyLightGreen.copy(0.8f)),
                onClick = { onDirectionChange(Pair(-1, 0)) },
                modifier = Modifier.size(buttonSize),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.AutoMirrored.Default.KeyboardArrowLeft, "Arrow Left Icon")
            }
            Spacer(modifier = Modifier.size(buttonSize))
            Spacer(modifier = Modifier.size(buttonSize / 2))
            Button(
                colors = ButtonDefaults.buttonColors()
                    .copy(containerColor = GameBoyLightGreen.copy(0.8f)),
                onClick = { onDirectionChange(Pair(1, 0)) },
                modifier = Modifier.size(buttonSize),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.AutoMirrored.Default.KeyboardArrowRight, "Arrow Right Icon")
            }
        }
        Button(
            colors = ButtonDefaults.buttonColors()
                .copy(containerColor = GameBoyLightGreen.copy(0.8f)),
            onClick = { onDirectionChange(Pair(0, 1)) },
            modifier = Modifier.size(buttonSize),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.KeyboardArrowDown, "Arrow Down Icon")
        }
    }
}