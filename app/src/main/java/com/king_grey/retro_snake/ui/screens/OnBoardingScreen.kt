package com.king_grey.retro_snake.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.king_grey.retro_snake.ui.navigation.Screen
import com.king_grey.retro_snake.ui.theme.DarkGreen

@Composable
fun OnBoardingScreen(navController: NavHostController) {
    Surface(
        color = DarkGreen, modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "Welcome to Retro Snake !", fontSize = 32.sp, color = Color.White)
            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = {
                navController.navigate(Screen.GameScreen.route)
            }) {
                Text(text = "Play", fontSize = 24.sp)
            }
        }
    }
}