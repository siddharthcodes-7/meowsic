package com.surya.meowsic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.surya.meowsic.data.stream.YouTubeStreamResolver
import com.surya.meowsic.presentation.home.HomeScreen
import com.surya.meowsic.presentation.library.LibraryScreen
import com.surya.meowsic.presentation.player.FullScreenPlayer
import com.surya.meowsic.presentation.player.MiniPlayer
import com.surya.meowsic.presentation.search.SearchScreen
import com.surya.meowsic.presentation.search.SearchViewModel
import com.surya.meowsic.ui.glass.AmbientBackground
import com.surya.meowsic.ui.glass.GlassBackdropHost
import com.surya.meowsic.ui.glass.GlassTabBar
import com.surya.meowsic.ui.glass.GlassTabItem
import com.surya.meowsic.ui.glass.WithoutHaze
import com.surya.meowsic.ui.theme.LiquidGlassTokens
import com.surya.meowsic.ui.theme.LocalAmbientPalette
import com.surya.meowsic.ui.theme.MeowsicTheme
import dev.chrisbanes.haze.hazeSource

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Authoritative NewPipe initialization (single-point initialization)
        YouTubeStreamResolver.initialize()

        setContent {
            val searchViewModel: SearchViewModel = viewModel()
            val currentSong by searchViewModel.currentSong.collectAsState()

            // Pass current song ID so MeowsicTheme can animate the ambient palette
            MeowsicTheme(currentSongId = currentSong?.id) {
                MeoMusicApp(searchViewModel = searchViewModel)
            }
        }
    }
}

@Composable
fun MeoMusicApp(
    searchViewModel: SearchViewModel = viewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val isFullScreenPlayerOpen by searchViewModel.isFullScreenOpen.collectAsState()
    val currentSong by searchViewModel.currentSong.collectAsState()
    val palette = LocalAmbientPalette.current

    BackHandler(enabled = isFullScreenPlayerOpen) {
        searchViewModel.closeFullScreen()
    }

    val tabItems = remember {
        listOf(
            GlassTabItem("Home", Icons.Default.Home, 0),
            GlassTabItem("Search", Icons.Default.Search, 1),
            GlassTabItem("Library", Icons.Default.LibraryMusic, 2)
        )
    }

    GlassBackdropHost { hazeState ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(LiquidGlassTokens.AmoledVoid)
        ) {
            // ── Layer 1: Animated ambient light background (behind everything) ──
            AmbientBackground(
                palette = palette,
                modifier = Modifier.fillMaxSize(),
                opacity = 0.30f
            )

            // ── Layer 2: Source content container that acts as the haze backdrop source ──
            // SIBLING of the floating bars (NOT ancestor) to avoid recursive render invalidation.
            // Screen content is wrapped in WithoutHaze so cards/lists never sample their own ancestor hazeSource.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .hazeSource(hazeState)
            ) {
                WithoutHaze {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 160.dp) // Reserve space so last list item scrolls above floating nav
                    ) {
                        when (selectedTab) {
                            0 -> HomeScreen()
                            1 -> SearchScreen(viewModel = searchViewModel)
                            2 -> LibraryScreen(searchViewModel = searchViewModel)
                        }
                    }
                }
            }

            // ── Layer 3: Floating bottom chrome (MiniPlayer + GlassTabBar) ──
            // Placed OUTSIDE the hazeSource Box as true siblings so they safely blur the content behind them.
            // Hidden when FullScreenPlayer is open so MiniPlayer does not recompose behind FullScreenPlayer.
            AnimatedVisibility(
                visible = !isFullScreenPlayerOpen,
                enter = fadeIn(tween(200)),
                exit = fadeOut(tween(150)),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Floating MiniPlayer (acts as tabViewBottomAccessory)
                    MiniPlayer(
                        viewModel = searchViewModel,
                        onOpenFullScreen = { searchViewModel.openFullScreen() }
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Floating Glass Tab Bar
                    GlassTabBar(
                        items = tabItems,
                        selectedIndex = selectedTab,
                        onTabSelected = { selectedTab = it },
                        modifier = Modifier.fillMaxWidth(),
                        hazeState = hazeState,
                        palette = palette
                    )
                }
            }

            // ── Layer 4: Full-screen player modal ──
            // Isolated with WithoutHaze so it never clashes with the main window blur
            AnimatedVisibility(
                visible = isFullScreenPlayerOpen && currentSong != null,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(durationMillis = 320)
                ) + fadeIn(tween(220)),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(durationMillis = 280)
                ) + fadeOut(tween(180)),
                modifier = Modifier.fillMaxSize()
            ) {
                WithoutHaze {
                    FullScreenPlayer(
                        viewModel = searchViewModel,
                        onDismiss = { searchViewModel.closeFullScreen() }
                    )
                }
            }
        }
    }
}
