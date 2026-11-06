package com.surya.meowsic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MeoMusicApp()
        }
    }
}

val background = Color(0xFF101010)
val accent = Color(0xFFB9F36A)

@Composable
fun MeoMusicApp() {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        containerColor = background,
        bottomBar = {
            // We use a Column here to stack the MiniPlayer directly on top of the NavigationBar
            Column {
                MiniPlayer()

                NavigationBar(
                    containerColor = Color(0xFF181818)
                ) {
                    val items = listOf("Home", "Search", "Library", "Profile")
                    val icons = listOf(
                        Icons.Default.Home,
                        Icons.Default.Search,
                        Icons.Default.LibraryMusic,
                        Icons.Default.Person
                    )

                    items.forEachIndexed { index, title ->
                        NavigationBarItem(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            icon = {
                                Icon(
                                    icons[index],
                                    contentDescription = title
                                )
                            },
                            label = { Text(title) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = accent,
                                selectedTextColor = accent,
                                indicatorColor = Color(0xFF303D20),
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (selectedTab) {
                0 -> HomeScreen()
                1 -> SimpleScreen("Search")
                2 -> SimpleScreen("Your Library")
                3 -> SimpleScreen("Profile")
            }
        }
    }
}

@Composable
fun MiniPlayer() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(Color(0xFF202020)) // Slightly lighter than background so it stands out
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Placeholder for Album Art
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Color.DarkGray, RoundedCornerShape(8.dp))
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Song Title & Artist
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Starboy",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "The Weeknd, Daft Punk",
                color = Color.LightGray,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Playback Controls
        IconButton(onClick = { /* TODO: Toggle Play/Pause */ }) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Play",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }

        IconButton(onClick = { /* TODO: Skip Next */ }) {
            Icon(
                imageVector = Icons.Default.SkipNext,
                contentDescription = "Skip Next",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
fun HomeScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .padding(24.dp)
    ) {
        Text("meo music", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
        Text("Your music. Your world.", color = Color.LightGray, fontSize = 15.sp)

        Spacer(modifier = Modifier.height(35.dp))

        Text("Made for you", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(18.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .background(accent, RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("Discover your music", color = Color.Black, fontSize = 23.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(35.dp))

        Text("Your Library", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        Text("Your songs will appear here.", color = Color.Gray, fontSize = 15.sp)
    }
}

@Composable
fun SimpleScreen(title: String) {
    Box(
        modifier = Modifier.fillMaxSize().background(background),
        contentAlignment = Alignment.Center
    ) {
        Text(title, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
    }
}