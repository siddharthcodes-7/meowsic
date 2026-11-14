package com.surya.meowsic.presentation.player

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.surya.meowsic.presentation.library.LibraryViewModel
import com.surya.meowsic.presentation.search.RepeatMode
import com.surya.meowsic.presentation.search.SearchViewModel
import com.surya.meowsic.ui.glass.AmbientBackground
import com.surya.meowsic.ui.glass.GlassIconButton
import com.surya.meowsic.ui.glass.GlassProminentButton
import com.surya.meowsic.ui.glass.GlassSlider
import com.surya.meowsic.ui.glass.GlassSurface
import com.surya.meowsic.ui.glass.GlassVariant
import com.surya.meowsic.ui.theme.LiquidGlassTokens
import com.surya.meowsic.ui.theme.LocalAmbientPalette
import java.util.Locale

@Composable
fun FullScreenPlayer(
    viewModel: SearchViewModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val isResolving by viewModel.isResolving.collectAsState()
    val isBuffering by viewModel.isBuffering.collectAsState()
    val repeatMode by viewModel.repeatMode.collectAsState()
    val isShuffle by viewModel.isShuffle.collectAsState()
    val queue by viewModel.queue.collectAsState()
    val currentIndex by viewModel.currentIndex.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var isQueueOpen by remember { mutableStateOf(false) }
    val libraryViewModel: LibraryViewModel = viewModel()

    val song = currentSong ?: return

    val palette = remember(song.id, song.title) {
        LiquidGlassTokens.getAmbientPaletteForSong(song.id + song.title)
    }
    val globalPalette = LocalAmbientPalette.current

    val isLiked by libraryViewModel.isLiked(song.id).collectAsState(initial = false)

    // Artwork spring animation
    val artScale by animateFloatAsState(
        targetValue = if (isPlaying) 1.0f else 0.92f,
        animationSpec = LiquidGlassTokens.TactileSpringSpecFloat,
        label = "ArtworkScale"
    )
    val artAlpha by animateFloatAsState(
        targetValue = if (isPlaying) 1.0f else 0.78f,
        animationSpec = LiquidGlassTokens.TactileSpringSpecFloat,
        label = "ArtworkAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LiquidGlassTokens.AmoledVoid)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Animated ambient background (glass system)
        AmbientBackground(palette = palette, modifier = Modifier.fillMaxSize(), opacity = 0.28f)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // ── 1. TOP BAR ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                GlassIconButton(onClick = onDismiss, size = 44.dp) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Collapse Player",
                        tint = LiquidGlassTokens.TextPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "NOW PLAYING",
                        color = LiquidGlassTokens.TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.4.sp
                    )
                    Text(
                        text = if (queue.isNotEmpty() && currentIndex >= 0) {
                            "Track ${currentIndex + 1} of ${queue.size}"
                        } else { "Meowsic" },
                        color = LiquidGlassTokens.TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                GlassIconButton(onClick = { isQueueOpen = true }, size = 44.dp) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                        contentDescription = "Open Queue",
                        tint = if (isQueueOpen) palette.primary else LiquidGlassTokens.TextPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── 2. ARTWORK ──
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.84f)
                    .aspectRatio(1f)
                    .graphicsLayer {
                        scaleX = artScale
                        scaleY = artScale
                        alpha = artAlpha
                    }
                    .shadow(
                        elevation = 32.dp,
                        shape = RoundedCornerShape(24.dp),
                        ambientColor = palette.primary,
                        spotColor = palette.secondary
                    )
                    .clip(RoundedCornerShape(24.dp))
                    .background(LiquidGlassTokens.SurfaceOledFallback),
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
                        modifier = Modifier.size(96.dp)
                    )
                }

                if (isResolving || isBuffering) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.55f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                color = palette.primary,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(42.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = if (isResolving) "Resolving stream..." else "Buffering...",
                                color = LiquidGlassTokens.TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── 3. SONG TITLE & ARTIST + LIKE BUTTON ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = song.title,
                        color = LiquidGlassTokens.TextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = LiquidGlassTokens.HeaderLetterSpacing,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = song.artist,
                        color = LiquidGlassTokens.TextSecondary,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Animated heart button (real like, persisted)
                val heartScale by animateFloatAsState(
                    targetValue = if (isLiked) 1.15f else 1f,
                    animationSpec = LiquidGlassTokens.TactileSpringSpecFloat,
                    label = "HeartScale"
                )
                IconButton(
                    onClick = { libraryViewModel.toggleLike(song) },
                    modifier = Modifier.graphicsLayer { scaleX = heartScale; scaleY = heartScale }
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (isLiked) "Unlike" else "Like",
                        tint = if (isLiked) palette.primary else LiquidGlassTokens.TextPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── 4. SEEK BAR (GlassSlider) ──
            FullScreenProgressSection(
                viewModel = viewModel,
                palette = palette
            )

            Spacer(modifier = Modifier.height(18.dp))

            // ── 5. TRANSPORT CONTROLS ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 28.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TactileIconButton(
                    onClick = { viewModel.toggleShuffle() },
                    isActive = isShuffle,
                    activeColor = palette.primary,
                    icon = Icons.Default.Shuffle,
                    contentDescription = "Shuffle"
                )

                TactileIconButton(
                    onClick = { viewModel.playPrevious() },
                    icon = Icons.Default.SkipPrevious,
                    contentDescription = "Previous Song",
                    size = 48.dp,
                    iconSize = 36.dp
                )

                // Primary play/pause — GlassProminentButton (glass system)
                GlassProminentButton(
                    onClick = { viewModel.togglePlayPause() },
                    size = 72.dp,
                    palette = palette
                ) {
                    if (isResolving || isBuffering) {
                        CircularProgressIndicator(
                            color = Color.Black,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(32.dp)
                        )
                    } else {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.Black,
                            modifier = Modifier.size(38.dp)
                        )
                    }
                }

                TactileIconButton(
                    onClick = { viewModel.playNext() },
                    icon = Icons.Default.SkipNext,
                    contentDescription = "Next Song",
                    size = 48.dp,
                    iconSize = 36.dp
                )

                TactileIconButton(
                    onClick = { viewModel.toggleRepeat() },
                    isActive = repeatMode != RepeatMode.OFF,
                    activeColor = palette.primary,
                    icon = when (repeatMode) {
                        RepeatMode.ONE -> Icons.Default.RepeatOne
                        else -> Icons.Default.Repeat
                    },
                    contentDescription = "Repeat"
                )
            }

            // ── Error banner ──
            errorMessage?.let { error ->
                GlassSurface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    variant = GlassVariant.PRIMARY,
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = error,
                            color = LiquidGlassTokens.Crimson,
                            fontSize = 12.sp,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text("Dismiss", color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Queue Bottom Sheet
        if (isQueueOpen) {
            QueueBottomSheet(
                queue = queue,
                currentIndex = currentIndex,
                palette = palette,
                onSongClick = { selectedSong ->
                    viewModel.playSong(selectedSong, playlist = queue)
                },
                onDismiss = { isQueueOpen = false }
            )
        }
    }
}

@Composable
private fun FullScreenProgressSection(
    viewModel: SearchViewModel,
    palette: LiquidGlassTokens.AmbientPalette
) {
    val position by viewModel.playbackPosition.collectAsState()
    val duration by viewModel.duration.collectAsState()

    GlassSlider(
        positionMs = position,
        durationMs = duration.coerceAtLeast(0L),
        palette = palette,
        onSeek = { seekPos -> viewModel.seekTo(seekPos) }
    )
}

// ── TactileIconButton ──
@Composable
private fun TactileIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    iconSize: Dp = 24.dp,
    isActive: Boolean = false,
    activeColor: Color = LiquidGlassTokens.ElectricLime
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1.0f,
        animationSpec = LiquidGlassTokens.TactileSpringSpecFloat,
        label = "IconScale"
    )
    IconButton(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = modifier.size(size).graphicsLayer { scaleX = scale; scaleY = scale }
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = if (isActive) activeColor else LiquidGlassTokens.TextPrimary,
                modifier = Modifier.size(iconSize)
            )
            if (isActive) {
                Spacer(modifier = Modifier.height(3.dp))
                Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(activeColor))
            }
        }
    }
}

/** Formats milliseconds into mm:ss timestamp string. */
fun formatTime(milliseconds: Long): String {
    if (milliseconds <= 0) return "00:00"
    val totalSeconds = milliseconds / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
}
