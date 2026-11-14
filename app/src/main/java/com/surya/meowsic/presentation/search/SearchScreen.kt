package com.surya.meowsic.presentation.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MoreVert
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.surya.meowsic.domain.Song
import com.surya.meowsic.ui.glass.GlassSurface
import com.surya.meowsic.ui.glass.GlassVariant
import com.surya.meowsic.ui.theme.LiquidGlassTokens
import com.surya.meowsic.ui.theme.LocalAmbientPalette
import kotlinx.coroutines.delay

@Composable
fun SearchScreen(
    viewModel: SearchViewModel
) {
    var searchQuery by remember { mutableStateOf("") }

    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val isResolving by viewModel.isResolving.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val currentSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val palette = LocalAmbientPalette.current

    // 500ms debounce
    LaunchedEffect(searchQuery) {
        if (searchQuery.length >= 2) {
            delay(500L)
            viewModel.searchYouTube(searchQuery)
        } else {
            viewModel.searchYouTube(searchQuery)
        }
    }

    // No opaque background — root AmbientLightOrbs shine through
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        // ── Title ──
        Text(
            text = "Search",
            color = LiquidGlassTokens.TextPrimary,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = LiquidGlassTokens.HeaderLetterSpacing,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
        )

        // ── GlassSurface search bar ──
        GlassSurface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(52.dp),
            variant = GlassVariant.SUBTLE,
            shape = RoundedCornerShape(28.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = if (searchQuery.isNotEmpty()) palette.primary else LiquidGlassTokens.TextMuted,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Box(modifier = Modifier.weight(1f)) {
                    if (searchQuery.isEmpty()) {
                        Text(
                            text = "Songs, artists, channels...",
                            color = LiquidGlassTokens.TextMuted,
                            fontSize = 15.sp
                        )
                    }
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { newValue ->
                            searchQuery = newValue
                            viewModel.onSearchQueryChanged(newValue)
                        },
                        textStyle = TextStyle(
                            color = LiquidGlassTokens.TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Normal
                        ),
                        cursorBrush = SolidColor(LiquidGlassTokens.TextPrimary),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                if (searchQuery.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {
                                    searchQuery = ""
                                    viewModel.onSearchQueryChanged("")
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = LiquidGlassTokens.TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ── Loading indicator ──
        AnimatedVisibility(
            visible = isSearching,
            enter = fadeIn(animationSpec = LiquidGlassTokens.TactileSpringSpecFloat),
            exit = fadeOut(animationSpec = LiquidGlassTokens.TactileSpringSpecFloat)
        ) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(1.5.dp)),
                color = palette.primary,
                trackColor = Color(0x1FFFFFFF)
            )
        }

        // ── Resolving banner — GlassSurface ──
        AnimatedVisibility(
            visible = isResolving && currentSong != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            GlassSurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                variant = GlassVariant.ELEVATED,
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = palette.primary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Preparing ${currentSong?.title ?: ""}...",
                        color = palette.primary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // ── Error banner — GlassSurface ──
        errorMessage?.let { error ->
            GlassSurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
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
        Box(Modifier.fillMaxWidth().height(0.5.dp).background(Color(0x1AFFFFFF)))

        // ── Empty state or results ──
        if (searchResults.isEmpty() && searchQuery.isBlank()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
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
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 160.dp)
            ) {
                items(items = searchResults, key = { song -> song.id }) { song ->
                    val isCurrent = currentSong?.id == song.id
                    val isSongResolving = isCurrent && isResolving
                    val isSongPlaying = isCurrent && isPlaying

                    ObsidianSongItem(
                        song = song,
                        isCurrent = isCurrent,
                        isResolving = isSongResolving,
                        isPlaying = isSongPlaying,
                        palette = palette,
                        onClick = {
                            if (isCurrent) {
                                viewModel.togglePlayPause()
                            } else {
                                viewModel.playSong(song, playlist = searchResults)
                            }
                        }
                    )
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .height(0.5.dp)
                            .background(Color(0x1AFFFFFF))
                    )
                }
            }
        }
    }
}

@Composable
private fun ObsidianSongItem(
    song: Song,
    isCurrent: Boolean = false,
    isResolving: Boolean = false,
    isPlaying: Boolean = false,
    palette: LiquidGlassTokens.AmbientPalette,
    onClick: () -> Unit
) {
    if (isCurrent) {
        // Wrap active/playing song in GlassSurface
        GlassSurface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                ),
            variant = GlassVariant.ELEVATED,
            shape = RoundedCornerShape(16.dp)
        ) {
            SongItemContent(song = song, isCurrent = true, isResolving = isResolving, isPlaying = isPlaying, palette = palette)
        }
    } else {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                )
                .padding(horizontal = 24.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Plain Box+AsyncImage replacing ObsidianSquareArtwork
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF181818))
            ) {
                AsyncImage(
                    model = song.thumbnailUrl,
                    contentDescription = song.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    color = LiquidGlassTokens.TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Normal,
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
            // More icon button — placeholder for "Add to playlist"
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More",
                    tint = LiquidGlassTokens.TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun SongItemContent(
    song: Song,
    isCurrent: Boolean,
    isResolving: Boolean,
    isPlaying: Boolean,
    palette: LiquidGlassTokens.AmbientPalette
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Plain Box+AsyncImage replacing ObsidianSquareArtwork
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF181818))
        ) {
            AsyncImage(
                model = song.thumbnailUrl,
                contentDescription = song.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                color = palette.primary,
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
        if (isResolving) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = palette.primary,
                strokeWidth = 2.dp
            )
        } else {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = palette.primary,
                modifier = Modifier.size(22.dp)
            )
        }
        // More icon button — placeholder for "Add to playlist"
        IconButton(onClick = {}) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "More",
                tint = LiquidGlassTokens.TextMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
