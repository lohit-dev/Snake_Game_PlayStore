package com.king_grey.retro_snake.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.king_grey.retro_snake.ui.theme.GameBoyLightGreen


@Composable
fun GameOverScreen(onRestart: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Game Over",
            fontSize = 64.sp,
            fontWeight = FontWeight.Bold,
            fontStyle = FontStyle.Italic,
            color = Color.Red,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.padding(20.dp))
        Button(
            colors = ButtonDefaults.buttonColors()
                .copy(containerColor = GameBoyLightGreen.copy(0.8f)),
            modifier = Modifier.padding(32.dp),
            onClick = onRestart,
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                "Play Again !!!",
                color = Color.White,
                fontSize = 22.sp,
                modifier = Modifier.padding(vertical = 12.dp),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}