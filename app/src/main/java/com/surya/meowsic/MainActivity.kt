package com.surya.meowsic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.surya.meowsic.presentation.home.HomeScreen
import com.surya.meowsic.presentation.library.LibraryScreen
import com.surya.meowsic.presentation.library.LibraryViewModel
import com.surya.meowsic.presentation.player.FullScreenPlayer
import com.surya.meowsic.presentation.player.MiniPlayer
import com.surya.meowsic.presentation.search.SearchScreen
import com.surya.meowsic.presentation.search.SearchViewModel
import com.surya.meowsic.ui.glass.GlassBackdrop
import com.surya.meowsic.ui.glass.GlassTabBar
import com.surya.meowsic.ui.glass.LocalAmbientPalette
import com.surya.meowsic.ui.theme.MeowsicTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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
    val libraryViewModel: LibraryViewModel = viewModel()
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    val currentSong by searchViewModel.currentSong.collectAsState()
    val isPlaying by searchViewModel.isPlaying.collectAsState()
    val isFullScreenOpen by searchViewModel.isFullScreenOpen.collectAsState()
    val errorMessage by searchViewModel.errorMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(errorMessage) {
        val msg = errorMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            searchViewModel.clearError()
        }
    }

    BackHandler(enabled = isFullScreenOpen) {
        searchViewModel.closeFullScreen()
    }

    GlassBackdrop(
        thumbnailUrl = currentSong?.thumbnailUrl,
        isPlaying = isPlaying
    ) {
        val palette = LocalAmbientPalette.current

        // Main Tab Screens
        Box(modifier = Modifier.fillMaxSize()) {
            Crossfade(
                targetState = selectedTab,
                animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
                label = "mainTabCrossfade"
            ) { tabIndex ->
                when (tabIndex) {
                    0 -> HomeScreen(
                        searchViewModel = searchViewModel,
                        libraryViewModel = libraryViewModel,
                        onNavigateToSearch = { selectedTab = 1 },
                        onNavigateToLibrary = { selectedTab = 2 }
                    )
                    1 -> SearchScreen(
                        viewModel = searchViewModel,
                        libraryViewModel = libraryViewModel
                    )
                    2 -> LibraryScreen(
                        searchViewModel = searchViewModel
                    )
                }
            }
        }

        // Bottom Floating Meowsic MiniPlayer + Expressive Navigation Bar
        AnimatedVisibility(
            visible = !isFullScreenOpen,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
            ) + fadeIn(tween(200)),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(200)
            ) + fadeOut(tween(150))
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedVisibility(
                    visible = currentSong != null,
                    enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut()
                ) {
                    MiniPlayer(
                        viewModel = searchViewModel,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        onOpenFullScreen = { searchViewModel.openFullScreen() }
                    )
                }

                GlassTabBar(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                    palette = palette
                )
            }
        }

        // Meowsic Unified FullScreenPlayer Sheet Overlay
        AnimatedVisibility(
            visible = isFullScreenOpen && currentSong != null,
            modifier = Modifier.fillMaxSize(),
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ) + fadeIn(tween(220)),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(250, easing = FastOutSlowInEasing)
            ) + fadeOut(tween(200))
        ) {
            FullScreenPlayer(
                viewModel = searchViewModel,
                onDismiss = { searchViewModel.closeFullScreen() }
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = if (currentSong != null) 168.dp else 96.dp, start = 16.dp, end = 16.dp)
        ) { data ->
            Snackbar(
                snackbarData = data,
                containerColor = MaterialTheme.colorScheme.inverseSurface,
                contentColor = MaterialTheme.colorScheme.inverseOnSurface
            )
        }
    }
}
