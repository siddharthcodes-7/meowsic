package com.surya.meowsic.presentation.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.surya.meowsic.domain.Song
import com.surya.meowsic.ui.components.LiquidGlassCard
import com.surya.meowsic.ui.theme.LiquidGlassTokens
import kotlinx.coroutines.delay

@Composable
fun SearchScreen(
    viewModel: SearchViewModel
) {
    // Local mutable state for the text field — allows instant UI updates while debouncing search
    var searchQuery by remember { mutableStateOf("") }

    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val isResolving by viewModel.isResolving.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val currentSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()

    // 500ms debounce: search fires 500ms after the user stops typing
    LaunchedEffect(searchQuery) {
        if (searchQuery.length >= 2) {
            delay(500L)
            viewModel.searchYouTube(searchQuery)
        } else {
            viewModel.searchYouTube(searchQuery) // clears results for short queries
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LiquidGlassTokens.AmoledVoid)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // --------------------------------
        // TITLE HEADER
        // --------------------------------
        Text(
            text = "Search",
            color = LiquidGlassTokens.TextPrimary,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = LiquidGlassTokens.HeaderLetterSpacing,
            modifier = Modifier.padding(bottom = 14.dp)
        )

        // --------------------------------
        // SEARCH INPUT BAR (LIQUID GLASS STYLED)
        // --------------------------------
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { newValue ->
                searchQuery = newValue
                viewModel.onSearchQueryChanged(newValue)
            },
            placeholder = {
                Text(
                    text = "Search songs, artists, channels...",
                    color = LiquidGlassTokens.TextMuted,
                    fontSize = 15.sp
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = if (searchQuery.isNotEmpty()) LiquidGlassTokens.ElectricLime else LiquidGlassTokens.TextMuted
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = {
                        searchQuery = ""
                        viewModel.onSearchQueryChanged("")
                    }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear search",
                            tint = LiquidGlassTokens.TextSecondary
                        )
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    brush = LiquidGlassTokens.SpecularRimGradient,
                    shape = RoundedCornerShape(18.dp)
                ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = LiquidGlassTokens.SurfaceOledFallback,
                unfocusedContainerColor = LiquidGlassTokens.SurfaceOledFallback,
                focusedBorderColor = LiquidGlassTokens.ElectricLime,
                unfocusedBorderColor = Color.Transparent,
                focusedTextColor = LiquidGlassTokens.TextPrimary,
                unfocusedTextColor = LiquidGlassTokens.TextPrimary,
                cursorColor = LiquidGlassTokens.ElectricLime
            ),
            singleLine = true,
            shape = RoundedCornerShape(18.dp)
        )

        // --------------------------------
        // SEARCH LOADING INDICATOR
        // --------------------------------
        AnimatedVisibility(
            visible = isSearching,
            enter = fadeIn(animationSpec = LiquidGlassTokens.TactileSpringSpecFloat),
            exit = fadeOut(animationSpec = LiquidGlassTokens.TactileSpringSpecFloat)
        ) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .height(3.dp),
                color = LiquidGlassTokens.ElectricLime,
                trackColor = Color(0x1FFFFFFF)
            )
        }

        // --------------------------------
        // RESOLVING / PREPARING BANNER
        // --------------------------------
        AnimatedVisibility(
            visible = isResolving && currentSong != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            LiquidGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(14.dp),
                isElevated = true
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = LiquidGlassTokens.ElectricLime,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Extracting liquid audio for ${currentSong?.title}...",
                        color = LiquidGlassTokens.ElectricLime,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // --------------------------------
        // ERROR BANNER
        // --------------------------------
        errorMessage?.let { error ->
            LiquidGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = error,
                        color = LiquidGlassTokens.Crimson,
                        fontSize = 13.sp,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { viewModel.clearError() }) {
                        Text("Dismiss", color = Color.White, fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // --------------------------------
        // EMPTY STATE OR RESULTS LIST
        // --------------------------------
        if (searchResults.isEmpty() && searchQuery.isBlank()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = LiquidGlassTokens.TextMuted.copy(alpha = 0.25f),
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Find your favorite songs and artists",
                        color = LiquidGlassTokens.TextMuted,
                        fontSize = 15.sp,
                        letterSpacing = LiquidGlassTokens.HeaderLetterSpacing
                    )
                }
            }
        } else {
            // Virtualized list conforming to GPU Rasterization Budget:
            // Static glass borders/gradients without per-row runtime blur
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                items(
                    items = searchResults,
                    key = { song -> song.id }
                ) { song ->
                    val isCurrent = currentSong?.id == song.id
                    val isSongResolving = isCurrent && isResolving
                    val isSongPlaying = isCurrent && isPlaying

                    SongResultItem(
                        song = song,
                        isCurrent = isCurrent,
                        isResolving = isSongResolving,
                        isPlaying = isSongPlaying,
                        onClick = {
                            if (isCurrent) {
                                viewModel.togglePlayPause()
                            } else {
                                viewModel.playSong(song, playlist = searchResults)
                            }
                        }
                    )
                }
            }
        }
    }
}

// ================================================
// SONG RESULT ITEM (PERFORMANCE OPTIMIZED LIQUID GLASS)
// ================================================

@Composable
fun SongResultItem(
    song: Song,
    isCurrent: Boolean = false,
    isResolving: Boolean = false,
    isPlaying: Boolean = false,
    onClick: () -> Unit
) {
    LiquidGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        isElevated = isCurrent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // THUMBNAIL
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(LiquidGlassTokens.SurfaceOledFallback)
                    .border(
                        width = 1.dp,
                        brush = LiquidGlassTokens.SpecularRimGradient,
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = song.thumbnailUrl,
                    contentDescription = song.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                if (isPlaying) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = "Playing",
                            tint = LiquidGlassTokens.ElectricLime,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // SONG DETAILS
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = song.title,
                    color = if (isCurrent) LiquidGlassTokens.ElectricLime else LiquidGlassTokens.TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = LiquidGlassTokens.HeaderLetterSpacing,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = song.artist,
                    color = LiquidGlassTokens.TextSecondary,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // STATUS / ACTION BUTTON
            if (isResolving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = LiquidGlassTokens.ElectricLime,
                    strokeWidth = 2.dp
                )
            } else if (isCurrent) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(LiquidGlassTokens.SurfaceGlassActive)
                        .border(
                            width = 1.dp,
                            color = LiquidGlassTokens.ElectricLime.copy(alpha = 0.6f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = LiquidGlassTokens.ElectricLime,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}
