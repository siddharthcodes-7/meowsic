package com.surya.meowsic.presentation.library

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.surya.meowsic.data.local.database.MeowsicDatabase
import com.surya.meowsic.data.repository.LibraryRepository
import com.surya.meowsic.domain.Song
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LikedSongsViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = LibraryRepository(MeowsicDatabase.getInstance(application))

    val songs: StateFlow<List<Song>> = repo.observeLikedSongs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleLike(song: Song) {
        viewModelScope.launch {
            repo.toggleLike(song)
        }
    }
}
