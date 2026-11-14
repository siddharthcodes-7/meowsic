package com.surya.meowsic.presentation.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MusicNote
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
import com.surya.meowsic.data.local.model.PlaylistWithCount
import com.surya.meowsic.domain.Song
import com.surya.meowsic.ui.glass.GlassSheet
import com.surya.meowsic.ui.theme.LiquidGlassTokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddToPlaylistSheet(
    song: Song,
    playlists: List<PlaylistWithCount>,
    onAddToPlaylist: (Long) -> Unit,
    onDismiss: () -> Unit,
    onCreatePlaylist: (String) -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }

    GlassSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Text(
                text = "Add to playlist",
                color = LiquidGlassTokens.TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                // "New playlist" row at top
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { showCreateDialog = true }
                            .padding(vertical = 12.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF1A1A2E)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = LiquidGlassTokens.TextPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = "New playlist",
                            color = LiquidGlassTokens.TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(Color(0x1AFFFFFF)))
                    Spacer(modifier = Modifier.height(4.dp))
                }

                items(playlists) { pwc ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                onAddToPlaylist(pwc.playlist.id)
                                onDismiss()
                            }
                            .padding(vertical = 10.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF181818)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (pwc.playlist.coverImagePath != null) {
                                AsyncImage(
                                    model = pwc.playlist.coverImagePath,
                                    contentDescription = pwc.playlist.name,
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
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = pwc.playlist.name,
                                color = LiquidGlassTokens.TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${pwc.songCount} songs",
                                color = LiquidGlassTokens.TextMuted,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        SimpleNameDialog(
            title = "New playlist",
            placeholder = "Playlist name",
            onDismiss = { showCreateDialog = false },
            onConfirm = { name ->
                onCreatePlaylist(name)
                showCreateDialog = false
                onDismiss()
            }
        )
    }
}

@Composable
fun SimpleNameDialog(
    title: String,
    placeholder: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = title, color = LiquidGlassTokens.TextPrimary, fontWeight = FontWeight.Bold)
        },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                placeholder = { Text(placeholder, color = LiquidGlassTokens.TextMuted) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = LiquidGlassTokens.TextPrimary,
                    unfocusedTextColor = LiquidGlassTokens.TextPrimary,
                    focusedBorderColor = Color(0xFF7B2FBE),
                    unfocusedBorderColor = Color(0x40FFFFFF),
                    cursorColor = Color(0xFF7B2FBE)
                )
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (text.isNotBlank()) onConfirm(text.trim()) },
                enabled = text.isNotBlank()
            ) {
                Text("Save", color = Color(0xFF7B2FBE))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = LiquidGlassTokens.TextMuted)
            }
        },
        containerColor = Color(0xFF0D0D16),
        titleContentColor = LiquidGlassTokens.TextPrimary,
        textContentColor = LiquidGlassTokens.TextPrimary
    )
}
