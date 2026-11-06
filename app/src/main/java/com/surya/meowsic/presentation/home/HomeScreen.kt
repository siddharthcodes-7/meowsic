package com.surya.meowsic.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen() {
    val background = Color(0xFF101010)
    val accent = Color(0xFFB9F36A)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .padding(24.dp)
    ) {
        Text("meo music", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
        Text("Your music. Your world.", color = Color.LightGray, fontSize = 15.sp)

        Spacer(modifier = Modifier.height(35.dp))

        Text("Made for you", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(18.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .background(accent, RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("Discover your music", color = Color.Black, fontSize = 23.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(35.dp))

        Text("Your Library", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        Text("Your songs will appear here.", color = Color.Gray, fontSize = 15.sp)
    }
}