package com.surya.meowsic.presentation.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

private val BackgroundColor = Color(0xFF101010)
private val CardBackground = Color(0xFF1E1E1E)
private val AccentColor = Color(0xFFB9F36A)
private val ErrorColor = Color(0xFFFF5252)

@Composable
fun SearchScreen(
    viewModel: SearchViewModel
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val isResolving by viewModel.isResolving.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val currentSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // --------------------------------
        // TITLE HEADER
        // --------------------------------
        Text(
            text = "Search",
            color = Color.White,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // --------------------------------
        // SEARCH INPUT BAR
        // --------------------------------
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.onSearchQueryChanged(it) },
            placeholder = {
                Text(
                    text = "Search songs, artists...",
                    color = Color.Gray,
                    fontSize = 15.sp
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = if (searchQuery.isNotEmpty()) AccentColor else Color.Gray
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear search",
                            tint = Color.LightGray
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = CardBackground,
                unfocusedContainerColor = CardBackground,
                focusedBorderColor = AccentColor,
                unfocusedBorderColor = Color(0xFF333333),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = AccentColor
            ),
            singleLine = true,
            shape = RoundedCornerShape(16.dp)
        )

        // --------------------------------
        // SEARCH LOADING INDICATOR
        // --------------------------------
        AnimatedVisibility(
            visible = isSearching,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .height(3.dp),
                color = AccentColor,
                trackColor = Color(0xFF262626)
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
            Surface(
                color = Color(0xFF1B2612),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = AccentColor,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Extracting audio for ${currentSong?.title}...",
                        color = AccentColor,
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
            Surface(
                color = Color(0xFF2A1515),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = error,
                        color = ErrorColor,
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
                        tint = Color(0xFF333333),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Find your favorite songs and artists",
                        color = Color.Gray,
                        fontSize = 15.sp
                    )
                }
            }
        } else {
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
                                viewModel.playSong(song)
                            }
                        }
                    )
                }
            }
        }
    }
}

// ================================================
// SONG RESULT ITEM
// ================================================

@Composable
fun SongResultItem(
    song: Song,
    isCurrent: Boolean = false,
    isResolving: Boolean = false,
    isPlaying: Boolean = false,
    onClick: () -> Unit
) {
    val backgroundColor = if (isCurrent) Color(0xFF1E2618) else CardBackground

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // THUMBNAIL
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF2B2B2B)),
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
                        .background(Color.Black.copy(alpha = 0.45f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = "Playing",
                        tint = AccentColor,
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
                color = if (isCurrent) AccentColor else Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = song.artist,
                color = Color.LightGray,
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
                color = AccentColor,
                strokeWidth = 2.dp
            )
        } else if (isCurrent) {
            IconButton(
                onClick = onClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = AccentColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
