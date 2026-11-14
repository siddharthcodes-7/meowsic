package com.surya.meowsic.presentation.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.surya.meowsic.data.local.model.PlaylistWithCount
import com.surya.meowsic.presentation.search.SearchViewModel
import com.surya.meowsic.ui.glass.GlassIconButton
import com.surya.meowsic.ui.glass.GlassSurface
import com.surya.meowsic.ui.glass.GlassVariant
import com.surya.meowsic.ui.theme.LiquidGlassTokens
import kotlinx.coroutines.launch

@Composable
fun LibraryScreen(
    searchViewModel: SearchViewModel = viewModel()
) {
    val context = LocalContext.current
    val libraryViewModel: LibraryViewModel = viewModel()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val likedSongs by libraryViewModel.likedSongs.collectAsState()
    val likedCount by libraryViewModel.likedCount.collectAsState()
    val playlists by libraryViewModel.playlistsWithCount.collectAsState()
    val thumbnails by libraryViewModel.playlistThumbnails.collectAsState()

    var showLikedSongs by remember { mutableStateOf(false) }
    var openPlaylistId by remember { mutableStateOf<Long?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }

    // Navigate to Liked Songs
    if (showLikedSongs) {
        LikedSongsScreen(
            songs = likedSongs,
            onBack = { showLikedSongs = false },
            onPlaySong = { song -> searchViewModel.playSong(song, playlist = likedSongs) },
            onToggleLike = { song -> libraryViewModel.toggleLike(song) }
        )
        return
    }

    // Navigate to Playlist Detail
    openPlaylistId?.let { pid ->
        PlaylistDetailScreen(
            playlistId = pid,
            onBack = { openPlaylistId = null },
            searchViewModel = searchViewModel,
            libraryViewModel = libraryViewModel,
            onShowSnackbar = { msg ->
                scope.launch { snackbarHostState.showSnackbar(msg) }
            }
        )
        return
    }

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 180.dp)
        ) {
            // ── Header ──
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Library",
                        color = LiquidGlassTokens.TextPrimary,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = LiquidGlassTokens.HeaderLetterSpacing
                    )
                    GlassIconButton(onClick = { showCreateDialog = true }, size = 40.dp) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "New Playlist",
                            tint = LiquidGlassTokens.TextPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(Color(0x1AFFFFFF)))
                Spacer(modifier = Modifier.height(16.dp))
            }

            // ── Liked Songs pinned card ──
            item {
                GlassSurface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .height(88.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { showLikedSongs = true },
                    variant = GlassVariant.ELEVATED,
                    shape = RoundedCornerShape(18.dp),
                    enableBlur = false
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF7B2FBE), Color(0xFF4A1A8C))
                                )
                            )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Liked Songs",
                                    color = Color.White,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "$likedCount songs",
                                    color = Color.White.copy(alpha = 0.75f),
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // ── Playlists section header ──
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Playlists",
                        color = LiquidGlassTokens.TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (playlists.isNotEmpty()) {
                        Text(
                            text = "${playlists.size}",
                            color = LiquidGlassTokens.TextMuted,
                            fontSize = 13.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // ── Playlists grid ──
            if (playlists.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.LibraryMusic,
                                contentDescription = null,
                                tint = LiquidGlassTokens.TextMuted,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No playlists yet",
                                color = LiquidGlassTokens.TextMuted,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap + to create one",
                                color = LiquidGlassTokens.TextMuted,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            } else {
                items(
                    items = playlists.chunked(2),
                    key = { row -> row.first().playlist.id }
                ) { rowItems ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowItems.forEach { pwc ->
                            Box(modifier = Modifier.weight(1f)) {
                                PlaylistCard(
                                    pwc = pwc,
                                    thumbnailUrls = thumbnails[pwc.playlist.id] ?: emptyList(),
                                    onClick = { openPlaylistId = pwc.playlist.id },
                                    onRename = { newName -> libraryViewModel.renamePlaylist(pwc.playlist.id, newName) },
                                    onChangeCover = { /* handled via dialog */ },
                                    onRemoveCover = { libraryViewModel.updatePlaylistCover(pwc.playlist.id, null) },
                                    onDelete = {
                                        libraryViewModel.deletePlaylist(pwc.playlist.id, context)
                                    }
                                )
                            }
                        }
                        if (rowItems.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }

    // Create Playlist Dialog
    if (showCreateDialog) {
        CreatePlaylistDialog(
            onDismiss = { showCreateDialog = false },
            onSave = { name, coverPath ->
                scope.launch {
                    libraryViewModel.createPlaylist(name, coverPath)
                }
                showCreateDialog = false
            }
        )
    }
}

@Composable
private fun PlaylistCard(
    pwc: PlaylistWithCount,
    thumbnailUrls: List<String>,
    onClick: () -> Unit,
    onRename: (String) -> Unit,
    onChangeCover: () -> Unit,
    onRemoveCover: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }

    GlassSurface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        variant = GlassVariant.PRIMARY,
        shape = RoundedCornerShape(16.dp),
        enableBlur = false
    ) {
        Column {
            // Cover area — 1:1 square
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(Color(0xFF181828)),
                contentAlignment = Alignment.Center
            ) {
                val coverPath = pwc.playlist.coverImagePath
                if (coverPath != null) {
                    AsyncImage(
                        model = coverPath,
                        contentDescription = pwc.playlist.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (thumbnailUrls.isNotEmpty()) {
                    // 2x2 collage of first 4 thumbnails
                    val urls = thumbnailUrls.take(4)
                    if (urls.size >= 4) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                AsyncImage(model = urls[0], contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.weight(1f).fillMaxHeight())
                                AsyncImage(model = urls[1], contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.weight(1f).fillMaxHeight())
                            }
                            Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                AsyncImage(model = urls[2], contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.weight(1f).fillMaxHeight())
                                AsyncImage(model = urls[3], contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.weight(1f).fillMaxHeight())
                            }
                        }
                    } else {
                        AsyncImage(model = urls[0], contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = LiquidGlassTokens.TextMuted,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // More button overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                ) {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(Color(0xFF1A1A2E))
                    ) {
                        DropdownMenuItem(
                            text = { Text("Rename", color = LiquidGlassTokens.TextPrimary, fontSize = 14.sp) },
                            onClick = { showMenu = false; showRenameDialog = true }
                        )
                        if (pwc.playlist.coverImagePath != null) {
                            DropdownMenuItem(
                                text = { Text("Remove cover", color = LiquidGlassTokens.TextPrimary, fontSize = 14.sp) },
                                onClick = { showMenu = false; onRemoveCover() }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Delete", color = Color(0xFFFF5252), fontSize = 14.sp) },
                            onClick = { showMenu = false; onDelete() }
                        )
                    }
                }
            }

            // Name + count
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp)) {
                Text(
                    text = pwc.playlist.name,
                    color = LiquidGlassTokens.TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${pwc.songCount} songs",
                    color = LiquidGlassTokens.TextMuted,
                    fontSize = 11.sp
                )
            }
        }
    }

    if (showRenameDialog) {
        SimpleNameDialog(
            title = "Rename playlist",
            placeholder = pwc.playlist.name,
            onDismiss = { showRenameDialog = false },
            onConfirm = { newName ->
                onRename(newName)
                showRenameDialog = false
            }
        )
    }
}
