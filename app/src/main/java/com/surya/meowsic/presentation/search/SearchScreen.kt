package com.surya.meowsic.presentation.search

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.surya.meowsic.domain.Song
import com.surya.meowsic.presentation.library.AddToPlaylistSheet
import com.surya.meowsic.presentation.library.LibraryViewModel
import com.surya.meowsic.ui.glass.AutoScrollingText
import com.surya.meowsic.ui.glass.PlayingEqIcon
import kotlinx.coroutines.launch

/**
 * Meowsic SearchScreen implementation:
 * - presentation/screens/SearchScreen.kt
 * - presentation/components/subcomps/EnhancedSongListItem.kt
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel = viewModel(),
    libraryViewModel: LibraryViewModel = viewModel()
) {
    val query by viewModel.searchQuery.collectAsState()
    val results by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val currentSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val likedSongs by libraryViewModel.likedSongs.collectAsState()
    val playlists by libraryViewModel.playlistsWithCount.collectAsState()

    val likedIds = remember(likedSongs) { likedSongs.map { it.id }.toSet() }
    var songForPlaylistSheet by remember { mutableStateOf<Song?>(null) }

    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    val quickChips = remember {
        listOf(
            "Anirudh Ravichander",
            "A.R. Rahman Hits",
            "Tamil Melody",
            "Yuvan Shankar Raja",
            "Harris Jayaraj",
            "Lo-Fi Chill",
            "Synthwave",
            "Phonk Workout"
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = statusBarTop + 12.dp, start = 16.dp, end = 16.dp)
        ) {
            Text(
                text = "Search",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Meowsic Expressive Pill Search Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 6.dp
            ) {
                TextField(
                    value = query,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    placeholder = {
                        Text(
                            text = "Search songs, artists, albums...",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                Icon(
                                    imageVector = Icons.Rounded.Clear,
                                    contentDescription = "Clear",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            focusManager.clearFocus()
                            viewModel.searchYouTube(query)
                        }
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            when {
                isSearching -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                results.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp)
                    ) {
                        Text(
                            text = "Trending & Quick Vibes",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            quickChips.forEach { tag ->
                                FilterChip(
                                    selected = query.equals(tag, ignoreCase = true),
                                    onClick = {
                                        viewModel.onSearchQueryChanged(tag)
                                        viewModel.searchYouTube(tag)
                                    },
                                    label = { Text(tag) },
                                    shape = CircleShape,
                                    colors = FilterChipDefaults.filterChipColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                        labelColor = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 176.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(results, key = { it.id }) { song ->
                            val isCurrent = currentSong?.id == song.id
                            val isLiked = likedIds.contains(song.id)
                            EnhancedSongListItem(
                                song = song,
                                isCurrentSong = isCurrent,
                                isPlaying = isCurrent && isPlaying,
                                isLiked = isLiked,
                                onToggleLike = { libraryViewModel.toggleLike(song) },
                                onMoreClick = { songForPlaylistSheet = song },
                                onClick = {
                                    focusManager.clearFocus()
                                    viewModel.playSong(song, results)
                                }
                            )
                        }
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 160.dp)
        )
    }

    songForPlaylistSheet?.let { selectedSong ->
        AddToPlaylistSheet(
            song = selectedSong,
            playlists = playlists,
            onAddToPlaylist = { playlistId ->
                libraryViewModel.addSongToPlaylist(playlistId, selectedSong) { added ->
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            if (added) "Added to playlist" else "Song already in playlist"
                        )
                    }
                }
                songForPlaylistSheet = null
            },
            onDismiss = { songForPlaylistSheet = null },
            onCreatePlaylist = { name ->
                scope.launch {
                    val newId = libraryViewModel.createPlaylist(name, null)
                    libraryViewModel.addSongToPlaylist(newId, selectedSong) {}
                    snackbarHostState.showSnackbar("Created '$name' & added song")
                }
                songForPlaylistSheet = null
            }
        )
    }
}

/**
 * Meowsic EnhancedSongListItem implementation: presentation/components/subcomps/EnhancedSongListItem.kt
 * Morphs row corners (22.dp -> 50.dp pill) and album art corners (12.dp -> 50.dp circle)
 * when isCurrentSong is true, with PlayingEqIcon and AutoScrollingText.
 */
@Composable
fun EnhancedSongListItem(
    song: Song,
    isCurrentSong: Boolean,
    isPlaying: Boolean,
    isLiked: Boolean = false,
    showLikeButton: Boolean = true,
    onToggleLike: (() -> Unit)? = null,
    onMoreClick: (() -> Unit)? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor by animateColorAsState(
        targetValue = if (isCurrentSong) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
        animationSpec = tween(300),
        label = "SongContainerColor"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isCurrentSong) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
        animationSpec = tween(300),
        label = "SongContentColor"
    )

    val cornerRadius by animateDpAsState(
        targetValue = if (isCurrentSong) 50.dp else 22.dp,
        animationSpec = tween(300),
        label = "SongCornerRadius"
    )

    val albumCornerRadius by animateDpAsState(
        targetValue = if (isCurrentSong) 50.dp else 12.dp,
        animationSpec = tween(300),
        label = "AlbumCornerRadius"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(cornerRadius))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(cornerRadius),
        color = containerColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(albumCornerRadius))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
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
                        imageVector = Icons.Rounded.MusicNote,
                        contentDescription = null,
                        tint = contentColor
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                if (isCurrentSong) {
                    AutoScrollingText(
                        text = song.title,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = contentColor
                    )
                } else {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = contentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (song.duration.isNotBlank()) "${song.artist} • ${song.duration}" else song.artist,
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor.copy(alpha = 0.72f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (isCurrentSong) {
                Spacer(modifier = Modifier.width(8.dp))
                PlayingEqIcon(
                    modifier = Modifier.size(width = 18.dp, height = 16.dp),
                    color = contentColor,
                    isPlaying = isPlaying
                )
            }

            if (showLikeButton && onToggleLike != null) {
                IconButton(
                    onClick = onToggleLike,
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (isLiked) MaterialTheme.colorScheme.secondary else contentColor.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            if (onMoreClick != null) {
                FilledIconButton(
                    onClick = onMoreClick,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = contentColor.copy(alpha = 0.1f),
                        contentColor = contentColor
                    ),
                    modifier = Modifier
                        .size(width = 36.dp, height = 36.dp)
                        .padding(start = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MoreVert,
                        contentDescription = "More options",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
