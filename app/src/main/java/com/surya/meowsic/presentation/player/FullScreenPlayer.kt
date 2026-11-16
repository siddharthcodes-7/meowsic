package com.surya.meowsic.presentation.player

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PlaylistAdd
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.surya.meowsic.presentation.library.AddToPlaylistSheet
import com.surya.meowsic.presentation.library.LibraryViewModel
import com.surya.meowsic.presentation.search.RepeatMode
import com.surya.meowsic.presentation.search.SearchViewModel
import com.surya.meowsic.ui.glass.AutoScrollingText
import com.surya.meowsic.ui.glass.GlassSlider
import com.surya.meowsic.ui.glass.LocalAmbientPalette
import com.surya.meowsic.ui.glass.ToggleSegmentButton
import com.surya.meowsic.ui.theme.LiquidGlassTokens
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private enum class LastClickedControl { NONE, PREVIOUS, PLAY_PAUSE, NEXT }

/**
 * Meowsic full screen player implementation:
 * - presentation/components/player/FullPlayerContent.kt
 * - presentation/components/player/AnimatedPlaybackControls.kt
 * - presentation/components/player/BottomToggleRow.kt
 */
@Composable
fun FullScreenPlayer(
    viewModel: SearchViewModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val libraryViewModel: LibraryViewModel = viewModel()
    val song by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val isResolving by viewModel.isResolving.collectAsState()
    val isBuffering by viewModel.isBuffering.collectAsState()
    val positionMs by viewModel.playbackPosition.collectAsState()
    val durationMs by viewModel.duration.collectAsState()
    val isShuffle by viewModel.isShuffle.collectAsState()
    val repeatMode by viewModel.repeatMode.collectAsState()
    val queue by viewModel.queue.collectAsState()
    val currentIndex by viewModel.currentIndex.collectAsState()

    val currentSong = song ?: return
    val isLiked by remember(currentSong.id) {
        libraryViewModel.isLiked(currentSong.id)
    }.collectAsState(initial = false)
    val playlists by libraryViewModel.playlistsWithCount.collectAsState()

    var showQueueSheet by remember { mutableStateOf(false) }
    var showAddToPlaylistSheet by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    val palette = LocalAmbientPalette.current
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    var dragOffsetY by remember { mutableFloatStateOf(0f) }
    val animatedDragY by animateFloatAsState(
        targetValue = dragOffsetY,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "playerDragY"
    )

    val artworkScale by animateFloatAsState(
        targetValue = if (isPlaying) 1f else 0.92f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "artworkScale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer {
                translationY = animatedDragY.coerceAtLeast(0f)
            }
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        palette.primaryContainer,
                        palette.surfaceContainerHigh,
                        palette.surfaceContainer
                    )
                )
            )
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragStart = { dragOffsetY = 0f },
                    onDragEnd = {
                        if (dragOffsetY > 160f) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onDismiss()
                        }
                        dragOffsetY = 0f
                    },
                    onDragCancel = { dragOffsetY = 0f }
                ) { change, dragAmount ->
                    if (dragOffsetY + dragAmount >= 0f) {
                        change.consume()
                        dragOffsetY += dragAmount
                    }
                }
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = statusBarPadding + 12.dp,
                    bottom = navBarPadding + 16.dp,
                    start = 24.dp,
                    end = 24.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Meowsic SubHeader Top Bar (Collapse button + Next Up Queue Pill)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledIconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onDismiss()
                    },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = palette.onPrimaryContainer.copy(alpha = 0.12f),
                        contentColor = palette.onPrimaryContainer
                    ),
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowDown,
                        contentDescription = "Collapse Player"
                    )
                }

                Surface(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        showQueueSheet = true
                    },
                    shape = CircleShape,
                    color = palette.onPrimaryContainer.copy(alpha = 0.12f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.QueueMusic,
                            contentDescription = "Queue",
                            tint = palette.onPrimaryContainer,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Playing Queue (${queue.size})",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = palette.onPrimaryContainer
                        )
                    }
                }
            }

            // 2. Expressive Album Cover Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .aspectRatio(1f)
                        .graphicsLayer {
                            scaleX = artworkScale
                            scaleY = artworkScale
                        }
                        .shadow(
                            elevation = if (isPlaying) 28.dp else 10.dp,
                            shape = RoundedCornerShape(32.dp),
                            spotColor = palette.primary
                        ),
                    shape = RoundedCornerShape(32.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        if (currentSong.thumbnailUrl.isNotBlank()) {
                            AsyncImage(
                                model = currentSong.thumbnailUrl,
                                contentDescription = currentSong.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.MusicNote,
                                contentDescription = null,
                                tint = palette.onPrimaryContainer,
                                modifier = Modifier.size(84.dp)
                            )
                        }

                        if (isResolving || isBuffering) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.32f)),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = palette.primary,
                                    strokeWidth = 3.5.dp,
                                    modifier = Modifier.size(52.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 3. Song Metadata + Add to Playlist Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    AutoScrollingText(
                        text = currentSong.title,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        ),
                        color = palette.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    AutoScrollingText(
                        text = currentSong.artist,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = palette.onPrimaryContainer.copy(alpha = 0.78f)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                FilledIconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        showAddToPlaylistSheet = true
                    },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = palette.onPrimaryContainer.copy(alpha = 0.14f),
                        contentColor = palette.onPrimaryContainer
                    ),
                    modifier = Modifier.size(46.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PlaylistAdd,
                        contentDescription = "Add to Playlist"
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 4. Meowsic WavyMusicSlider Seek Bar
            GlassSlider(
                positionMs = positionMs,
                durationMs = durationMs,
                palette = palette,
                onSeek = { viewModel.seekTo(it) },
                isPlaying = isPlaying
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 5. Meowsic AnimatedPlaybackControls (Morphing Weights + Play/Pause Squircle)
            MeowsicAnimatedPlaybackControls(
                isPlaying = isPlaying,
                palette = palette,
                onPrevious = { viewModel.playPrevious() },
                onPlayPause = { viewModel.togglePlayPause() },
                onNext = { viewModel.playNext(true) }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 6. Meowsic BottomToggleRow (Segmented Shuffle, Repeat, Like Bar)
            MeowsicBottomToggleRow(
                isShuffleEnabled = isShuffle,
                repeatMode = repeatMode,
                isFavorite = isLiked,
                palette = palette,
                onShuffleToggle = { viewModel.toggleShuffle() },
                onRepeatToggle = { viewModel.toggleRepeat() },
                onFavoriteToggle = { libraryViewModel.toggleLike(currentSong) }
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = navBarPadding + 16.dp)
        )
    }

    if (showQueueSheet) {
        QueueBottomSheet(
            queue = queue,
            currentIndex = currentIndex,
            palette = palette,
            onSongClick = { clickedSong ->
                viewModel.playSong(clickedSong, queue)
            },
            onDismiss = { showQueueSheet = false }
        )
    }

    if (showAddToPlaylistSheet) {
        AddToPlaylistSheet(
            song = currentSong,
            playlists = playlists,
            onAddToPlaylist = { playlistId ->
                libraryViewModel.addSongToPlaylist(playlistId, currentSong) { added ->
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            if (added) "Added to playlist" else "Song already in playlist"
                        )
                    }
                }
                showAddToPlaylistSheet = false
            },
            onDismiss = { showAddToPlaylistSheet = false },
            onCreatePlaylist = { newName ->
                scope.launch {
                    val newId = libraryViewModel.createPlaylist(newName, null)
                    libraryViewModel.addSongToPlaylist(newId, currentSong) {}
                    snackbarHostState.showSnackbar("Created '$newName' & added song")
                }
                showAddToPlaylistSheet = false
            }
        )
    }
}

/**
 * Meowsic AnimatedPlaybackControls: presentation/components/player/AnimatedPlaybackControls.kt
 */
@Composable
private fun MeowsicAnimatedPlaybackControls(
    isPlaying: Boolean,
    palette: LiquidGlassTokens.AmbientPalette,
    onPrevious: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    height: Dp = 86.dp
) {
    val haptic = LocalHapticFeedback.current
    var lastClicked by remember { mutableStateOf<LastClickedControl>(LastClickedControl.NONE) }

    LaunchedEffect(lastClicked) {
        if (lastClicked != LastClickedControl.NONE) {
            delay(280L)
            lastClicked = LastClickedControl.NONE
        }
    }

    val weightSpec = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMediumLow
    )

    val prevWeight by animateFloatAsState(
        targetValue = when (lastClicked) {
            LastClickedControl.PREVIOUS -> 1.18f
            LastClickedControl.NONE -> 1f
            else -> 0.72f
        },
        animationSpec = weightSpec,
        label = "prevWeight"
    )
    val playWeight by animateFloatAsState(
        targetValue = when (lastClicked) {
            LastClickedControl.PLAY_PAUSE -> 1.22f
            LastClickedControl.NONE -> 1.08f
            else -> 0.78f
        },
        animationSpec = weightSpec,
        label = "playWeight"
    )
    val nextWeight by animateFloatAsState(
        targetValue = when (lastClicked) {
            LastClickedControl.NEXT -> 1.18f
            LastClickedControl.NONE -> 1f
            else -> 0.72f
        },
        animationSpec = weightSpec,
        label = "nextWeight"
    )

    val playCorner by animateDpAsState(
        targetValue = if (!isPlaying) 50.dp else 24.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "playCorner"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(height),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Previous Button
        Box(
            modifier = Modifier
                .weight(prevWeight)
                .fillMaxHeight()
                .clip(CircleShape)
                .background(palette.secondary)
                .clickable {
                    lastClicked = LastClickedControl.PREVIOUS
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onPrevious()
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.SkipPrevious,
                contentDescription = "Previous",
                tint = Color(0xFF281020),
                modifier = Modifier.size(34.dp)
            )
        }

        // Play / Pause Morphing Squircle Button
        Box(
            modifier = Modifier
                .weight(playWeight)
                .fillMaxHeight()
                .clip(RoundedCornerShape(playCorner))
                .background(palette.primary)
                .clickable {
                    lastClicked = LastClickedControl.PLAY_PAUSE
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onPlayPause()
                },
            contentAlignment = Alignment.Center
        ) {
            Crossfade(
                targetState = isPlaying,
                animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
                label = "playPauseCrossfade"
            ) { playing ->
                Icon(
                    imageVector = if (playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = if (playing) "Pause" else "Play",
                    tint = palette.onPrimary,
                    modifier = Modifier.size(38.dp)
                )
            }
        }

        // Next Button
        Box(
            modifier = Modifier
                .weight(nextWeight)
                .fillMaxHeight()
                .clip(CircleShape)
                .background(palette.secondary)
                .clickable {
                    lastClicked = LastClickedControl.NEXT
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onNext()
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.SkipNext,
                contentDescription = "Next",
                tint = Color(0xFF281020),
                modifier = Modifier.size(34.dp)
            )
        }
    }
}

/**
 * Meowsic BottomToggleRow: presentation/components/player/BottomToggleRow.kt
 */
@Composable
private fun MeowsicBottomToggleRow(
    isShuffleEnabled: Boolean,
    repeatMode: RepeatMode,
    isFavorite: Boolean,
    palette: LiquidGlassTokens.AmbientPalette,
    onShuffleToggle: () -> Unit,
    onRepeatToggle: () -> Unit,
    onFavoriteToggle: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val rowCornerRadius = 60.dp
    val isRepeatActive = repeatMode != RepeatMode.OFF

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(62.dp)
            .clip(RoundedCornerShape(rowCornerRadius))
            .background(palette.surfaceContainer)
            .padding(5.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ToggleSegmentButton(
                modifier = Modifier.weight(1f),
                active = isShuffleEnabled,
                activeColor = palette.primary,
                activeCornerRadius = rowCornerRadius,
                activeContentColor = palette.onPrimary,
                inactiveColor = palette.surfaceContainerHigh,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onShuffleToggle()
                }
            ) {
                Icon(
                    imageVector = Icons.Rounded.Shuffle,
                    contentDescription = "Shuffle",
                    tint = if (isShuffleEnabled) palette.onPrimary else palette.onPrimaryContainer.copy(alpha = 0.78f),
                    modifier = Modifier.size(24.dp)
                )
            }

            ToggleSegmentButton(
                modifier = Modifier.weight(1f),
                active = isRepeatActive,
                activeColor = palette.secondary,
                activeCornerRadius = rowCornerRadius,
                activeContentColor = Color(0xFF281020),
                inactiveColor = palette.surfaceContainerHigh,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onRepeatToggle()
                }
            ) {
                val repeatIcon = when (repeatMode) {
                    RepeatMode.ONE -> Icons.Rounded.RepeatOne
                    else -> Icons.Rounded.Repeat
                }
                Icon(
                    imageVector = repeatIcon,
                    contentDescription = "Repeat",
                    tint = if (isRepeatActive) Color(0xFF281020) else palette.onPrimaryContainer.copy(alpha = 0.78f),
                    modifier = Modifier.size(24.dp)
                )
            }

            ToggleSegmentButton(
                modifier = Modifier.weight(1f),
                active = isFavorite,
                activeColor = palette.tertiary,
                activeCornerRadius = rowCornerRadius,
                activeContentColor = Color(0xFF2D1206),
                inactiveColor = palette.surfaceContainerHigh,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onFavoriteToggle()
                }
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (isFavorite) Color(0xFF2D1206) else palette.onPrimaryContainer.copy(alpha = 0.78f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
