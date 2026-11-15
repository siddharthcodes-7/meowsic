package com.surya.meowsic.ui.glass

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.surya.meowsic.ui.theme.GlassTokens
import dev.chrisbanes.haze.HazeState

/**
 * Crash-proof Material 3 Expressive surface replacement for GlassSurface.
 * Never invokes RenderNode / HazeChild when hazeState is null.
 */
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(GlassTokens.CardCornerRadius),
    hazeState: HazeState? = LocalHazeState.current,
    blurRadius: Dp = GlassTokens.DefaultBlurRadius,
    surfaceAlpha: Float = 0.92f,
    tintColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    borderColor: Color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
    enableBlur: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = tintColor.copy(alpha = surfaceAlpha.coerceIn(0.65f, 1f)),
        border = BorderStroke(1.dp, borderColor),
        tonalElevation = 4.dp
    ) {
        Box(content = content)
    }
}
