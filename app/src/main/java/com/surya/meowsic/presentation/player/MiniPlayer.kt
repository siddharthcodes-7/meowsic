package com.surya.meowsic.presentation.player

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.surya.meowsic.presentation.search.SearchViewModel
import com.surya.meowsic.ui.glass.AutoScrollingText
import com.surya.meowsic.ui.glass.LocalAmbientPalette
import kotlin.math.max

/**
 * Meowsic MiniPlayer implementation: presentation/components/UnifiedPlayerSheetShared.kt (MiniPlayerContentInternal)
 */
@Composable
fun MiniPlayer(
    viewModel: SearchViewModel,
    modifier: Modifier = Modifier,
    onOpenFullScreen: () -> Unit = { viewModel.openFullScreen() }
) {
    val song by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val isResolving by viewModel.isResolving.collectAsState()
    val isBuffering by viewModel.isBuffering.collectAsState()
    val positionMs by viewModel.playbackPosition.collectAsState()
    val durationMs by viewModel.duration.collectAsState()

    val currentSong = song ?: return
    val palette = LocalAmbientPalette.current
    val haptic = LocalHapticFeedback.current

    val isLoading = isResolving || isBuffering
    val progress = if (durationMs > 0L) {
        (positionMs.toFloat() / max(durationMs, 1L).toFloat()).coerceIn(0f, 1f)
    } else 0f

    val containerBg by animateColorAsState(
        targetValue = palette.primaryContainer,
        animationSpec = tween(400),
        label = "miniPlayerBg"
    )

    var horizontalDrag by remember { mutableFloatStateOf(0f) }
    var verticalDrag by remember { mutableFloatStateOf(0f) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onOpenFullScreen()
            }
            .pointerInput(currentSong.id) {
                detectHorizontalDragGestures(
                    onDragStart = { horizontalDrag = 0f },
                    onDragEnd = {
                        if (horizontalDrag < -90f) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.playNext(true)
                        } else if (horizontalDrag > 90f) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.playPrevious()
                        }
                        horizontalDrag = 0f
                    }
                ) { change, dragAmount ->
                    change.consume()
                    horizontalDrag += dragAmount
                }
            }
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragStart = { verticalDrag = 0f },
                    onDragEnd = {
                        if (verticalDrag < -45f) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onOpenFullScreen()
                        }
                        verticalDrag = 0f
                    }
                ) { change, dragAmount ->
                    change.consume()
                    verticalDrag += dragAmount
                }
            },
        shape = RoundedCornerShape(28.dp),
        color = containerBg,
        tonalElevation = 8.dp,
        shadowElevation = 10.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(66.dp)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular Artwork + Buffering Spinner Overlay (Meowsic style)
                Box(
                    modifier = Modifier.size(46.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        if (currentSong.thumbnailUrl.isNotBlank()) {
                            AsyncImage(
                                model = currentSong.thumbnailUrl,
                                contentDescription = currentSong.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.MusicNote,
                                    contentDescription = null,
                                    tint = palette.onPrimaryContainer
                                )
                            }
                        }
                    }

                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(46.dp),
                            color = palette.primary,
                            strokeWidth = 2.5.dp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // AutoScrolling Title & Artist
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    AutoScrollingText(
                        text = currentSong.title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = palette.onPrimaryContainer
                    )
                    AutoScrollingText(
                        text = if (isResolving) "Loading stream..." else currentSong.artist,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp),
                        color = palette.onPrimaryContainer.copy(alpha = 0.75f)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Previous Button (36.dp circle)
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(palette.onPrimary)
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.playPrevious()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SkipPrevious,
                        contentDescription = "Previous",
                        tint = palette.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Play/Pause Button (38.dp circle in Primary)
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(palette.primary)
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.togglePlayPause()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = palette.onPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Next Button (36.dp circle)
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(palette.onPrimary)
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.playNext(true)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SkipNext,
                        contentDescription = "Next",
                        tint = palette.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .padding(horizontal = 16.dp),
                color = palette.primary,
                trackColor = palette.onPrimaryContainer.copy(alpha = 0.16f),
                strokeCap = StrokeCap.Round
            )
        }
    }
}
