package com.surya.meowsic.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.surya.meowsic.ui.theme.LiquidGlassTokens

/**
 * Renders localized ambient light emitters behind glass containers.
 * Layer-promoted via graphicsLayer to prevent compositor recalculations.
 */
@Composable
fun AmbientLightOrbs(
    palette: LiquidGlassTokens.AmbientPalette,
    modifier: Modifier = Modifier,
    opacity: Float = 0.35f
) {
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer {
                // Compositor layer isolation: prevents repaint of glass overlay
                clip = false
            }
    ) {
        val width = size.width
        val height = size.height

        // Orb 1: Primary color (Top-Left / Center-Left)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    palette.primary.copy(alpha = opacity),
                    palette.primary.copy(alpha = opacity * 0.5f),
                    Color.Transparent
                ),
                center = Offset(width * 0.25f, height * 0.30f),
                radius = width * 0.65f
            ),
            radius = width * 0.65f,
            center = Offset(width * 0.25f, height * 0.30f)
        )

        // Orb 2: Secondary color (Bottom-Right / Center-Right)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    palette.secondary.copy(alpha = opacity * 0.9f),
                    palette.secondary.copy(alpha = opacity * 0.4f),
                    Color.Transparent
                ),
                center = Offset(width * 0.75f, height * 0.60f),
                radius = width * 0.70f
            ),
            radius = width * 0.70f,
            center = Offset(width * 0.75f, height * 0.60f)
        )

        // Orb 3: Tertiary color (Bottom-Center / Accent)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    palette.tertiary.copy(alpha = opacity * 0.75f),
                    palette.tertiary.copy(alpha = opacity * 0.3f),
                    Color.Transparent
                ),
                center = Offset(width * 0.50f, height * 0.85f),
                radius = width * 0.50f
            ),
            radius = width * 0.50f,
            center = Offset(width * 0.50f, height * 0.85f)
        )
    }
}

/**
 * Liquid Glass Card Container
 * Features:
 * - Liquid glass gradient surface
 * - 3D Specular top/left rim lighting
 * - Inner refraction top bevel highlight
 * - Outer elevation drop shadow
 */
@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    isElevated: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    val surfaceBrush = if (isElevated) {
        LiquidGlassTokens.SurfaceGlassElevated
    } else {
        LiquidGlassTokens.SurfaceGlassPrimary
    }

    Box(
        modifier = modifier
            .shadow(
                elevation = if (isElevated) 24.dp else 12.dp,
                shape = shape,
                ambientColor = Color.Black,
                spotColor = Color.Black
            )
            .clip(shape)
            .background(surfaceBrush)
            .border(
                width = 1.dp,
                brush = LiquidGlassTokens.SpecularRimGradient,
                shape = shape
            )
            .drawWithContent {
                drawContent()
                // Top inner refraction bevel highlight
                drawLine(
                    color = LiquidGlassTokens.SpecularBevelHighlight,
                    start = Offset(x = 16.dp.toPx(), y = 1.dp.toPx()),
                    end = Offset(x = size.width - 16.dp.toPx(), y = 1.dp.toPx()),
                    strokeWidth = 1.5.dp.toPx()
                )
            },
        content = content
    )
}

/**
 * Tactile Liquid Orb Button
 * Used for primary Play/Pause controls.
 * Features:
 * - High-refraction liquid glass elevated gradient
 * - Specular rim light-catching border
 * - Micro-scale press physics (scale down to 0.94 on active press)
 */
@Composable
fun LiquidOrbButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 72.dp,
    palette: LiquidGlassTokens.AmbientPalette = LiquidGlassTokens.DefaultPalette,
    enabled: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        animationSpec = LiquidGlassTokens.TactileSpringSpecFloat,
        label = "OrbScale"
    )

    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = 16.dp,
                shape = CircleShape,
                ambientColor = palette.primary,
                spotColor = palette.secondary
            )
            .clip(CircleShape)
            // Liquid orb background with high-luminance accent glow
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        palette.primary.copy(alpha = 0.95f),
                        palette.secondary.copy(alpha = 0.85f)
                    )
                )
            )
            .border(
                width = 1.5.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.70f), // Top specular rim highlight
                        Color.White.copy(alpha = 0.15f)
                    )
                ),
                shape = CircleShape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center,
        content = content
    )
}

/**
 * Floating Liquid Glass Pill
 * Used for floating MiniPlayer, pill badges, and detached navigation chrome.
 */
@Composable
fun LiquidGlassPill(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(28.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    val baseModifier = modifier
        .shadow(
            elevation = 20.dp,
            shape = shape,
            ambientColor = Color.Black,
            spotColor = Color.Black
        )
        .clip(shape)
        .background(LiquidGlassTokens.SurfaceGlassElevated)
        .border(
            width = 1.dp,
            brush = LiquidGlassTokens.SpecularRimGradient,
            shape = shape
        )
        .drawWithContent {
            drawContent()
            // Top specular rim highlight
            drawLine(
                color = LiquidGlassTokens.SpecularBevelHighlight,
                start = Offset(x = 24.dp.toPx(), y = 1.dp.toPx()),
                end = Offset(x = size.width - 24.dp.toPx(), y = 1.dp.toPx()),
                strokeWidth = 1.2.dp.toPx()
            )
        }

    val clickableModifier = if (onClick != null) {
        baseModifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
    } else {
        baseModifier
    }

    Box(
        modifier = clickableModifier,
        content = content
    )
}
