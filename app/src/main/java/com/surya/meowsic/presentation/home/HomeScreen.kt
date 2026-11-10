package com.surya.meowsic.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.surya.meowsic.ui.components.LiquidGlassCard
import com.surya.meowsic.ui.theme.LiquidGlassTokens

@Composable
fun HomeScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LiquidGlassTokens.AmoledVoid)
            .padding(24.dp)
    ) {
        Text(
            text = "meowsic",
            color = LiquidGlassTokens.TextPrimary,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = LiquidGlassTokens.HeaderLetterSpacing
        )
        Text(
            text = "Your music. Your world.",
            color = LiquidGlassTokens.TextSecondary,
            fontSize = 15.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Made for you",
            color = LiquidGlassTokens.TextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = LiquidGlassTokens.HeaderLetterSpacing
        )
        Spacer(modifier = Modifier.height(16.dp))

        LiquidGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            shape = RoundedCornerShape(22.dp),
            isElevated = true
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Discover your music",
                    color = LiquidGlassTokens.ElectricLime,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = LiquidGlassTokens.HeaderLetterSpacing
                )
            }
        }

        Spacer(modifier = Modifier.height(35.dp))

        Text(
            text = "Your Library",
            color = LiquidGlassTokens.TextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = LiquidGlassTokens.HeaderLetterSpacing
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Your songs and playlists will appear here.",
            color = LiquidGlassTokens.TextMuted,
            fontSize = 15.sp
        )
    }
}
