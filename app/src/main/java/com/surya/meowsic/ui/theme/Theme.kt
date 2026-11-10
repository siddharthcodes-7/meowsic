package com.surya.meowsic.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AmoledLiquidGlassColorScheme = darkColorScheme(
    primary = LiquidGlassTokens.ElectricLime,
    onPrimary = Color.Black,
    secondary = LiquidGlassTokens.ElectricCyan,
    onSecondary = Color.Black,
    tertiary = LiquidGlassTokens.Crimson,
    onTertiary = Color.White,
    background = LiquidGlassTokens.AmoledVoid,
    onBackground = LiquidGlassTokens.TextPrimary,
    surface = LiquidGlassTokens.SurfaceOledFallback,
    onSurface = LiquidGlassTokens.TextPrimary,
    surfaceVariant = Color(0x1FFFFFFF),
    onSurfaceVariant = LiquidGlassTokens.TextSecondary,
    outline = LiquidGlassTokens.SpecularRimBorder
)

@Composable
fun MeowsicTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AmoledLiquidGlassColorScheme,
        typography = Typography,
        content = content
    )
}
