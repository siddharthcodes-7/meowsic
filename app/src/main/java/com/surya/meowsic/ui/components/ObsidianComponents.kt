package com.surya.meowsic.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.surya.meowsic.ui.theme.Obsidian

/**
 * OBSIDIAN Glass Surface
 *
 * A frosted-glass-style container with:
 * - Subtle semi-transparent fill
 * - 1dp border in GlassBorder
 * - Top specular reflection line
 */
@Composable
fun ObsidianGlassSurface(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = Obsidian.GlassRadius.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    Box(
        modifier = modifier
            .clip(shape)
            .background(color = Obsidian.GlassFill, shape = shape)
            .border(
                width = 1.dp,
                color = Obsidian.GlassBorder,
                shape = shape
            )
            .drawWithContent {
                drawContent()
                // Top specular reflection line
                drawLine(
                    color = Obsidian.GlassTopReflection,
                    start = Offset(x = cornerRadius.toPx(), y = 1.dp.toPx()),
                    end = Offset(x = size.width - cornerRadius.toPx(), y = 1.dp.toPx()),
                    strokeWidth = 1.dp.toPx()
                )
            },
        content = content
    )
}

/**
 * OBSIDIAN Section Header
 *
 * Displays a section title (titleLarge weight/size) with an optional "See All" action.
 */
@Composable
fun ObsidianSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    onSeeAllClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Obsidian.SpacingMd.dp, vertical = Obsidian.SpacingXs.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            color = Obsidian.TextPrimary,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.sp
        )
        if (onSeeAllClick != null) {
            Text(
                text = "See All",
                color = Obsidian.TextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = onSeeAllClick
                )
            )
        }
    }
}

/**
 * OBSIDIAN Divider
 *
 * A subtle 0.5dp horizontal separator in Obsidian.Separator color.
 */
@Composable
fun ObsidianDivider(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(0.5.dp)
            .background(Obsidian.Separator)
    )
}

/**
 * OBSIDIAN Icon Button
 *
 * Minimal icon button with spring-based press scale animation (0.88 on press).
 */
@Composable
fun ObsidianIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = Obsidian.IconTint,
    size: Dp = 24.dp,
    contentDescription: String? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "ObsidianIconButtonScale"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(size)
        )
    }
}

/**
 * OBSIDIAN Square Artwork
 *
 * Square album art with configurable corner radius, size, shadow, and fallback music note icon.
 */
@Composable
fun ObsidianSquareArtwork(
    imageUrl: String?,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    cornerRadius: Dp = Obsidian.ArtworkRadius.dp,
    contentDescription: String? = null
) {
    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(Obsidian.SurfaceSecondary),
        contentAlignment = Alignment.Center
    ) {
        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Fallback music note icon using canvas draw
            androidx.compose.foundation.Canvas(modifier = Modifier.size(size * 0.45f)) {
                val w = this.size.width
                val h = this.size.height
                // Simple music note shape: filled circle at bottom + stem
                drawCircle(
                    color = Obsidian.TextTertiary,
                    radius = w * 0.28f,
                    center = Offset(w * 0.3f, h * 0.75f)
                )
                drawLine(
                    color = Obsidian.TextTertiary,
                    start = Offset(w * 0.56f, h * 0.75f),
                    end = Offset(w * 0.56f, h * 0.15f),
                    strokeWidth = w * 0.1f
                )
                drawLine(
                    color = Obsidian.TextTertiary,
                    start = Offset(w * 0.56f, h * 0.15f),
                    end = Offset(w * 0.90f, h * 0.25f),
                    strokeWidth = w * 0.1f
                )
            }
        }
    }
}

/**
 * OBSIDIAN Progress Bar
 *
 * Custom seek bar with pill-shaped track:
 * - Track height: 4dp, expands to 6dp while dragging
 * - Track color: Obsidian.ProgressTrack (empty), Obsidian.ProgressActive (filled)
 * - No Material Slider used — raw pointer input for precise control
 */
@Composable
fun ObsidianProgressBar(
    progress: Float, // 0f..1f
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var isDragging by remember { mutableStateOf(false) }
    var trackWidthPx by remember { mutableIntStateOf(0) }

    val trackHeight by animateDpAsState(
        targetValue = if (isDragging) 6.dp else 4.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "ObsidianProgressBarHeight"
    )

    val density = LocalDensity.current
    val trackHeightPx = with(density) { trackHeight.toPx() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(24.dp) // generous touch target height
            .onSizeChanged { trackWidthPx = it.width }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    if (trackWidthPx > 0) {
                        val fraction = (offset.x / trackWidthPx).coerceIn(0f, 1f)
                        onSeek(fraction)
                    }
                }
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { isDragging = true },
                    onDragEnd = { isDragging = false },
                    onDragCancel = { isDragging = false },
                    onHorizontalDrag = { change, _ ->
                        change.consume()
                        if (trackWidthPx > 0) {
                            val fraction = (change.position.x / trackWidthPx).coerceIn(0f, 1f)
                            onSeek(fraction)
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(trackHeight)
        ) {
            val w = size.width
            val h = size.height
            val cornerR = CornerRadius(h / 2f, h / 2f)
            val filledWidth = w * progress.coerceIn(0f, 1f)

            // Background track
            drawRoundRect(
                color = Obsidian.ProgressTrack,
                size = Size(w, h),
                cornerRadius = cornerR
            )

            // Filled portion
            if (filledWidth > 0f) {
                drawRoundRect(
                    color = Obsidian.ProgressActive,
                    size = Size(filledWidth, h),
                    cornerRadius = cornerR
                )
            }
        }
    }
}

/**
 * OBSIDIAN Text Button
 *
 * Minimal text-only button with Obsidian.TextSecondary color.
 */
@Composable
fun ObsidianTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = Obsidian.TextSecondary
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "ObsidianTextButtonScale"
    )

    Text(
        text = text,
        color = color,
        fontSize = 15.sp,
        fontWeight = FontWeight.Medium,
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = Obsidian.SpacingXs.dp, vertical = Obsidian.SpacingXxs.dp)
    )
}
