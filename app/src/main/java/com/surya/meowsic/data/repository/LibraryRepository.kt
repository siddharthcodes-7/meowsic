package com.surya.meowsic.data.repository

import android.content.Context
import com.surya.meowsic.data.local.database.MeowsicDatabase
import com.surya.meowsic.data.local.entities.PlaylistEntity
import com.surya.meowsic.data.local.mapper.toDomain
import com.surya.meowsic.data.local.mapper.toEntity
import com.surya.meowsic.data.local.model.PlaylistWithCount
import com.surya.meowsic.domain.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File

class LibraryRepository(private val db: MeowsicDatabase) {

    private val likedSongDao = db.likedSongDao()
    private val playlistDao = db.playlistDao()
    private val songDao = db.songDao()

    // ── Liked Songs ──────────────────────────────────────────────────────────

    fun observeLikedSongs(): Flow<List<Song>> =
        likedSongDao.observeLikedSongs().map { entities -> entities.map { it.toDomain() } }

    fun observeLikedCount(): Flow<Int> = likedSongDao.observeLikedCount()

    fun isLiked(songId: String): Flow<Boolean> = likedSongDao.isLiked(songId)

    suspend fun toggleLike(song: Song) {
        val isCurrentlyLiked = likedSongDao.isLikedDirect(song.id)
        if (isCurrentlyLiked) {
            likedSongDao.unlikeSong(song.id)
        } else {
            likedSongDao.likeSong(song.toEntity())
        }
    }

    // ── Playlists ─────────────────────────────────────────────────────────────

    fun observePlaylistsWithCount(): Flow<List<PlaylistWithCount>> =
        playlistDao.observePlaylistsWithCount()

    fun observePlaylistThumbnails(): Flow<Map<Long, List<String>>> =
        playlistDao.observeAllPlaylistThumbnails().map { thumbnails ->
            thumbnails
                .groupBy { it.playlistId }
                .mapValues { (_, items) ->
                    items.map { it.thumbnailUrl }.take(4)
                }
        }

    fun observePlaylistSongs(playlistId: Long): Flow<List<Song>> =
        playlistDao.observePlaylistSongs(playlistId).map { entities -> entities.map { it.toDomain() } }

    fun observePlaylistById(playlistId: Long): Flow<PlaylistEntity?> =
        playlistDao.observePlaylistById(playlistId)

    suspend fun createPlaylist(name: String, coverImagePath: String? = null): Long {
        val entity = PlaylistEntity(
            name = name,
            coverImagePath = coverImagePath,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        return playlistDao.insertPlaylist(entity)
    }

    suspend fun renamePlaylist(playlistId: Long, name: String) {
        playlistDao.renamePlaylist(playlistId, name)
    }

    suspend fun updatePlaylistCover(playlistId: Long, path: String?) {
        playlistDao.updatePlaylistCover(playlistId, path)
    }

    suspend fun deletePlaylist(playlistId: Long, context: Context) {
        // Delete the cover image file if it exists
        val coversDir = File(context.filesDir, "playlist_covers")
        val playlist = playlistDao.getPlaylistById(playlistId)
        playlist?.coverImagePath?.let { coverPath ->
            val coverFile = File(coverPath)
            if (coverFile.exists()) {
                coverFile.delete()
            }
        }
        playlistDao.deletePlaylist(playlistId)
    }

    suspend fun addSongToPlaylist(playlistId: Long, song: Song): Boolean {
        return playlistDao.addSongToPlaylist(playlistId, song.toEntity())
    }

    suspend fun removeSongFromPlaylist(playlistId: Long, songId: String) {
        playlistDao.removeSongFromPlaylist(playlistId, songId)
    }

    suspend fun reorderPlaylistSongs(playlistId: Long, songIdsInOrder: List<String>) {
        playlistDao.reorderSongs(playlistId, songIdsInOrder)
    }

    suspend fun isSongInPlaylistDirect(playlistId: Long, songId: String): Boolean =
        playlistDao.isSongInPlaylistDirect(playlistId, songId)

    fun isSongInPlaylist(playlistId: Long, songId: String): Flow<Boolean> =
        playlistDao.isSongInPlaylist(playlistId, songId)
}
