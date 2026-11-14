package com.surya.meowsic.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf

val LocalAmbientPalette = staticCompositionLocalOf { LiquidGlassTokens.DefaultPalette }

private val LiquidGlassColorScheme = darkColorScheme(
    primary = LiquidGlassTokens.TextPrimary,
    onPrimary = LiquidGlassTokens.AmoledVoid,
    secondary = LiquidGlassTokens.TextSecondary,
    onSecondary = LiquidGlassTokens.AmoledVoid,
    background = LiquidGlassTokens.AmoledVoid,
    onBackground = LiquidGlassTokens.TextPrimary,
    surface = LiquidGlassTokens.SurfaceOledFallback,
    onSurface = LiquidGlassTokens.TextPrimary,
    surfaceVariant = LiquidGlassTokens.SurfaceElevated,
    onSurfaceVariant = LiquidGlassTokens.TextSecondary,
    error = LiquidGlassTokens.Crimson,
    onError = LiquidGlassTokens.AmoledVoid
)

@Composable
fun MeowsicTheme(
    currentSongId: String? = null,
    content: @Composable () -> Unit
) {
    val targetPalette = LiquidGlassTokens.getAmbientPaletteForSong(currentSongId)

    val primary by animateColorAsState(
        targetValue = targetPalette.primary,
        animationSpec = tween(durationMillis = LiquidGlassTokens.DurationAmbient),
        label = "AmbientPrimary"
    )
    val secondary by animateColorAsState(
        targetValue = targetPalette.secondary,
        animationSpec = tween(durationMillis = LiquidGlassTokens.DurationAmbient),
        label = "AmbientSecondary"
    )
    val tertiary by animateColorAsState(
        targetValue = targetPalette.tertiary,
        animationSpec = tween(durationMillis = LiquidGlassTokens.DurationAmbient),
        label = "AmbientTertiary"
    )

    val animatedPalette = LiquidGlassTokens.AmbientPalette(
        primary = primary,
        secondary = secondary,
        tertiary = tertiary
    )

    CompositionLocalProvider(LocalAmbientPalette provides animatedPalette) {
        MaterialTheme(
            colorScheme = LiquidGlassColorScheme,
            typography = Typography,
            content = content
        )
    }
}
