package com.surya.meowsic.presentation.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.surya.meowsic.presentation.search.SearchViewModel
import com.surya.meowsic.ui.components.LiquidGlassPill
import com.surya.meowsic.ui.theme.LiquidGlassTokens

@Composable
fun MiniPlayer(
    viewModel: SearchViewModel,
    modifier: Modifier = Modifier,
    onOpenFullScreen: () -> Unit = { viewModel.openFullScreen() }
) {
    val currentSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val isResolving by viewModel.isResolving.collectAsState()
    val isBuffering by viewModel.isBuffering.collectAsState()
    val position by viewModel.playbackPosition.collectAsState()
    val duration by viewModel.duration.collectAsState()

    AnimatedVisibility(
        visible = currentSong != null,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = LiquidGlassTokens.SlideSpringSpec
        ),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = LiquidGlassTokens.SlideSpringSpec
        ),
        modifier = modifier
    ) {
        val song = currentSong ?: return@AnimatedVisibility

        val palette = remember(song.id, song.title) {
            LiquidGlassTokens.getAmbientPaletteForSong(song.id + song.title)
        }

        // Detached floating glass pill: 12.dp horizontal margin, 12.dp bottom offset, 28.dp radius
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            LiquidGlassPill(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                onClick = onOpenFullScreen
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Artwork with subtle specular border & ambient glow
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(LiquidGlassTokens.SurfaceOledFallback)
                                .border(
                                    width = 1.dp,
                                    brush = LiquidGlassTokens.SpecularRimGradient,
                                    shape = RoundedCornerShape(14.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (song.thumbnailUrl.isNotBlank()) {
                                AsyncImage(
                                    model = song.thumbnailUrl,
                                    contentDescription = song.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.MusicNote,
                                    contentDescription = null,
                                    tint = LiquidGlassTokens.TextMuted,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Song Title & Artist / Status
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = song.title,
                                color = LiquidGlassTokens.TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = LiquidGlassTokens.HeaderLetterSpacing,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = when {
                                    isResolving -> "Resolving stream..."
                                    isBuffering -> "Buffering..."
                                    else -> song.artist
                                },
                                color = if (isResolving || isBuffering) palette.primary else LiquidGlassTokens.TextSecondary,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Play / Pause Micro Liquid Orb
                        if (isResolving || isBuffering) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .padding(horizontal = 6.dp)
                                    .size(24.dp),
                                color = palette.primary,
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            IconButton(
                                onClick = { viewModel.togglePlayPause() },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(palette.primary.copy(alpha = 0.22f))
                                        .border(
                                            width = 1.dp,
                                            color = palette.primary.copy(alpha = 0.6f),
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = if (isPlaying) "Pause" else "Play",
                                        tint = palette.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        // Skip Next Button
                        IconButton(
                            onClick = { viewModel.playNext() },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipNext,
                                contentDescription = "Next Song",
                                tint = LiquidGlassTokens.TextPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    // Integrated Micro Scrubber / Progress track at bottom of the pill
                    val progress = if (duration > 0) {
                        (position.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
                    } else if (isResolving || isBuffering) {
                        0.5f
                    } else {
                        0f
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.5.dp)
                            .background(Color(0x1FFFFFFF))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress)
                                .fillMaxHeight()
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(palette.primary, palette.secondary)
                                    )
                                )
                        )
                    }
                }
            }
        }
    }
}
