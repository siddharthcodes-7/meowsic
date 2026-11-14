package com.surya.meowsic.ui.glass

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.surya.meowsic.presentation.player.formatTime
import com.surya.meowsic.ui.theme.LiquidGlassTokens

/**
 * Glass seek slider with animated track height and palette-gradient fill.
 * Replaces [LiquidScrubber] with identical functionality but glass thumb style.
 */
@Composable
fun GlassSlider(
    positionMs: Long,
    durationMs: Long,
    palette: LiquidGlassTokens.AmbientPalette,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var isDragging by remember { mutableStateOf(false) }
    var dragProgress by remember { mutableFloatStateOf(0f) }
    var barWidthPx by remember { mutableFloatStateOf(1f) }

    val effectiveProgress = if (isDragging) {
        dragProgress
    } else if (durationMs > 0) {
        (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }

    val trackHeight by animateDpAsState(
        targetValue = if (isDragging) 10.dp else 6.dp,
        animationSpec = LiquidGlassTokens.TactileSpringSpecDp,
        label = "GlassSliderHeight"
    )
    val thumbRadius by animateDpAsState(
        targetValue = if (isDragging) 10.dp else 0.dp,
        animationSpec = LiquidGlassTokens.TactileSpringSpecDp,
        label = "GlassThumbRadius"
    )

    val currentDisplayMs = if (isDragging && durationMs > 0) {
        (dragProgress * durationMs).toLong()
    } else {
        positionMs
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .onSizeChanged { barWidthPx = it.width.toFloat().coerceAtLeast(1f) }
                .pointerInput(durationMs) {
                    detectTapGestures { offset ->
                        if (durationMs > 0) {
                            onSeek(((offset.x / barWidthPx).coerceIn(0f, 1f) * durationMs).toLong())
                        }
                    }
                }
                .pointerInput(durationMs) {
                    detectHorizontalDragGestures(
                        onDragStart = { offset ->
                            isDragging = true
                            dragProgress = (offset.x / barWidthPx).coerceIn(0f, 1f)
                        },
                        onDragEnd = {
                            if (durationMs > 0) onSeek((dragProgress * durationMs).toLong())
                            isDragging = false
                        },
                        onDragCancel = { isDragging = false },
                        onHorizontalDrag = { change, _ ->
                            change.consume()
                            dragProgress = (change.position.x / barWidthPx).coerceIn(0f, 1f)
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxWidth().height(trackHeight)) {
                val cornerRadius = CornerRadius(size.height / 2f, size.height / 2f)

                // Track background
                drawRoundRect(
                    color = Color(0x26FFFFFF),
                    size = size,
                    cornerRadius = cornerRadius
                )

                val fillWidth = size.width * effectiveProgress
                if (fillWidth > 0f) {
                    // Filled track — palette gradient
                    drawRoundRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(palette.primary, palette.secondary),
                            startX = 0f,
                            endX = size.width
                        ),
                        size = Size(fillWidth, size.height),
                        cornerRadius = cornerRadius
                    )

                    // Glass thumb — white circle with palette glow
                    if (thumbRadius.toPx() > 0f) {
                        val thumbCenter = Offset(fillWidth, size.height / 2f)
                        // Glow ring
                        drawCircle(
                            color = palette.primary.copy(alpha = 0.4f),
                            radius = thumbRadius.toPx() + 3.dp.toPx(),
                            center = thumbCenter
                        )
                        // White thumb
                        drawCircle(
                            color = Color.White,
                            radius = thumbRadius.toPx(),
                            center = thumbCenter
                        )
                        // Specular highlight on thumb
                        drawCircle(
                            color = Color.White.copy(alpha = 0.6f),
                            radius = thumbRadius.toPx() * 0.4f,
                            center = thumbCenter - Offset(thumbRadius.toPx() * 0.2f, thumbRadius.toPx() * 0.2f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
        ) {
            Text(
                text = formatTime(currentDisplayMs),
                color = LiquidGlassTokens.TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = formatTime(durationMs),
                color = LiquidGlassTokens.TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
