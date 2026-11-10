package com.surya.meowsic.presentation.player

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.surya.meowsic.presentation.search.RepeatMode
import com.surya.meowsic.presentation.search.SearchViewModel
import com.surya.meowsic.ui.components.AmbientLightOrbs
import com.surya.meowsic.ui.components.LiquidGlassCard
import com.surya.meowsic.ui.components.LiquidOrbButton
import com.surya.meowsic.ui.theme.LiquidGlassTokens
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
    val position by viewModel.playbackPosition.collectAsState()
    val duration by viewModel.duration.collectAsState()
    val repeatMode by viewModel.repeatMode.collectAsState()
    val isShuffle by viewModel.isShuffle.collectAsState()
    val queue by viewModel.queue.collectAsState()
    val currentIndex by viewModel.currentIndex.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var isFavorite by remember { mutableStateOf(false) }
    var isQueueOpen by remember { mutableStateOf(false) }

    val song = currentSong ?: return

    // Dynamic ambient palette based on current song
    val palette = remember(song.id, song.title) {
        LiquidGlassTokens.getAmbientPaletteForSong(song.id + song.title)
    }

    // Smooth Artwork Scale Animation: 1.0 when playing, 0.92 with dimmed opacity when paused
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

    // True AMOLED Void Canvas (#000000)
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LiquidGlassTokens.AmoledVoid)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Localized Ambient Light Emitters behind primary glass chrome
        AmbientLightOrbs(
            palette = palette,
            opacity = 0.32f
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // ----------------------------------------------------
            // 1. TOP HEADER BAR
            // ----------------------------------------------------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Collapse Player",
                        tint = LiquidGlassTokens.TextPrimary,
                        modifier = Modifier.size(34.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "PLAYING FROM QUEUE",
                        color = LiquidGlassTokens.TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.4.sp
                    )
                    Text(
                        text = if (queue.isNotEmpty() && currentIndex >= 0) {
                            "Track ${currentIndex + 1} of ${queue.size}"
                        } else {
                            "Meowsic"
                        },
                        color = LiquidGlassTokens.TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                IconButton(onClick = { isQueueOpen = true }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                        contentDescription = "Open Queue",
                        tint = if (isQueueOpen) palette.primary else LiquidGlassTokens.TextPrimary,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ----------------------------------------------------
            // 2. NOW PLAYING VIEW & ALBUM ART HALO
            // ----------------------------------------------------
            Box(
                modifier = Modifier
                    .fillMaxWidth()
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
                    .background(LiquidGlassTokens.SurfaceOledFallback)
                    .border(
                        width = 1.dp,
                        brush = LiquidGlassTokens.SpecularRimGradient,
                        shape = RoundedCornerShape(24.dp)
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
                        modifier = Modifier.size(96.dp)
                    )
                }

                // Inner specular highlight bevel over artwork
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(
                            width = 1.dp,
                            color = LiquidGlassTokens.SpecularRimBorder,
                            shape = RoundedCornerShape(24.dp)
                        )
                )

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
                                text = if (isResolving) "Resolving liquid stream..." else "Buffering...",
                                color = LiquidGlassTokens.TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ----------------------------------------------------
            // 3. SONG TITLE & ARTIST ROW
            // ----------------------------------------------------
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
                        fontWeight = FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(onClick = { isFavorite = !isFavorite }) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (isFavorite) "Liked" else "Like",
                        tint = if (isFavorite) palette.tertiary else LiquidGlassTokens.TextPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ----------------------------------------------------
            // 4. LIQUID SCRUBBER / SEEK BAR
            // ----------------------------------------------------
            LiquidScrubber(
                positionMs = position,
                durationMs = duration.coerceAtLeast(0L),
                palette = palette,
                onSeek = { seekPos -> viewModel.seekTo(seekPos) }
            )

            Spacer(modifier = Modifier.height(18.dp))

            // ----------------------------------------------------
            // 5. TACTILE TRANSPORT CONTROLS
            // ----------------------------------------------------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 28.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shuffle Button
                TactileIconButton(
                    onClick = { viewModel.toggleShuffle() },
                    isActive = isShuffle,
                    activeColor = palette.primary,
                    icon = Icons.Default.Shuffle,
                    contentDescription = "Shuffle"
                )

                // Previous Button
                TactileIconButton(
                    onClick = { viewModel.playPrevious() },
                    icon = Icons.Default.SkipPrevious,
                    contentDescription = "Previous Song",
                    size = 48.dp,
                    iconSize = 36.dp
                )

                // Primary Play / Pause Liquid Orb
                LiquidOrbButton(
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

                // Next Button
                TactileIconButton(
                    onClick = { viewModel.playNext() },
                    icon = Icons.Default.SkipNext,
                    contentDescription = "Next Song",
                    size = 48.dp,
                    iconSize = 36.dp
                )

                // Repeat Button
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

            // Error banner if any
            errorMessage?.let { error ->
                LiquidGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
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

        // Up-Next Queue Modal / Bottom Sheet
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

/**
 * Liquid Scrubber / Seek Bar
 * Specs:
 * - Unfilled track: rgba(255, 255, 255, 0.12), height 6.dp, pill-shaped.
 * - Filled track: High-luminance gradient with soft outer glow.
 * - Interactive state: Expand track thickness to 10.dp on touch/drag with glowing specular thumb.
 */
@Composable
fun LiquidScrubber(
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

    // Interactive expansion: 6.dp default -> 10.dp when scrubbing
    val trackHeight by animateDpAsState(
        targetValue = if (isDragging) 10.dp else 6.dp,
        animationSpec = LiquidGlassTokens.TactileSpringSpecDp,
        label = "ScrubberHeight"
    )

    // Thumb radius expansion
    val thumbRadius by animateDpAsState(
        targetValue = if (isDragging) 9.dp else 0.dp,
        animationSpec = LiquidGlassTokens.TactileSpringSpecDp,
        label = "ThumbRadius"
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
                            val newProgress = (offset.x / barWidthPx).coerceIn(0f, 1f)
                            onSeek((newProgress * durationMs).toLong())
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
                            if (durationMs > 0) {
                                onSeek((dragProgress * durationMs).toLong())
                            }
                            isDragging = false
                        },
                        onDragCancel = {
                            isDragging = false
                        },
                        onHorizontalDrag = { change, _ ->
                            change.consume()
                            dragProgress = (change.position.x / barWidthPx).coerceIn(0f, 1f)
                        }
                    )
                },
            contentAlignment = Alignment.CenterStart
        ) {
            Canvas(modifier = Modifier.fillMaxWidth().height(trackHeight)) {
                val cornerRadius = CornerRadius(size.height / 2f, size.height / 2f)

                // 1. Unfilled track: rgba(255, 255, 255, 0.12)
                drawRoundRect(
                    color = Color(0x1FFFFFFF),
                    size = size,
                    cornerRadius = cornerRadius
                )

                // 2. Filled track: High-luminance gradient (Electric Cyan / Palette)
                val fillWidth = size.width * effectiveProgress
                if (fillWidth > 0f) {
                    drawRoundRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                palette.primary,
                                palette.secondary
                            ),
                            startX = 0f,
                            endX = size.width
                        ),
                        size = Size(fillWidth, size.height),
                        cornerRadius = cornerRadius
                    )

                    // Glowing specular thumb
                    if (thumbRadius.toPx() > 0f) {
                        drawCircle(
                            color = Color.White,
                            radius = thumbRadius.toPx(),
                            center = Offset(fillWidth, size.height / 2f)
                        )
                        // Specular rim
                        drawCircle(
                            color = palette.primary,
                            radius = thumbRadius.toPx() + 2.dp.toPx(),
                            center = Offset(fillWidth, size.height / 2f),
                            alpha = 0.5f
                        )
                    }
                }
            }
        }

        // Timestamps
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatTime(currentDisplayMs),
                color = LiquidGlassTokens.TextMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = formatTime(durationMs),
                color = LiquidGlassTokens.TextMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Tactile Secondary Transport Control
 */
@Composable
private fun TactileIconButton(
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 44.dp,
    iconSize: androidx.compose.ui.unit.Dp = 24.dp,
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
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = if (isActive) activeColor else LiquidGlassTokens.TextPrimary,
                modifier = Modifier.size(iconSize)
            )
            if (isActive) {
                Spacer(modifier = Modifier.height(3.dp))
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(activeColor)
                )
            }
        }
    }
}

/**
 * Formats milliseconds into mm:ss timestamp string.
 */
fun formatTime(milliseconds: Long): String {
    if (milliseconds <= 0) return "00:00"
    val totalSeconds = milliseconds / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
}
