package com.surya.meowsic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import com.surya.meowsic.presentation.home.HomeScreen
import com.surya.meowsic.presentation.player.MiniPlayer
import com.surya.meowsic.presentation.search.SearchScreen

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
                1 -> SearchScreen()
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