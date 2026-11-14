package com.surya.meowsic.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.surya.meowsic.ui.glass.GlassSurface
import com.surya.meowsic.ui.glass.GlassVariant
import com.surya.meowsic.ui.theme.LiquidGlassTokens

@Composable
fun HomeScreen() {
    // No opaque background — root AmbientBackground shines through
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text(
                text = "meowsic",
                color = LiquidGlassTokens.TextPrimary,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = LiquidGlassTokens.HeaderLetterSpacing
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Discover & play music.",
                color = LiquidGlassTokens.TextSecondary,
                fontSize = 15.sp
            )
        }

        Box(Modifier.fillMaxWidth().height(0.5.dp).background(Color(0x1AFFFFFF)))

        // Content — 160dp bottom padding so last item scrolls above floating chrome
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp),
            contentPadding = PaddingValues(bottom = 160.dp)
        ) {
            // Section: Made For You
            item {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)) {
                    Text(text = "Made For You", color = LiquidGlassTokens.TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                GlassSurface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .padding(horizontal = 20.dp),
                    variant = GlassVariant.ELEVATED,
                    shape = RoundedCornerShape(22.dp),
                    enableBlur = false
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = LiquidGlassTokens.TextMuted,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Search a song to start listening",
                            color = LiquidGlassTokens.TextSecondary,
                            fontSize = 14.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Section: Your Queue
            item {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)) {
                    Text(text = "Your Queue", color = LiquidGlassTokens.TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                GlassSurface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    variant = GlassVariant.PRIMARY,
                    shape = RoundedCornerShape(18.dp),
                    enableBlur = false
                ) {
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp)) {
                        Text(
                            text = "Songs you add to the queue will appear here.",
                            color = LiquidGlassTokens.TextMuted,
                            fontSize = 14.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Section: About
            item {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)) {
                    Text(text = "About", color = LiquidGlassTokens.TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                GlassSurface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    variant = GlassVariant.PRIMARY,
                    shape = RoundedCornerShape(18.dp),
                    enableBlur = false
                ) {
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp)) {
                        Text(
                            text = "Meowsic is an open-source music player powered by YouTube. " +
                                    "Stream any song, build your queue, and enjoy crystal-clear audio — " +
                                    "no subscriptions, no ads.",
                            color = LiquidGlassTokens.TextMuted,
                            fontSize = 13.sp,
                            lineHeight = 20.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
