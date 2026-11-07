package com.surya.meowsic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.surya.meowsic.data.stream.YouTubeStreamResolver
import com.surya.meowsic.presentation.home.HomeScreen
import com.surya.meowsic.presentation.player.MiniPlayer
import com.surya.meowsic.presentation.search.SearchScreen
import com.surya.meowsic.presentation.search.SearchViewModel
import com.surya.meowsic.ui.theme.MeowsicTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Authoritative NewPipe initialization (single-point initialization)
        YouTubeStreamResolver.initialize()

        setContent {
            MeowsicTheme {
                MeoMusicApp()
            }
        }
    }
}

val background = Color(0xFF101010)
val accent = Color(0xFFB9F36A)

@Composable
fun MeoMusicApp(
    searchViewModel: SearchViewModel = viewModel()
) {
    var selectedTab by remember { mutableIntStateOf(1) } // Default to Search tab so user can immediately search & play

    Scaffold(
        containerColor = background,
        bottomBar = {
            Column {
                MiniPlayer(viewModel = searchViewModel)

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
                1 -> SearchScreen(viewModel = searchViewModel)
                2 -> SimpleScreen("Your Library")
                3 -> SimpleScreen("Profile")
            }
        }
    }
}

@Composable
fun SimpleScreen(title: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(background),
        contentAlignment = Alignment.Center
    ) {
        Text(title, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
    }
}
