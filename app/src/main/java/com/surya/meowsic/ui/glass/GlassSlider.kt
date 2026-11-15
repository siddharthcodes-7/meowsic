package com.surya.meowsic.ui.glass

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.surya.meowsic.ui.theme.LiquidGlassTokens
import java.util.Locale
import kotlin.math.PI
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.sin

/**
 * Meowsic WavyMusicSlider implementation: presentation/components/WavyMusicSlider.kt
 * Expressive animated sine-wave progress bar that flattens when paused or scrubbed,
 * with a thumb that morphs from a circle into a vertical pill line when dragged.
 */
@Composable
fun WavyMusicSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    onValueChangeFinished: (() -> Unit)? = null,
    isPlaying: Boolean = true,
    trackHeight: Dp = 5.dp,
    thumbRadius: Dp = 8.dp,
    activeTrackColor: Color = MaterialTheme.colorScheme.primary,
    inactiveTrackColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.22f),
    thumbColor: Color = MaterialTheme.colorScheme.primary,
    waveAmplitudeWhenPlaying: Dp = 3.5.dp,
    waveLength: Dp = 38.dp,
    waveAnimationDuration: Int = 1800,
    hideInactiveTrackPortion: Boolean = true,
    isWaveEligible: Boolean = true,
    thumbLineHeightWhenInteracting: Dp = 24.dp
) {
    val haptic = LocalHapticFeedback.current
    var isDragging by remember { mutableStateOf(false) }

    val thumbInteractionFraction by animateFloatAsState(
        targetValue = if (isDragging) 1f else 0f,
        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
        label = "ThumbInteractionAnim"
    )

    val shouldShowWave = isWaveEligible && isPlaying && !isDragging

    val animatedWaveAmplitudeDp by animateFloatAsState(
        targetValue = if (shouldShowWave) waveAmplitudeWhenPlaying.value else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "WaveAmplitudeAnim"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "WavePhaseInfinite")
    val phaseShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = waveAnimationDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "WavePhaseShiftAnim"
    )

    val density = LocalDensity.current
    val trackHeightPx = with(density) { trackHeight.toPx() }
    val thumbRadiusPx = with(density) { thumbRadius.toPx() }
    val waveAmplitudePx = with(density) { animatedWaveAmplitudeDp.dp.toPx() }
    val waveLengthPx = with(density) { waveLength.toPx() }
    val waveFrequency = if (waveLengthPx > 0f) (2f * PI.toFloat()) / waveLengthPx else 0f
    val thumbLineHeightPx = with(density) { thumbLineHeightWhenInteracting.toPx() }
    val thumbGapPx = with(density) { 4.dp.toPx() }

    val wavePath = remember { Path() }
    val sliderVisualHeight = remember(trackHeight, thumbRadius, waveAmplitudeWhenPlaying, thumbLineHeightWhenInteracting) {
        maxOf(trackHeight * 2, thumbRadius * 2, (waveAmplitudeWhenPlaying * 2) + trackHeight, thumbLineHeightWhenInteracting) + 8.dp
    }

    val normalizedValue = remember(value, valueRange) {
        if (valueRange.endInclusive - valueRange.start <= 0f) 0f
        else ((value - valueRange.start) / (valueRange.endInclusive - valueRange.start)).coerceIn(0f, 1f)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(sliderVisualHeight)
            .pointerInput(enabled, valueRange) {
                if (!enabled) return@pointerInput
                detectTapGestures { offset ->
                    val usableWidth = (size.width - 2 * thumbRadiusPx).coerceAtLeast(1f)
                    val fraction = ((offset.x - thumbRadiusPx) / usableWidth).coerceIn(0f, 1f)
                    val newVal = valueRange.start + fraction * (valueRange.endInclusive - valueRange.start)
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onValueChange(newVal)
                    onValueChangeFinished?.invoke()
                }
            }
            .pointerInput(enabled, valueRange) {
                if (!enabled) return@pointerInput
                detectHorizontalDragGestures(
                    onDragStart = {
                        isDragging = true
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    },
                    onDragEnd = {
                        isDragging = false
                        onValueChangeFinished?.invoke()
                    },
                    onDragCancel = {
                        isDragging = false
                        onValueChangeFinished?.invoke()
                    },
                    onHorizontalDrag = { change, _ ->
                        change.consume()
                        val usableWidth = (size.width - 2 * thumbRadiusPx).coerceAtLeast(1f)
                        val fraction = ((change.position.x - thumbRadiusPx) / usableWidth).coerceIn(0f, 1f)
                        val newVal = valueRange.start + fraction * (valueRange.endInclusive - valueRange.start)
                        onValueChange(newVal)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .drawWithCache {
                    val canvasWidth = size.width
                    val localCenterY = size.height / 2f
                    val localTrackStart = thumbRadiusPx
                    val localTrackEnd = (canvasWidth - thumbRadiusPx).coerceAtLeast(localTrackStart)
                    val localTrackWidth = (localTrackEnd - localTrackStart).coerceAtLeast(0f)

                    val activeStroke = Stroke(
                        width = trackHeightPx,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )

                    onDrawBehind {
                        val currentProgressPx = localTrackStart + localTrackWidth * normalizedValue

                        // Inactive track portion
                        if (hideInactiveTrackPortion) {
                            if (currentProgressPx < localTrackEnd) {
                                drawLine(
                                    color = inactiveTrackColor,
                                    start = Offset(currentProgressPx, localCenterY),
                                    end = Offset(localTrackEnd, localCenterY),
                                    strokeWidth = trackHeightPx,
                                    cap = StrokeCap.Round
                                )
                            }
                        } else {
                            drawLine(
                                color = inactiveTrackColor,
                                start = Offset(localTrackStart, localCenterY),
                                end = Offset(localTrackEnd, localCenterY),
                                strokeWidth = trackHeightPx,
                                cap = StrokeCap.Round
                            )
                        }

                        // Active track (Wavy or Straight)
                        if (normalizedValue > 0f) {
                            val activeTrackVisualEnd = (currentProgressPx - (thumbGapPx * thumbInteractionFraction))
                                .coerceAtLeast(localTrackStart)

                            if (waveAmplitudePx > 0.01f && waveFrequency > 0f && activeTrackVisualEnd > localTrackStart) {
                                wavePath.reset()
                                val waveStartDrawX = localTrackStart
                                val waveEndDrawX = activeTrackVisualEnd
                                val periodPx = (2f * PI.toFloat()) / waveFrequency
                                val samplesPerCycle = 20f
                                val waveStep = (periodPx / samplesPerCycle)
                                    .coerceAtLeast(1.2f)
                                    .coerceAtMost(trackHeightPx)

                                fun yAt(x: Float): Float {
                                    val s = sin(waveFrequency * (x - waveStartDrawX) + phaseShift)
                                    return (localCenterY + waveAmplitudePx * s)
                                        .coerceIn(
                                            localCenterY - waveAmplitudePx - trackHeightPx / 2f,
                                            localCenterY + waveAmplitudePx + trackHeightPx / 2f
                                        )
                                }

                                var prevX = waveStartDrawX
                                var prevY = yAt(prevX)
                                wavePath.moveTo(prevX, prevY)

                                var x = prevX + waveStep
                                while (x < waveEndDrawX) {
                                    val y = yAt(x)
                                    val midX = (prevX + x) * 0.5f
                                    val midY = (prevY + y) * 0.5f
                                    wavePath.quadraticTo(prevX, prevY, midX, midY)
                                    prevX = x
                                    prevY = y
                                    x += waveStep
                                }
                                val endY = yAt(waveEndDrawX)
                                wavePath.quadraticTo(prevX, prevY, waveEndDrawX, endY)

                                drawPath(
                                    path = wavePath,
                                    color = activeTrackColor,
                                    style = activeStroke
                                )
                            } else if (activeTrackVisualEnd > localTrackStart) {
                                drawLine(
                                    color = activeTrackColor,
                                    start = Offset(localTrackStart, localCenterY),
                                    end = Offset(activeTrackVisualEnd, localCenterY),
                                    strokeWidth = trackHeightPx,
                                    cap = StrokeCap.Round
                                )
                            }
                        }

                        // Morphing thumb (circle -> vertical pill on drag)
                        val currentThumbCenterX = localTrackStart + localTrackWidth * normalizedValue
                        val thumbCurrentWidthPx = thumbRadiusPx * 2f - (thumbRadiusPx * 1.1f) * thumbInteractionFraction
                        val thumbCurrentHeightPx = (thumbRadiusPx * 2f) + ((thumbLineHeightPx - (thumbRadiusPx * 2f)) * thumbInteractionFraction)

                        drawRoundRect(
                            color = thumbColor,
                            topLeft = Offset(
                                x = currentThumbCenterX - thumbCurrentWidthPx / 2f,
                                y = localCenterY - thumbCurrentHeightPx / 2f
                            ),
                            size = Size(thumbCurrentWidthPx, thumbCurrentHeightPx),
                            cornerRadius = CornerRadius(thumbCurrentWidthPx / 2f)
                        )
                    }
                }
        ) {}
    }
}

/**
 * Backward-compatible GlassSlider wrapper using Meowsic's WavyMusicSlider + time labels.
 */
@Composable
fun GlassSlider(
    positionMs: Long,
    durationMs: Long,
    palette: LiquidGlassTokens.AmbientPalette = LiquidGlassTokens.DefaultPalette,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier,
    isPlaying: Boolean = true
) {
    val safeDuration = max(durationMs, 1L)
    var isScrubbing by remember { mutableStateOf(false) }
    var scrubFraction by remember { mutableFloatStateOf(0f) }

    val liveFraction = (positionMs.toFloat() / safeDuration.toFloat()).coerceIn(0f, 1f)
    LaunchedEffect(liveFraction, isScrubbing) {
        if (!isScrubbing) {
            scrubFraction = liveFraction
        }
    }

    val displayedMs = if (isScrubbing) {
        (scrubFraction * safeDuration).toLong()
    } else {
        positionMs.coerceAtLeast(0L)
    }

    Column(modifier = modifier.fillMaxWidth()) {
        WavyMusicSlider(
            value = scrubFraction,
            onValueChange = { frac ->
                isScrubbing = true
                scrubFraction = frac.coerceIn(0f, 1f)
            },
            onValueChangeFinished = {
                val targetMs = (scrubFraction * safeDuration).toLong()
                isScrubbing = false
                onSeek(targetMs)
            },
            isPlaying = isPlaying,
            activeTrackColor = palette.primary,
            thumbColor = palette.primary,
            inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.22f)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatDurationMs(displayedMs),
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = if (durationMs > 0L) formatDurationMs(durationMs) else "--:--",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f)
            )
        }
    }
}

fun formatDurationMs(ms: Long): String {
    val totalSeconds = ceil(ms.coerceAtLeast(0L) / 1000.0).toLong()
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.US, "%d:%02d", minutes, seconds)
}
