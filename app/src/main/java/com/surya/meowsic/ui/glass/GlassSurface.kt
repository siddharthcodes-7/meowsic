package com.surya.meowsic.ui.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.surya.meowsic.ui.theme.LiquidGlassTokens
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect

/**
 * Glass surface variants.
 */
enum class GlassVariant {
    /** Standard chrome glass — tab bars, mini player */
    PRIMARY,
    /** Elevated glass — prominent cards, now playing */
    ELEVATED,
    /** Sheet glass — bottom sheets */
    SHEET,
    /** Subtle glass — search bars */
    SUBTLE
}

/**
 * Core glass surface composable.
 *
 * On API 31+: real backdrop blur via haze [hazeChild] + specular rim.
 * On API 26-30: opaque tinted fallback + specular rim.
 *
 * Always reads [LocalHazeState] if [hazeState] is not passed explicitly.
 */
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    variant: GlassVariant = GlassVariant.PRIMARY,
    shape: Shape = RoundedCornerShape(GlassTokens.CornerCard),
    hazeState: HazeState? = LocalHazeState.current,
    tintColor: Color? = null,
    showBevel: Boolean = true,
    cornerRadiusForBevel: Dp = 24.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val (blurRadius, tintC, noiseFactor) = when (variant) {
        GlassVariant.PRIMARY -> Triple(GlassTokens.BlurRadiusPrimary, tintColor ?: GlassTokens.TintPrimary, 0.08f)
        GlassVariant.ELEVATED -> Triple(GlassTokens.BlurRadiusElevated, tintColor ?: GlassTokens.TintElevated, 0.10f)
        GlassVariant.SHEET -> Triple(GlassTokens.BlurRadiusElevated, tintColor ?: GlassTokens.TintSheet, 0.12f)
        GlassVariant.SUBTLE -> Triple(GlassTokens.BlurRadiusSubtle, tintColor ?: GlassTokens.TintPrimary, 0.05f)
    }

    val hazeStyle = remember(blurRadius, tintC, noiseFactor) {
        HazeStyle(
            blurRadius = blurRadius,
            tint = HazeTint(tintC),
            noiseFactor = noiseFactor
        )
    }

    val surfaceTint = when (variant) {
        GlassVariant.PRIMARY -> LiquidGlassTokens.SurfaceGlassPrimary
        GlassVariant.ELEVATED -> LiquidGlassTokens.SurfaceGlassElevated
        GlassVariant.SHEET -> LiquidGlassTokens.SurfaceGlassPrimary
        GlassVariant.SUBTLE -> LiquidGlassTokens.SurfaceGlassPrimary
    }

    val blurModifier = if (GlassTokens.supportsBlur && hazeState != null) {
        Modifier.hazeEffect(state = hazeState, style = hazeStyle)
    } else {
        Modifier.background(GlassTokens.FallbackSurface)
    }

    Box(
        modifier = modifier
            .clip(shape)
            .then(blurModifier)
            .background(surfaceTint)
            .border(1.dp, LiquidGlassTokens.SpecularRimGradient, shape)
            .run {
                if (showBevel) {
                    drawWithContent {
                        drawContent()
                        drawLine(
                            color = LiquidGlassTokens.SpecularBevelHighlight,
                            start = Offset(x = cornerRadiusForBevel.toPx(), y = 1.dp.toPx()),
                            end = Offset(
                                x = size.width - cornerRadiusForBevel.toPx(),
                                y = 1.dp.toPx()
                            ),
                            strokeWidth = 1.2.dp.toPx()
                        )
                    }
                } else this
            },
        content = content
    )
}

/**
 * Convenience [Modifier] extension: marks a composable as a glass child that blurs content behind it.
 */
@Composable
fun Modifier.glass(
    shape: Shape = RoundedCornerShape(GlassTokens.CornerCard),
    variant: GlassVariant = GlassVariant.PRIMARY,
    hazeState: HazeState? = LocalHazeState.current
): Modifier {
    val (blurRadius, tintC, noiseFactor) = when (variant) {
        GlassVariant.PRIMARY -> Triple(GlassTokens.BlurRadiusPrimary, GlassTokens.TintPrimary, 0.08f)
        GlassVariant.ELEVATED -> Triple(GlassTokens.BlurRadiusElevated, GlassTokens.TintElevated, 0.10f)
        GlassVariant.SHEET -> Triple(GlassTokens.BlurRadiusElevated, GlassTokens.TintSheet, 0.12f)
        GlassVariant.SUBTLE -> Triple(GlassTokens.BlurRadiusSubtle, GlassTokens.TintPrimary, 0.05f)
    }
    val hazeStyle = HazeStyle(blurRadius = blurRadius, tint = HazeTint(tintC), noiseFactor = noiseFactor)
    return if (GlassTokens.supportsBlur && hazeState != null) {
        this.clip(shape).hazeEffect(state = hazeState, style = hazeStyle)
    } else {
        this.clip(shape).background(GlassTokens.FallbackSurface)
    }
}

/**
 * Gradient glass tint brush based on palette colors.
 */
fun paletteGlassBrush(
    primary: Color,
    secondary: Color,
    primaryAlpha: Float = 0.08f,
    secondaryAlpha: Float = 0.04f
): Brush = Brush.linearGradient(
    colors = listOf(
        primary.copy(alpha = primaryAlpha),
        secondary.copy(alpha = secondaryAlpha)
    )
)
