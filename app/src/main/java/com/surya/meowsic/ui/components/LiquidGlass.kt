package com.surya.meowsic.ui.components

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.surya.meowsic.ui.theme.LiquidGlassTokens
import com.surya.meowsic.ui.theme.LocalAmbientPalette

// ------------------------------------------------------------
// iOS 26 Liquid Glass AGSL Shader (API 33+)
// Implements SDF rounded-box edge lensing + chromatic dispersion
// ------------------------------------------------------------
private const val IOS26_LIQUID_GLASS_AGSL = """
    uniform shader content;
    uniform float2 resolution;
    uniform float cornerRadius;
    uniform float refraction;

    float sdRoundedBox(float2 p, float2 b, float r) {
        float2 q = abs(p) - b + r;
        return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - r;
    }

    half4 main(float2 fragCoord) {
        float2 halfSize = resolution * 0.5;
        float2 p = fragCoord - halfSize;
        float r = min(cornerRadius, min(halfSize.x, halfSize.y));
        float dist = sdRoundedBox(p, halfSize, r);

        if (dist > 0.0) {
            return content.eval(fragCoord);
        }

        float edgeFactor = clamp(1.0 + dist / max(r, 1.0), 0.0, 1.0);
        float2 dir = normalize(p + float2(0.0001, 0.0001));
        float2 offset = dir * (edgeFactor * edgeFactor) * refraction * r;

        half red   = content.eval(fragCoord - offset * 1.08).r;
        half green = content.eval(fragCoord - offset).g;
        half blue  = content.eval(fragCoord - offset * 0.92).b;
        half alpha = content.eval(fragCoord).a;

        return half4(red, green, blue, alpha);
    }
"""

/**
 * Applies iOS 26-style lens refraction via AGSL RuntimeShader on API 33+.
 * Falls back gracefully on API 26–32 (no visible effect, correct geometry preserved).
 */
fun Modifier.liquidGlassLens(
    cornerRadiusDp: Dp = 24.dp,
    refraction: Float = 0.14f
): Modifier = composed {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val shader = remember { RuntimeShader(IOS26_LIQUID_GLASS_AGSL) }
        this.graphicsLayer {
            val w = size.width.coerceAtLeast(1f)
            val h = size.height.coerceAtLeast(1f)
            shader.setFloatUniform("resolution", w, h)
            shader.setFloatUniform("cornerRadius", cornerRadiusDp.toPx())
            shader.setFloatUniform("refraction", refraction)
            renderEffect = RenderEffect
                .createRuntimeShaderEffect(shader, "content")
                .asComposeRenderEffect()
        }
    } else {
        this
    }
}

/**
 * Renders localized ambient light emitters behind glass containers.
 * Uses LocalAmbientPalette when no explicit palette is passed.
 */
@Composable
fun AmbientLightOrbs(
    palette: LiquidGlassTokens.AmbientPalette = LocalAmbientPalette.current,
    modifier: Modifier = Modifier,
    opacity: Float = 0.35f
) {
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer { clip = false }
    ) {
        val width = size.width
        val height = size.height

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
 * - Ambient-palette-tinted glass gradient surface
 * - 3D Specular top/left rim lighting (SpecularRimGradient border)
 * - Inner refraction top bevel highlight (SpecularBevelHighlight)
 * - Outer elevation drop shadow tinted by palette.primary
 * - Optional AGSL lens refraction on API 33+
 */
@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    isElevated: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    val palette = LocalAmbientPalette.current
    val surfaceBrush = if (isElevated) {
        LiquidGlassTokens.SurfaceGlassElevated
    } else {
        LiquidGlassTokens.SurfaceGlassPrimary
    }
    val cornerRadius = when (shape) {
        is RoundedCornerShape -> 24.dp
        else -> 24.dp
    }

    Box(
        modifier = modifier
            .liquidGlassLens(cornerRadiusDp = cornerRadius, refraction = 0.12f)
            .shadow(
                elevation = if (isElevated) 24.dp else 12.dp,
                shape = shape,
                ambientColor = palette.primary.copy(alpha = 0.20f),
                spotColor = Color.Black
            )
            .clip(shape)
            .background(surfaceBrush)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        palette.primary.copy(alpha = if (isElevated) 0.10f else 0.05f),
                        palette.secondary.copy(alpha = 0.03f)
                    )
                )
            )
            .border(1.dp, LiquidGlassTokens.SpecularRimGradient, shape)
            .drawWithContent {
                drawContent()
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
 * Tactile Liquid Orb Button — mirrors SwiftUI `.buttonStyle(.glassProminent)` + `.buttonBorderShape(.circle)`.
 * - Radial gradient fill using palette colors (tinted by LocalAmbientPalette)
 * - Specular rim border
 * - Bouncy spring press scale (0.92f)
 * - Outer glow shadow tinted by palette.primary
 */
@Composable
fun LiquidOrbButton(
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
        targetValue = if (isPressed) 0.92f else 1.0f,
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
                elevation = 20.dp,
                shape = CircleShape,
                ambientColor = palette.primary.copy(alpha = 0.65f),
                spotColor = palette.secondary.copy(alpha = 0.55f)
            )
            .clip(CircleShape)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        palette.primary.copy(alpha = 0.95f),
                        palette.secondary.copy(alpha = 0.85f)
                    )
                ),
                shape = CircleShape
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
 * Floating Liquid Glass Pill — mirrors SwiftUI `.glassEffect(.regular.interactive(), in: .capsule)`.
 * - Ambient-palette-tinted elevated glass surface
 * - Specular rim gradient border + top bevel highlight
 * - Bouncy spring press scale (0.96f) when onClick is provided
 * - Optional AGSL lens refraction on API 33+
 */
@Composable
fun LiquidGlassPill(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(28.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val palette = LocalAmbientPalette.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.96f else 1f,
        animationSpec = LiquidGlassTokens.TactileSpringSpecFloat,
        label = "LiquidPillScale"
    )

    val baseModifier = modifier
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .shadow(
            elevation = 20.dp,
            shape = shape,
            ambientColor = palette.primary.copy(alpha = 0.22f),
            spotColor = Color.Black
        )
        .clip(shape)
        .background(LiquidGlassTokens.SurfaceGlassElevated)
        .background(
            Brush.horizontalGradient(
                colors = listOf(
                    palette.primary.copy(alpha = 0.12f),
                    palette.secondary.copy(alpha = 0.08f)
                )
            )
        )
        .border(1.dp, LiquidGlassTokens.SpecularRimGradient, shape)
        .drawWithContent {
            drawContent()
            val strokeWidth = 1.dp.toPx()
            drawLine(
                color = LiquidGlassTokens.SpecularBevelHighlight,
                start = Offset(x = 24.dp.toPx(), y = strokeWidth),
                end = Offset(x = size.width - 24.dp.toPx(), y = strokeWidth),
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
