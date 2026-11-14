package com.surya.meowsic.ui.glass

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.surya.meowsic.ui.theme.LiquidGlassTokens
import com.surya.meowsic.ui.theme.LocalAmbientPalette
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect

/**
 * Glass icon button — small, circular, glass-tinted.
 * Uses haze backdrop blur on API 31+, falls back to opaque surface.
 */
@Composable
fun GlassIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    hazeState: HazeState? = LocalHazeState.current,
    enabled: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f,
        animationSpec = GlassTokens.pressSpring(),
        label = "GlassIconBtnScale"
    )

    val hazeStyle = remember {
        HazeStyle(
            blurRadius = GlassTokens.BlurRadiusSubtle,
            tint = HazeTint(GlassTokens.TintPrimary),
            noiseFactor = 0.06f
        )
    }

    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(CircleShape)
            .then(
                if (GlassTokens.supportsBlur && hazeState != null) {
                    Modifier.hazeEffect(state = hazeState, style = hazeStyle)
                } else {
                    Modifier.background(GlassTokens.FallbackSurface)
                }
            )
            .background(LiquidGlassTokens.SurfaceGlassPrimary)
            .border(0.5.dp, LiquidGlassTokens.SpecularRimBorder, CircleShape)
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
 * Glass prominent button — the primary action orb (play/pause).
 * Filled with palette gradient + specular rim + spring scale.
 * Matches iOS `.glassEffect(.regular.interactive(), in: .circle)` semantics.
 */
@Composable
fun GlassProminentButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 72.dp,
    palette: LiquidGlassTokens.AmbientPalette = LocalAmbientPalette.current,
    enabled: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1f,
        animationSpec = GlassTokens.pressSpring(),
        label = "GlassProminentScale"
    )

    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .shadow(
                elevation = 20.dp,
                shape = CircleShape,
                ambientColor = palette.primary.copy(alpha = 0.65f),
                spotColor = palette.secondary.copy(alpha = 0.55f)
            )
            .clip(CircleShape)
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
                        Color.White.copy(alpha = 0.70f),
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
 * Text + background glass button — for filter chips, secondary actions.
 */
@Composable
fun GlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(GlassTokens.CornerButton),
    isActive: Boolean = false,
    activeColor: Color = Color.Unspecified,
    hazeState: HazeState? = LocalHazeState.current,
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = GlassTokens.pressSpring(),
        label = "GlassBtnScale"
    )

    val hazeStyle = remember {
        HazeStyle(
            blurRadius = GlassTokens.BlurRadiusSubtle,
            tint = HazeTint(GlassTokens.TintPrimary),
            noiseFactor = 0.06f
        )
    }

    Box(
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(shape)
            .then(
                if (GlassTokens.supportsBlur && hazeState != null) {
                    Modifier.hazeEffect(state = hazeState, style = hazeStyle)
                } else {
                    Modifier.background(GlassTokens.FallbackSurface)
                }
            )
            .run {
                if (isActive && activeColor != Color.Unspecified) {
                    background(activeColor.copy(alpha = 0.18f))
                } else {
                    background(LiquidGlassTokens.SurfaceGlassPrimary)
                }
            }
            .border(
                1.dp,
                if (isActive && activeColor != Color.Unspecified)
                    activeColor.copy(alpha = 0.50f)
                else
                    LiquidGlassTokens.SpecularRimBorder,
                shape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center,
        content = content
    )
}
