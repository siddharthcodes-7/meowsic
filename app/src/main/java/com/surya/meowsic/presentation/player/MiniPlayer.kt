package com.surya.meowsic.presentation.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.surya.meowsic.presentation.library.LibraryViewModel
import com.surya.meowsic.presentation.search.SearchViewModel
import com.surya.meowsic.ui.glass.GlassSurface
import com.surya.meowsic.ui.glass.GlassVariant
import com.surya.meowsic.ui.glass.GlassTokens
import com.surya.meowsic.ui.theme.LiquidGlassTokens
import com.surya.meowsic.ui.theme.LocalAmbientPalette

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
    val palette = LocalAmbientPalette.current
    val libraryViewModel: LibraryViewModel = viewModel()

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
        val isLiked by libraryViewModel.isLiked(song.id).collectAsState(initial = false)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 4.dp)
        ) {
            GlassSurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onOpenFullScreen
                    ),
                variant = GlassVariant.ELEVATED,
                shape = RoundedCornerShape(GlassTokens.CornerMiniPlayer)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Main row: artwork | title+artist | like | play | skip
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(72.dp)
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Artwork
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF181818)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (song.thumbnailUrl.isNotBlank()) {
                                AsyncImage(
                                    model = song.thumbnailUrl,
                                    contentDescription = song.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Title & artist
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = song.title,
                                color = LiquidGlassTokens.TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
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
                                color = if (isResolving || isBuffering)
                                    palette.primary
                                else
                                    LiquidGlassTokens.TextSecondary,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        // Heart / Like button
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() },
                                    onClick = { libraryViewModel.toggleLike(song) }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = if (isLiked) "Unlike" else "Like",
                                tint = if (isLiked) palette.primary else LiquidGlassTokens.TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        // Play / Pause
                        if (isResolving || isBuffering) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .padding(horizontal = 6.dp)
                                    .size(24.dp),
                                color = palette.primary,
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(
                                                palette.primary.copy(alpha = 0.90f),
                                                palette.secondary.copy(alpha = 0.80f)
                                            )
                                        )
                                    )
                                    .clickable(
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() },
                                        onClick = { viewModel.togglePlayPause() }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (isPlaying) "Pause" else "Play",
                                    tint = Color.Black,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        // Skip next
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() },
                                    onClick = { viewModel.playNext() }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipNext,
                                contentDescription = "Next Song",
                                tint = LiquidGlassTokens.TextPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))
                    }

                    // Progress bar — palette gradient fill
                    val progress = if (duration > 0) {
                        (position.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
                    } else {
                        0f
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(Color(0x1AFFFFFF))
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
