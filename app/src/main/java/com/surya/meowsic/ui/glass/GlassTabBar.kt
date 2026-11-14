package com.surya.meowsic.ui.glass

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.surya.meowsic.ui.theme.LiquidGlassTokens
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect

data class GlassTabItem(
    val label: String,
    val icon: ImageVector,
    val index: Int
)

/**
 * Floating glass tab bar — full-width capsule with:
 * - Real backdrop blur via haze (API 31+)
 * - Sliding glass selection lens that moves between tabs
 * - Spring-animated press scale on each tab item
 * - Specular bevel rim
 */
@Composable
fun GlassTabBar(
    items: List<GlassTabItem>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    hazeState: HazeState? = LocalHazeState.current,
    palette: LiquidGlassTokens.AmbientPalette = LiquidGlassTokens.DefaultPalette
) {
    val shape = RoundedCornerShape(GlassTokens.CornerTabBar)
    val hazeStyle = remember {
        HazeStyle(
            blurRadius = GlassTokens.BlurRadiusElevated,
            tint = HazeTint(GlassTokens.TintPrimary),
            noiseFactor = 0.08f
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .height(64.dp)
            .shadow(
                elevation = 20.dp,
                shape = shape,
                ambientColor = palette.primary.copy(alpha = 0.18f),
                spotColor = Color.Black.copy(alpha = 0.6f)
            )
            .clip(shape)
            .then(
                if (GlassTokens.supportsBlur && hazeState != null) {
                    Modifier.hazeEffect(state = hazeState, style = hazeStyle)
                } else {
                    Modifier.background(GlassTokens.FallbackSurface)
                }
            )
            .background(LiquidGlassTokens.SurfaceGlassElevated)
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        palette.primary.copy(alpha = 0.08f),
                        palette.secondary.copy(alpha = 0.05f)
                    )
                )
            )
            .border(1.dp, LiquidGlassTokens.SpecularRimGradient, shape)
            .drawWithContent {
                drawContent()
                drawLine(
                    color = LiquidGlassTokens.SpecularBevelHighlight,
                    start = Offset(x = 32.dp.toPx(), y = 1.dp.toPx()),
                    end = Offset(x = size.width - 32.dp.toPx(), y = 1.dp.toPx()),
                    strokeWidth = 1.2.dp.toPx()
                )
            }
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                GlassTabItem(
                    item = item,
                    isSelected = selectedIndex == item.index,
                    palette = palette,
                    onClick = { onTabSelected(item.index) }
                )
            }
        }
    }
}

@Composable
private fun RowScope.GlassTabItem(
    item: GlassTabItem,
    isSelected: Boolean,
    palette: LiquidGlassTokens.AmbientPalette,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1f,
        animationSpec = GlassTokens.pressSpring(),
        label = "TabItemScale${item.index}"
    )

    // Selection indicator height animation
    val indicatorHeight by animateDpAsState(
        targetValue = if (isSelected) 2.dp else 0.dp,
        animationSpec = GlassTokens.pressSpring(),
        label = "TabIndicator${item.index}"
    )

    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(RoundedCornerShape(28.dp))
            .background(
                if (isSelected) LiquidGlassTokens.SurfaceGlassActive
                else Color.Transparent
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.label,
            tint = if (isSelected) palette.primary else LiquidGlassTokens.TextMuted,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = item.label,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) palette.primary else LiquidGlassTokens.TextMuted
        )
        // Selection indicator dot
        if (indicatorHeight > 0.dp) {
            Spacer(modifier = Modifier.height(1.dp))
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(palette.primary)
            )
        }
    }
}
