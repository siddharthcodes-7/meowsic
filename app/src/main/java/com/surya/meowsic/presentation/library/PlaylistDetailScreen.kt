package com.surya.meowsic.presentation.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
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
import com.surya.meowsic.domain.Song
import com.surya.meowsic.presentation.search.SearchViewModel
import com.surya.meowsic.ui.glass.GlassIconButton
import com.surya.meowsic.ui.glass.GlassSurface
import com.surya.meowsic.ui.glass.GlassVariant
import com.surya.meowsic.ui.theme.LiquidGlassTokens
import kotlinx.coroutines.launch

@Composable
fun PlaylistDetailScreen(
    playlistId: Long,
    onBack: () -> Unit,
    searchViewModel: SearchViewModel,
    libraryViewModel: LibraryViewModel,
    onShowSnackbar: (String) -> Unit = {}
) {
    val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as android.app.Application
    val detailVm: PlaylistDetailViewModel = viewModel(
        factory = PlaylistDetailViewModel.factory(app, playlistId)
    )

    val playlist by detailVm.playlist.collectAsState()
    val songs by detailVm.songs.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(innerPadding)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlassIconButton(onClick = onBack, size = 40.dp) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = LiquidGlassTokens.TextPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = playlist?.name ?: "",
                        color = LiquidGlassTokens.TextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${songs.size} songs",
                        color = LiquidGlassTokens.TextMuted,
                        fontSize = 13.sp
                    )
                }
                // Play button
                if (songs.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(50))
                            .background(
                                Brush.radialGradient(listOf(Color(0xFF7B2FBE), Color(0xFF4A1A8C)))
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { searchViewModel.playSong(songs.first(), playlist = songs) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    // Shuffle button
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color(0x26FFFFFF))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                val shuffled = songs.shuffled()
                                if (shuffled.isNotEmpty()) {
                                    searchViewModel.playSong(shuffled.first(), playlist = shuffled)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shuffle,
                            contentDescription = "Shuffle",
                            tint = LiquidGlassTokens.TextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Cover art (if any)
            playlist?.coverImagePath?.let { path ->
                AsyncImage(
                    model = path,
                    contentDescription = "Playlist cover",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(Color(0x1AFFFFFF)))

            if (songs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = LiquidGlassTokens.TextMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No songs in this playlist",
                            color = LiquidGlassTokens.TextMuted,
                            fontSize = 15.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 180.dp)
                ) {
                    itemsIndexed(songs, key = { _, item -> item.id }) { index, song ->
                        PlaylistSongRow(
                            song = song,
                            canMoveUp = index > 0,
                            canMoveDown = index < songs.size - 1,
                            onClick = { searchViewModel.playSong(song, playlist = songs) },
                            onMoveUp = {
                                if (index > 0) {
                                    val list = songs.map { it.id }.toMutableList()
                                    val item = list.removeAt(index)
                                    list.add(index - 1, item)
                                    detailVm.reorderSongs(list)
                                }
                            },
                            onMoveDown = {
                                if (index < songs.size - 1) {
                                    val list = songs.map { it.id }.toMutableList()
                                    val item = list.removeAt(index)
                                    list.add(index + 1, item)
                                    detailVm.reorderSongs(list)
                                }
                            },
                            onRemove = {
                                val removed = song
                                detailVm.removeSong(song.id)
                                scope.launch {
                                    val result = snackbarHostState.showSnackbar(
                                        message = "Removed \"${removed.title}\"",
                                        actionLabel = "Undo",
                                        duration = SnackbarDuration.Short
                                    )
                                    if (result == SnackbarResult.ActionPerformed) {
                                        detailVm.addSong(removed)
                                    }
                                }
                            }
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(0.5.dp)
                                .padding(horizontal = 24.dp)
                                .background(Color(0x1AFFFFFF))
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaylistSongRow(
    song: Song,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onClick: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(10.dp))
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
            } else {
                Icon(Icons.Default.MusicNote, contentDescription = null, tint = LiquidGlassTokens.TextMuted)
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                color = LiquidGlassTokens.TextPrimary,
                fontSize = 15.sp,
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

        // Reorder arrows
        if (canMoveUp) {
            IconButton(onClick = onMoveUp, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = "Move up",
                    tint = LiquidGlassTokens.TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        if (canMoveDown) {
            IconButton(onClick = onMoveDown, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Move down",
                    tint = LiquidGlassTokens.TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Remove button
        IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
            Icon(
                imageVector = Icons.Default.Remove,
                contentDescription = "Remove from playlist",
                tint = LiquidGlassTokens.TextMuted,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
