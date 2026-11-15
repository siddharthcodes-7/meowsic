package com.surya.meowsic.ui.glass

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

/**
 * Crash-proof Material 3 Expressive icon button (replaces crashy Haze GlassIconButton).
 */
@Composable
fun GlassIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    isActive: Boolean = false,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    tintColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
    hazeState: HazeState? = null,
    enabled: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "iconBtnScale"
    )
    val bgColor by animateColorAsState(
        targetValue = if (isActive) activeColor else tintColor,
        animationSpec = tween(220),
        label = "iconBtnBg"
    )

    Surface(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                role = Role.Button
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            },
        shape = CircleShape,
        color = bgColor
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
            content = content
        )
    }
}

/**
 * Crash-proof Material 3 Expressive pill button (replaces crashy Haze GlassButton).
 */
@Composable
fun GlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isActive: Boolean = false,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    tintColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
    hazeState: HazeState? = null,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "btnScale"
    )
    val bgColor by animateColorAsState(
        targetValue = if (isActive) activeColor else tintColor,
        animationSpec = tween(220),
        label = "btnBg"
    )

    Surface(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(50))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                role = Role.Button
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            },
        shape = RoundedCornerShape(50),
        color = bgColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}

/**
 * Meowsic ToggleSegmentButton implementation: presentation/components/ToggleSegmentButton.kt
 * Morphs corner radius from 8.dp (inactive) to 50.dp (active pill) with bouncy spring physics.
 */
@Composable
fun ToggleSegmentButton(
    modifier: Modifier = Modifier,
    active: Boolean,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    inactiveColor: Color = MaterialTheme.colorScheme.surfaceContainerLowest,
    activeContentColor: Color = MaterialTheme.colorScheme.onPrimary,
    inactiveContentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    activeCornerRadius: Dp = 50.dp,
    onClick: () -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (active) activeColor else inactiveColor,
        animationSpec = tween(durationMillis = 250),
        label = "SegmentBgColor"
    )
    val cornerRadius by animateDpAsState(
        targetValue = if (active) activeCornerRadius else 10.dp,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "SegmentCornerRadius"
    )

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(cornerRadius))
            .background(bgColor)
            .clickable(onClick = onClick)
            .semantics { },
        contentAlignment = Alignment.Center,
        content = content
    )
}

/**
 * Meowsic PlayingEqIcon implementation: presentation/components/subcomps/PlayingEqIcon.kt
 * Continuous 3-bar organic sine-wave equalizer that morphs into dots when paused.
 */
@Composable
fun PlayingEqIcon(
    modifier: Modifier = Modifier,
    color: Color,
    isPlaying: Boolean = true,
    bars: Int = 3
) {
    val infinite = rememberInfiniteTransition(label = "eq")
    val phase by infinite.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )
    val wander by infinite.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wander"
    )

    val activity = remember { Animatable(if (isPlaying) 1f else 0f) }
    LaunchedEffect(isPlaying) {
        activity.animateTo(
            targetValue = if (isPlaying) 1f else 0f,
            animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing)
        )
    }

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val gapRatio = 0.45f
        val barW = w / (bars + (bars - 1) * gapRatio)
        val gap = barW * gapRatio
        val r = CornerRadius(barW / 2f, barW / 2f)

        val minFactor = 0.24f
        val maxFactor = 0.96f
        val dotHeight = barW

        val slowShift = 0.7f * sin(wander)
        val speeds = floatArrayOf(1.0f, 1.25f, 0.9f)
        val offsets = floatArrayOf(0f, 1.9f, 3.4f)

        repeat(bars) { i ->
            val spd = speeds[i % speeds.size]
            val off = offsets[i % offsets.size]
            val p1 = phase * spd + off + slowShift
            val p2 = phase * (spd * 1.8f) + off * 0.6f - slowShift * 0.6f

            val primary = abs(sin(p1))
            val secondary = abs(sin(p2))
            val raw = (0.7f * primary + 0.3f * secondary)
            val eased = raw * raw * (3f - 2f * raw)

            val frac = minFactor + (maxFactor - minFactor) * eased
            val fullBarH = (h * frac).coerceAtLeast(barW)
            val a = activity.value
            val barH = dotHeight + (fullBarH - dotHeight) * a

            val top = (h - barH) / 2f
            val left = i * (barW + gap)

            drawRoundRect(
                color = color,
                topLeft = Offset(left, top),
                size = Size(barW, barH),
                cornerRadius = r
            )
        }
    }
}

/**
 * Meowsic AutoScrollingText implementation: presentation/components/MarqueeText.kt
 * Auto-scrolling marquee text with smooth gradient fade edges when overflowing.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AutoScrollingText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
    textAlign: TextAlign? = null,
    gradientEdgeColor: Color = Color.Transparent
) {
    val density = LocalDensity.current
    val fadeWidthPx = with(density) { 18.dp.toPx() }
    var isOverflowing by remember(text) { mutableStateOf(false) }
    var containerWidth by remember { mutableIntStateOf(0) }

    SubcomposeLayout(modifier = modifier) { constraints ->
        val textMeasurable = subcompose("measureText") {
            Text(
                text = text,
                style = style,
                maxLines = 1,
                softWrap = false
            )
        }.first()
        val intrinsicWidth = textMeasurable.maxIntrinsicWidth(Constraints.Infinity)
        val maxWidth = constraints.maxWidth
        isOverflowing = intrinsicWidth > maxWidth
        containerWidth = maxWidth

        val contentPlaceable = subcompose("content") {
            if (!isOverflowing) {
                Text(
                    text = text,
                    style = style,
                    color = color,
                    textAlign = textAlign,
                    maxLines = 1,
                    overflow = TextOverflow.Clip
                )
            } else {
                Box(
                    modifier = Modifier
                        .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                        .drawWithContent {
                            drawContent()
                            val gradientWidth = fadeWidthPx.coerceAtMost(size.width / 4f)
                            drawRect(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(Color.Transparent, Color.Black),
                                    startX = 0f,
                                    endX = gradientWidth
                                ),
                                size = Size(gradientWidth, size.height),
                                blendMode = BlendMode.DstIn
                            )
                            drawRect(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(Color.Black, Color.Transparent),
                                    startX = size.width - gradientWidth,
                                    endX = size.width
                                ),
                                topLeft = Offset(size.width - gradientWidth, 0f),
                                size = Size(gradientWidth, size.height),
                                blendMode = BlendMode.DstIn
                            )
                        }
                ) {
                    Text(
                        text = text,
                        style = style,
                        color = color,
                        textAlign = textAlign,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.basicMarquee(
                            iterations = Int.MAX_VALUE,
                            initialDelayMillis = 1200,
                            velocity = 28.dp
                        )
                    )
                }
            }
        }.first().measure(constraints)

        layout(contentPlaceable.width, contentPlaceable.height) {
            contentPlaceable.place(0, 0)
        }
    }
}
