package com.surya.meowsic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.surya.meowsic.data.stream.YouTubeStreamResolver
import com.surya.meowsic.presentation.home.HomeScreen
import com.surya.meowsic.presentation.player.FullScreenPlayer
import com.surya.meowsic.presentation.player.MiniPlayer
import com.surya.meowsic.presentation.search.SearchScreen
import com.surya.meowsic.presentation.search.SearchViewModel
import com.surya.meowsic.ui.theme.LiquidGlassTokens
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

@Composable
fun MeoMusicApp(
    searchViewModel: SearchViewModel = viewModel()
) {
    var selectedTab by remember { mutableIntStateOf(1) } // Default to Search tab
    val isFullScreenPlayerOpen by searchViewModel.isFullScreenOpen.collectAsState()
    val currentSong by searchViewModel.currentSong.collectAsState()

    // Handle back button to collapse full-screen player when visible
    BackHandler(enabled = isFullScreenPlayerOpen) {
        searchViewModel.closeFullScreen()
    }

    // 1. True Void Canvas: Pure #000000 global root background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LiquidGlassTokens.AmoledVoid)
    ) {
        Scaffold(
            containerColor = LiquidGlassTokens.AmoledVoid,
            bottomBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                ) {
                    // Floating Mini-Player
                    MiniPlayer(
                        viewModel = searchViewModel,
                        onOpenFullScreen = { searchViewModel.openFullScreen() }
                    )

                    // Floating Liquid Glass Navigation Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(LiquidGlassTokens.SurfaceGlassElevated)
                            .border(
                                width = 1.dp,
                                brush = LiquidGlassTokens.SpecularRimGradient,
                                shape = RoundedCornerShape(24.dp)
                            )
                    ) {
                        NavigationBar(
                            containerColor = Color.Transparent,
                            tonalElevation = 0.dp,
                            modifier = Modifier.height(64.dp)
                        ) {
                            val items = listOf("Home", "Search", "Library", "Profile")
                            val icons = listOf(
                                Icons.Default.Home,
                                Icons.Default.Search,
                                Icons.Default.LibraryMusic,
                                Icons.Default.Person
                            )

                            items.forEachIndexed { index, title ->
                                val isSelected = selectedTab == index
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = { selectedTab = index },
                                    icon = {
                                        Icon(
                                            icons[index],
                                            contentDescription = title,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = title,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = LiquidGlassTokens.ElectricLime,
                                        selectedTextColor = LiquidGlassTokens.ElectricLime,
                                        indicatorColor = LiquidGlassTokens.SurfaceGlassActive,
                                        unselectedIconColor = LiquidGlassTokens.TextMuted,
                                        unselectedTextColor = LiquidGlassTokens.TextMuted
                                    )
                                )
                            }
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

        // Full-screen AMOLED Liquid Glass music player
        AnimatedVisibility(
            visible = isFullScreenPlayerOpen && currentSong != null,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = LiquidGlassTokens.SlideSpringSpec
            ),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = LiquidGlassTokens.SlideSpringSpec
            ),
            modifier = Modifier.fillMaxSize()
        ) {
            FullScreenPlayer(
                viewModel = searchViewModel,
                onDismiss = { searchViewModel.closeFullScreen() }
            )
        }
    }
}

@Composable
fun SimpleScreen(title: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LiquidGlassTokens.AmoledVoid),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = LiquidGlassTokens.TextPrimary,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = LiquidGlassTokens.HeaderLetterSpacing
        )
    }
}
