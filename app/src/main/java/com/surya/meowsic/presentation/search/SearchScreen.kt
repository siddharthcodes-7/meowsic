package com.surya.meowsic.presentation.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape

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

import androidx.lifecycle.viewmodel.compose.viewModel

import coil.compose.AsyncImage

import com.surya.meowsic.domain.Song


@Composable
fun SearchScreen(
    viewModel: SearchViewModel = viewModel()
) {

    var searchQuery by remember {
        mutableStateOf("")
    }

    val searchResults by
    viewModel.searchResults.collectAsState()

    val isSearching by
    viewModel.isSearching.collectAsState()

    val isResolving by
    viewModel.isResolving.collectAsState()

    val errorMessage by
    viewModel.errorMessage.collectAsState()

    val currentSong by
    viewModel.currentSong.collectAsState()

    val isPlaying by
    viewModel.isPlaying.collectAsState()


    val background = Color(0xFF101010)

    val accent = Color(0xFFB9F36A)


    Column(

        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .padding(16.dp)

    ) {

        // --------------------------------
        // HEADER
        // --------------------------------

        Text(

            text = "Search",

            color = Color.White,

            fontSize = 28.sp,

            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )


        // --------------------------------
        // SEARCH FIELD
        // --------------------------------

        OutlinedTextField(

            value = searchQuery,

            onValueChange = {

                searchQuery = it

                viewModel.searchYouTube(it)
            },

            placeholder = {

                Text(
                    "Search songs, artists...",
                    color = Color.Gray
                )
            },

            modifier = Modifier.fillMaxWidth(),

            colors = OutlinedTextFieldDefaults.colors(

                focusedBorderColor = accent,

                unfocusedBorderColor = Color.Gray,

                focusedTextColor = Color.White,

                unfocusedTextColor = Color.White,

                cursorColor = accent
            ),

            singleLine = true,

            shape = RoundedCornerShape(14.dp)
        )


        Spacer(
            modifier = Modifier.height(16.dp)
        )


        // --------------------------------
        // SEARCH LOADING
        // --------------------------------

        if (isSearching) {

            LinearProgressIndicator(

                modifier = Modifier.fillMaxWidth(),

                color = accent
            )
        }


        // --------------------------------
        // PLAYBACK STATUS
        // --------------------------------

        if (isResolving && currentSong != null) {

            Row(

                verticalAlignment = Alignment.CenterVertically,

                modifier = Modifier.padding(vertical = 12.dp)

            ) {

                CircularProgressIndicator(

                    modifier = Modifier.size(22.dp),

                    color = accent,

                    strokeWidth = 2.dp
                )

                Spacer(
                    modifier = Modifier.width(12.dp)
                )

                Text(

                    text = "Preparing ${currentSong?.title}...",

                    color = accent,

                    fontSize = 13.sp,

                    maxLines = 1,

                    overflow = TextOverflow.Ellipsis
                )
            }
        }


        // --------------------------------
        // ERROR MESSAGE
        // --------------------------------

        errorMessage?.let { error ->

            Text(

                text = error,

                color = Color(0xFFFF7777),

                fontSize = 13.sp,

                modifier = Modifier.padding(vertical = 8.dp)
            )
        }


        // --------------------------------
        // SEARCH RESULTS
        // --------------------------------

        LazyColumn(

            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),

            verticalArrangement =
                Arrangement.spacedBy(12.dp)

        ) {

            items(

                items = searchResults,

                key = { song -> song.id }

            ) { song ->

                SongResultItem(

                    song = song,

                    isLoading =
                        isResolving &&
                                currentSong?.id == song.id,

                    onClick = {

                        viewModel.playSong(song)
                    }
                )
            }
        }


        // --------------------------------
        // NOW PLAYING
        // --------------------------------

        currentSong?.let { song ->

            Row(

                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF202020))
                    .padding(12.dp),

                verticalAlignment =
                    Alignment.CenterVertically

            ) {

                AsyncImage(

                    model = song.thumbnailUrl,

                    contentDescription = song.title,

                    contentScale = ContentScale.Crop,

                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                )


                Spacer(
                    modifier = Modifier.width(12.dp)
                )


                Column(

                    modifier = Modifier.weight(1f)

                ) {

                    Text(

                        text = song.title,

                        color = Color.White,

                        fontWeight = FontWeight.Bold,

                        maxLines = 1,

                        overflow = TextOverflow.Ellipsis
                    )

                    Text(

                        text = if (isResolving) {
                            "Loading audio..."
                        } else if (isPlaying) {
                            "Now playing"
                        } else {
                            "Paused"
                        },

                        color = accent,

                        fontSize = 12.sp
                    )
                }


                IconButton(

                    onClick = {

                        viewModel.togglePlayPause()
                    },

                    enabled = !isResolving

                ) {

                    Text(

                        text = if (isPlaying) "Ⅱ" else "▶",

                        color = accent,

                        fontSize = 22.sp
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

    isLoading: Boolean = false,

    onClick: () -> Unit

) {

    Row(

        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable {
                onClick()
            }
            .padding(vertical = 6.dp),

        verticalAlignment =
            Alignment.CenterVertically

    ) {

        // THUMBNAIL

        AsyncImage(

            model = song.thumbnailUrl,

            contentDescription = song.title,

            contentScale = ContentScale.Crop,

            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.DarkGray)
        )


        Spacer(
            modifier = Modifier.width(12.dp)
        )


        // SONG INFORMATION

        Column(

            modifier = Modifier.weight(1f)

        ) {

            Text(

                text = song.title,

                color = Color.White,

                fontSize = 15.sp,

                fontWeight = FontWeight.Medium,

                maxLines = 1,

                overflow = TextOverflow.Ellipsis
            )


            Text(

                text = song.artist,

                color = Color.Gray,

                fontSize = 13.sp,

                maxLines = 1,

                overflow = TextOverflow.Ellipsis
            )
        }


        // LOADING INDICATOR

        if (isLoading) {

            CircularProgressIndicator(

                modifier = Modifier.size(22.dp),

                color = Color(0xFFB9F36A),

                strokeWidth = 2.dp
            )
        }
    }
}