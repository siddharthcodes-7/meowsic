package com.surya.meowsic.presentation.library

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.surya.meowsic.data.local.database.MeowsicDatabase
import com.surya.meowsic.data.local.model.PlaylistWithCount
import com.surya.meowsic.data.repository.LibraryRepository
import com.surya.meowsic.domain.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LibraryViewModel(application: Application) : AndroidViewModel(application) {

    private val db = MeowsicDatabase.getInstance(application)
    private val repo = LibraryRepository(db)

    val likedSongs: StateFlow<List<Song>> = repo.observeLikedSongs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val likedCount: StateFlow<Int> = repo.observeLikedCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val playlistsWithCount: StateFlow<List<PlaylistWithCount>> = repo.observePlaylistsWithCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val playlistThumbnails: StateFlow<Map<Long, List<String>>> = repo.observePlaylistThumbnails()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    fun toggleLike(song: Song) {
        viewModelScope.launch {
            repo.toggleLike(song)
        }
    }

    fun isLiked(songId: String): Flow<Boolean> = repo.isLiked(songId)

    suspend fun createPlaylist(name: String, coverImagePath: String? = null): Long =
        repo.createPlaylist(name, coverImagePath)

    fun renamePlaylist(playlistId: Long, name: String) {
        viewModelScope.launch {
            repo.renamePlaylist(playlistId, name)
        }
    }

    fun updatePlaylistCover(playlistId: Long, path: String?) {
        viewModelScope.launch {
            repo.updatePlaylistCover(playlistId, path)
        }
    }

    fun deletePlaylist(playlistId: Long, context: Context) {
        viewModelScope.launch {
            repo.deletePlaylist(playlistId, context)
        }
    }

    fun addSongToPlaylist(playlistId: Long, song: Song, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = repo.addSongToPlaylist(playlistId, song)
            onResult(result)
        }
    }

    fun removeSongFromPlaylist(playlistId: Long, songId: String) {
        viewModelScope.launch {
            repo.removeSongFromPlaylist(playlistId, songId)
        }
    }
}
