package com.surya.meowsic.ui.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.surya.meowsic.ui.theme.LiquidGlassTokens
import dev.chrisbanes.haze.HazeState

val LocalHazeState = staticCompositionLocalOf<HazeState?> { null }
val LocalAmbientPalette = staticCompositionLocalOf { LiquidGlassTokens.DefaultPalette }

@Composable
fun WithoutHaze(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalHazeState provides null) {
        content()
    }
}

@Composable
fun GlassBackdrop(
    thumbnailUrl: String?,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    enableBlur: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    val paletteState = LiquidGlassTokens.rememberAmbientPalette(thumbnailUrl)
    val palette = paletteState.value
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            palette.surfaceContainerHigh.copy(alpha = 0.85f),
            MaterialTheme.colorScheme.background,
            Color(0xFF0E0918)
        )
    )

    CompositionLocalProvider(
        LocalHazeState provides null,
        LocalAmbientPalette provides palette
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(backgroundBrush),
            content = content
        )
    }
}
