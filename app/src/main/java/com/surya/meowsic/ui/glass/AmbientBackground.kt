package com.surya.meowsic.ui.glass

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.InfiniteTransition
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import com.surya.meowsic.ui.theme.LiquidGlassTokens

/**
 * Animated ambient background — slow drifting colored blobs.
 * Replaces the static [AmbientLightOrbs] with animated positions.
 *
 * Uses [InfiniteTransition] with 20–40s durations to drift orb positions.
 * Each orb is an independent radial gradient drawn on Canvas.
 *
 * Performance: single Canvas draw call per frame, 3 orbs max, 60ms blur not applied here
 * (blur is handled by haze at the glass child layer).
 */
@Composable
fun AmbientBackground(
    palette: LiquidGlassTokens.AmbientPalette,
    modifier: Modifier = Modifier,
    opacity: Float = 0.30f
) {
    val transition = rememberInfiniteTransition(label = "AmbientOrbs")

    // Orb 1: primary — drifts horizontally (slow, 28s cycle)
    val orb1X by transition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.40f,
        animationSpec = infiniteRepeatable(
            animation = tween(28_000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Orb1X"
    )
    val orb1Y by transition.animateFloat(
        initialValue = 0.20f,
        targetValue = 0.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(35_000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Orb1Y"
    )

    // Orb 2: secondary — drifts in opposite direction (medium, 32s cycle)
    val orb2X by transition.animateFloat(
        initialValue = 0.80f,
        targetValue = 0.55f,
        animationSpec = infiniteRepeatable(
            animation = tween(32_000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Orb2X"
    )
    val orb2Y by transition.animateFloat(
        initialValue = 0.55f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(40_000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Orb2Y"
    )

    // Orb 3: tertiary — smallest, fastest (20s cycle)
    val orb3X by transition.animateFloat(
        initialValue = 0.40f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(20_000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Orb3X"
    )
    val orb3Y by transition.animateFloat(
        initialValue = 0.80f,
        targetValue = 0.90f,
        animationSpec = infiniteRepeatable(
            animation = tween(24_000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Orb3Y"
    )

    Canvas(
        modifier = modifier.graphicsLayer { clip = false }
    ) {
        val w = size.width
        val h = size.height

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    palette.primary.copy(alpha = opacity),
                    palette.primary.copy(alpha = opacity * 0.4f),
                    Color.Transparent
                ),
                center = Offset(w * orb1X, h * orb1Y),
                radius = w * 0.70f
            ),
            radius = w * 0.70f,
            center = Offset(w * orb1X, h * orb1Y)
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    palette.secondary.copy(alpha = opacity * 0.85f),
                    palette.secondary.copy(alpha = opacity * 0.35f),
                    Color.Transparent
                ),
                center = Offset(w * orb2X, h * orb2Y),
                radius = w * 0.75f
            ),
            radius = w * 0.75f,
            center = Offset(w * orb2X, h * orb2Y)
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    palette.tertiary.copy(alpha = opacity * 0.65f),
                    palette.tertiary.copy(alpha = opacity * 0.25f),
                    Color.Transparent
                ),
                center = Offset(w * orb3X, h * orb3Y),
                radius = w * 0.50f
            ),
            radius = w * 0.50f,
            center = Offset(w * orb3X, h * orb3Y)
        )
    }
}
