package com.surya.meowsic.presentation.player

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.surya.meowsic.presentation.search.RepeatMode
import com.surya.meowsic.presentation.search.SearchViewModel
import java.util.Locale

private val BackgroundColor = Color(0xFF101010)
private val AccentColor = Color(0xFFB9F36A)
private val TextSecondaryColor = Color(0xFFB3B3B3)

@OptIn(ExperimentalMaterial3Api::class)
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

    // Track slider dragging locally to prevent jumpy playback position updates
    var sliderPosition by remember { mutableStateOf<Float?>(null) }
    var isFavorite by remember { mutableStateOf(false) }

    val song = currentSong ?: return

    val currentMs = sliderPosition?.toLong() ?: position
    val effectiveDuration = duration.coerceAtLeast(0L)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF222B18),
                        BackgroundColor,
                        BackgroundColor
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // --------------------------------
            // TOP HEADER BAR
            // --------------------------------
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
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "PLAYING FROM QUEUE",
                        color = Color(0xFFAAAAAA),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = if (queue.isNotEmpty() && currentIndex >= 0) {
                            "Track ${currentIndex + 1} of ${queue.size}"
                        } else {
                            "Meowsic"
                        },
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                IconButton(onClick = { /* Additional actions if needed */ }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --------------------------------
            // LARGE ALBUM ARTWORK
            // --------------------------------
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .shadow(
                        elevation = 20.dp,
                        shape = RoundedCornerShape(20.dp),
                        clip = false,
                        ambientColor = Color.Black,
                        spotColor = Color.Black
                    )
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF1E1E1E)),
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
                        tint = Color.DarkGray,
                        modifier = Modifier.size(96.dp)
                    )
                }

                if (isResolving || isBuffering) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.45f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                color = AccentColor,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(42.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = if (isResolving) "Extracting stream..." else "Buffering...",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // --------------------------------
            // SONG TITLE & ARTIST ROW
            // --------------------------------
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
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = song.artist,
                        color = TextSecondaryColor,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(onClick = { isFavorite = !isFavorite }) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (isFavorite) "Liked" else "Like",
                        tint = if (isFavorite) AccentColor else Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // --------------------------------
            // SEEK BAR & TIMESTAMPS
            // --------------------------------
            Column(modifier = Modifier.fillMaxWidth()) {
                val sliderMax = if (effectiveDuration > 0) effectiveDuration.toFloat() else 1f
                val sliderVal = currentMs.toFloat().coerceIn(0f, sliderMax)

                Slider(
                    value = sliderVal,
                    onValueChange = { newPos ->
                        sliderPosition = newPos
                    },
                    onValueChangeFinished = {
                        sliderPosition?.let { pos ->
                            viewModel.seekTo(pos.toLong())
                        }
                        sliderPosition = null
                    },
                    valueRange = 0f..sliderMax,
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = AccentColor,
                        inactiveTrackColor = Color(0xFF333333)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatTime(currentMs),
                        color = TextSecondaryColor,
                        fontSize = 12.sp
                    )
                    Text(
                        text = formatTime(effectiveDuration),
                        color = TextSecondaryColor,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // --------------------------------
            // PLAYBACK CONTROLS (Shuffle, Prev, Play/Pause, Next, Repeat)
            // --------------------------------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shuffle Button
                IconButton(
                    onClick = { viewModel.toggleShuffle() },
                    modifier = Modifier.size(44.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shuffle,
                            contentDescription = "Shuffle",
                            tint = if (isShuffle) AccentColor else Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                        if (isShuffle) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(AccentColor)
                            )
                        }
                    }
                }

                // Previous Button
                IconButton(
                    onClick = { viewModel.playPrevious() },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous Song",
                        tint = Color.White,
                        modifier = Modifier.size(38.dp)
                    )
                }

                // Play / Pause Prominent Circular Accent Button
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(CircleShape)
                        .background(AccentColor)
                        .clickable { viewModel.togglePlayPause() },
                    contentAlignment = Alignment.Center
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
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                // Next Button
                IconButton(
                    onClick = { viewModel.playNext() },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next Song",
                        tint = Color.White,
                        modifier = Modifier.size(38.dp)
                    )
                }

                // Repeat Button
                IconButton(
                    onClick = { viewModel.toggleRepeat() },
                    modifier = Modifier.size(44.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = when (repeatMode) {
                                RepeatMode.ONE -> Icons.Default.RepeatOne
                                else -> Icons.Default.Repeat
                            },
                            contentDescription = "Repeat",
                            tint = if (repeatMode != RepeatMode.OFF) AccentColor else Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                        if (repeatMode != RepeatMode.OFF) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(AccentColor)
                            )
                        }
                    }
                }
            }

            // --------------------------------
            // ERROR BANNER IF PRESENT
            // --------------------------------
            errorMessage?.let { error ->
                Surface(
                    color = Color(0xFF2A1515),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = error,
                            color = Color(0xFFFF6B6B),
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
