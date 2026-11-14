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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.surya.meowsic.domain.Song
import com.surya.meowsic.ui.glass.GlassSurface
import com.surya.meowsic.ui.glass.GlassVariant
import com.surya.meowsic.ui.theme.LiquidGlassTokens

@Composable
fun LikedSongsScreen(
    songs: List<Song>,
    onBack: () -> Unit,
    onPlaySong: (Song) -> Unit,
    onToggleLike: (Song) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = LiquidGlassTokens.TextPrimary,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Liked Songs",
                    color = LiquidGlassTokens.TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = LiquidGlassTokens.HeaderLetterSpacing
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
                        ) { onPlaySong(songs.first()) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(Color(0x1AFFFFFF)))

        if (songs.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        tint = LiquidGlassTokens.TextMuted,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No liked songs yet",
                        color = LiquidGlassTokens.TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Heart a song while it's playing",
                        color = LiquidGlassTokens.TextMuted,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 180.dp)
            ) {
                items(songs, key = { it.id }) { song ->
                    LikedSongRow(
                        song = song,
                        onClick = { onPlaySong(song) },
                        onUnlike = { onToggleLike(song) }
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

@Composable
private fun LikedSongRow(
    song: Song,
    onClick: () -> Unit,
    onUnlike: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Artwork
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
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = LiquidGlassTokens.TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
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
        // Unlike button
        IconButton(onClick = onUnlike, modifier = Modifier.size(36.dp)) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = "Unlike",
                tint = Color(0xFF7B2FBE),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
