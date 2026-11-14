package com.surya.meowsic.presentation.library

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.surya.meowsic.data.local.database.MeowsicDatabase
import com.surya.meowsic.data.local.entities.PlaylistEntity
import com.surya.meowsic.data.repository.LibraryRepository
import com.surya.meowsic.domain.Song
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PlaylistDetailViewModel(
    application: Application,
    val playlistId: Long
) : AndroidViewModel(application) {

    private val repo = LibraryRepository(MeowsicDatabase.getInstance(application))

    val playlist: StateFlow<PlaylistEntity?> = repo.observePlaylistById(playlistId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val songs: StateFlow<List<Song>> = repo.observePlaylistSongs(playlistId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun removeSong(songId: String) {
        viewModelScope.launch {
            repo.removeSongFromPlaylist(playlistId, songId)
        }
    }

    fun addSong(song: Song) {
        viewModelScope.launch {
            repo.addSongToPlaylist(playlistId, song)
        }
    }

    fun reorderSongs(songIdsInOrder: List<String>) {
        viewModelScope.launch {
            repo.reorderPlaylistSongs(playlistId, songIdsInOrder)
        }
    }

    companion object {
        fun factory(app: Application, playlistId: Long): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                    return PlaylistDetailViewModel(app, playlistId) as T
                }
            }
    }
}
