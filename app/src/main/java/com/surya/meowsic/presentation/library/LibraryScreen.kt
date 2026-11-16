package com.surya.meowsic.presentation.library

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.surya.meowsic.data.local.model.PlaylistWithCount
import com.surya.meowsic.presentation.search.SearchViewModel
import kotlinx.coroutines.launch
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * Meowsic LibraryScreen implementation:
 * - presentation/screens/LibraryScreen.kt
 * - presentation/components/PlaylistContainer.kt
 * - presentation/components/PlaylistArtCollage.kt
 */
@Composable
fun LibraryScreen(
    searchViewModel: SearchViewModel = viewModel()
) {
    val libraryViewModel: LibraryViewModel = viewModel()
    val likedSongs by libraryViewModel.likedSongs.collectAsState()
    val likedCount by libraryViewModel.likedCount.collectAsState()
    val playlists by libraryViewModel.playlistsWithCount.collectAsState()
    val playlistThumbnails by libraryViewModel.playlistThumbnails.collectAsState()

    var showLikedSongs by rememberSaveable { mutableStateOf(false) }
    var selectedPlaylistId by rememberSaveable { mutableStateOf<Long?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var playlistToRename by remember { mutableStateOf<PlaylistWithCount?>(null) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    BackHandler(enabled = showLikedSongs || selectedPlaylistId != null) {
        when {
            selectedPlaylistId != null -> selectedPlaylistId = null
            showLikedSongs -> showLikedSongs = false
        }
    }

    when {
        selectedPlaylistId != null -> {
            PlaylistDetailScreen(
                playlistId = selectedPlaylistId!!,
                onBack = { selectedPlaylistId = null },
                searchViewModel = searchViewModel,
                libraryViewModel = libraryViewModel,
                onShowSnackbar = { msg ->
                    scope.launch { snackbarHostState.showSnackbar(msg) }
                }
            )
        }
        showLikedSongs -> {
            LikedSongsScreen(
                songs = likedSongs,
                onBack = { showLikedSongs = false },
                onPlaySong = { song ->
                    searchViewModel.playSong(song, likedSongs)
                },
                onToggleLike = { song ->
                    libraryViewModel.toggleLike(song)
                }
            )
        }
        else -> {
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = statusBarTop + 12.dp),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 184.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Header with New Playlist button
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Library",
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground
                            )

                            ExtendedFloatingActionButton(
                                onClick = { showCreateDialog = true },
                                shape = CircleShape,
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ) {
                                Icon(Icons.Rounded.Add, contentDescription = "Create Playlist")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("New Playlist", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Liked Songs Pinned Expressive Banner Card
                    item {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(28.dp))
                                .clickable { showLikedSongs = true },
                            shape = RoundedCornerShape(28.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(
                                                MaterialTheme.colorScheme.secondaryContainer,
                                                MaterialTheme.colorScheme.primaryContainer
                                            )
                                        )
                                    )
                                    .padding(18.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(58.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.secondary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Favorite,
                                        contentDescription = "Liked Songs",
                                        tint = MaterialTheme.colorScheme.onSecondary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Liked Songs",
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    Text(
                                        text = "$likedCount favorite tracks",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            text = "Your Playlists (${playlists.size})",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                        )
                    }

                    if (playlists.isEmpty()) {
                        item {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(24.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerLow
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.QueueMusic,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "No playlists yet",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Tap 'New Playlist' above to build your first collection.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                        items(playlists, key = { it.playlist.id }) { item ->
                            val thumbs = playlistThumbnails[item.playlist.id].orEmpty()
                            MeowsicPlaylistItemCard(
                                item = item,
                                thumbnails = thumbs,
                                onClick = { selectedPlaylistId = item.playlist.id },
                                onRename = { playlistToRename = item },
                                onDelete = {
                                    libraryViewModel.deletePlaylist(item.playlist.id, context)
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Deleted '${item.playlist.name}'")
                                    }
                                }
                            )
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
        }
    }

    if (showCreateDialog) {
        CreatePlaylistDialog(
            onDismiss = { showCreateDialog = false },
            onSave = { name, coverPath ->
                scope.launch {
                    libraryViewModel.createPlaylist(name, coverPath)
                    snackbarHostState.showSnackbar("Created '$name'")
                }
                showCreateDialog = false
            }
        )
    }

    playlistToRename?.let { target ->
        SimpleNameDialog(
            title = "Rename Playlist",
            initialValue = target.playlist.name,
            onDismiss = { playlistToRename = null },
            onConfirm = { newName ->
                libraryViewModel.renamePlaylist(target.playlist.id, newName)
                playlistToRename = null
            }
        )
    }
}

/**
 * Meowsic PlaylistItemCard: presentation/components/PlaylistContainer.kt (PlaylistItem)
 * & presentation/components/PlaylistArtCollage.kt (1, 2, 3 triangle, or 4 grid circular collage)
 */
@Composable
private fun MeowsicPlaylistItemCard(
    item: PlaylistWithCount,
    thumbnails: List<String>,
    onClick: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (!item.playlist.coverImagePath.isNullOrBlank()) {
                    AsyncImage(
                        model = item.playlist.coverImagePath,
                        contentDescription = item.playlist.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    PlaylistArtCollage(
                        urls = thumbnails,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.playlist.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${item.songCount} Songs",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        imageVector = Icons.Rounded.MoreVert,
                        contentDescription = "Playlist Options",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Rename") },
                        leadingIcon = { Icon(Icons.Rounded.Edit, contentDescription = null) },
                        onClick = {
                            menuExpanded = false
                            onRename()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        leadingIcon = { Icon(Icons.Rounded.DeleteOutline, contentDescription = null) },
                        onClick = {
                            menuExpanded = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}

/**
 * Meowsic PlaylistArtCollage: presentation/components/PlaylistArtCollage.kt
 */
@Composable
fun PlaylistArtCollage(
    urls: List<String>,
    modifier: Modifier = Modifier
) {
    val distinctUrls = remember(urls) { urls.filter { it.isNotBlank() }.distinct().take(4) }

    if (distinctUrls.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Rounded.MusicNote,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    BoxWithConstraints(modifier = modifier) {
        val spacing = 3.dp
        when (distinctUrls.size) {
            1 -> {
                AsyncImage(
                    model = distinctUrls[0],
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )
            }
            2 -> {
                val circleSize = min(maxWidth.value, (maxHeight.value - spacing.value) / 2f).dp
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(spacing, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    distinctUrls.forEach { url ->
                        AsyncImage(
                            model = url,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(circleSize)
                                .clip(CircleShape)
                        )
                    }
                }
            }
            3 -> {
                Layout(
                    content = {
                        distinctUrls.forEach { url ->
                            AsyncImage(
                                model = url,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.clip(CircleShape)
                            )
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                ) { measurables, constraints ->
                    val w = constraints.maxWidth.toFloat()
                    val h = constraints.maxHeight.toFloat()
                    val s = spacing.toPx()
                    val sqrt3 = sqrt(3f)

                    val dFromW = (w - s) / 2f
                    val dFromH = (2f * h - sqrt3 * s) / (2f + sqrt3)
                    val d = min(dFromW, dFromH).coerceAtLeast(0f)
                    val r = d / 2f
                    val side = d + s

                    val bboxW = 2f * r + side
                    val bboxH = 2f * r + (sqrt3 / 2f) * side
                    val leftOffset = (w - bboxW) / 2f
                    val topOffset = (h - bboxH) / 2f

                    val diameterInt = d.roundToInt()
                    val childConstraints = Constraints.fixed(diameterInt, diameterInt)
                    val placeables = measurables.map { it.measure(childConstraints) }

                    val c0x = leftOffset + r + side / 2f
                    val c0y = topOffset + r
                    val c1x = leftOffset + r
                    val c1y = topOffset + r + (sqrt3 / 2f) * side
                    val c2x = leftOffset + r + side
                    val c2y = c1y

                    layout(constraints.maxWidth, constraints.maxHeight) {
                        placeables.getOrNull(0)?.place((c0x - r).roundToInt(), (c0y - r).roundToInt())
                        placeables.getOrNull(1)?.place((c1x - r).roundToInt(), (c1y - r).roundToInt())
                        placeables.getOrNull(2)?.place((c2x - r).roundToInt(), (c2y - r).roundToInt())
                    }
                }
            }
            else -> {
                val cellSize = ((min(maxWidth.value, maxHeight.value) - spacing.value) / 2f).dp
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(spacing, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
                        AsyncImage(
                            model = distinctUrls[0],
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(cellSize)
                                .clip(CircleShape)
                        )
                        AsyncImage(
                            model = distinctUrls[1],
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(cellSize)
                                .clip(CircleShape)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
                        AsyncImage(
                            model = distinctUrls[2],
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(cellSize)
                                .clip(CircleShape)
                        )
                        AsyncImage(
                            model = distinctUrls[3],
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(cellSize)
                                .clip(CircleShape)
                        )
                    }
                }
            }
        }
    }
}
