package com.surya.meowsic.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.surya.meowsic.data.local.entities.PlaylistEntity
import com.surya.meowsic.data.local.entities.PlaylistSongEntity
import com.surya.meowsic.data.local.entities.SongEntity
import com.surya.meowsic.data.local.model.PlaylistSongThumbnail
import com.surya.meowsic.data.local.model.PlaylistWithCount
import kotlinx.coroutines.flow.Flow

@Dao
abstract class PlaylistDao {

    @Query("""
        SELECT p.*, COUNT(ps.id) AS songCount 
        FROM playlists p 
        LEFT JOIN playlist_songs ps ON p.id = ps.playlistId 
        GROUP BY p.id 
        ORDER BY p.updatedAt DESC
    """)
    abstract fun observePlaylistsWithCount(): Flow<List<PlaylistWithCount>>

    @Query("""
        SELECT ps.playlistId, s.thumbnailUrl 
        FROM playlist_songs ps 
        INNER JOIN songs s ON ps.songId = s.songId 
        ORDER BY ps.playlistId, ps.position ASC
    """)
    abstract fun observeAllPlaylistThumbnails(): Flow<List<PlaylistSongThumbnail>>

    @Query("SELECT * FROM playlists WHERE id = :playlistId")
    abstract fun observePlaylistById(playlistId: Long): Flow<PlaylistEntity?>

    @Query("SELECT * FROM playlists WHERE id = :playlistId")
    abstract suspend fun getPlaylistById(playlistId: Long): PlaylistEntity?

    @Query("""
        SELECT s.* FROM songs s 
        INNER JOIN playlist_songs ps ON s.songId = ps.songId 
        WHERE ps.playlistId = :playlistId 
        ORDER BY ps.position ASC
    """)
    abstract fun observePlaylistSongs(playlistId: Long): Flow<List<SongEntity>>

    @Query("""
        SELECT s.* FROM songs s 
        INNER JOIN playlist_songs ps ON s.songId = ps.songId 
        WHERE ps.playlistId = :playlistId 
        ORDER BY ps.position ASC
    """)
    abstract suspend fun getPlaylistSongsDirect(playlistId: Long): List<SongEntity>

    @Query("SELECT EXISTS(SELECT 1 FROM playlist_songs WHERE playlistId = :playlistId AND songId = :songId)")
    abstract fun isSongInPlaylist(playlistId: Long, songId: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM playlist_songs WHERE playlistId = :playlistId AND songId = :songId)")
    abstract suspend fun isSongInPlaylistDirect(playlistId: Long, songId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Update
    abstract suspend fun updatePlaylist(playlist: PlaylistEntity)

    @Query("UPDATE playlists SET name = :name, updatedAt = :updatedAt WHERE id = :playlistId")
    abstract suspend fun renamePlaylist(playlistId: Long, name: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE playlists SET coverImagePath = :coverImagePath, updatedAt = :updatedAt WHERE id = :playlistId")
    abstract suspend fun updatePlaylistCover(playlistId: Long, coverImagePath: String?, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    abstract suspend fun deletePlaylist(playlistId: Long)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insertPlaylistSong(playlistSong: PlaylistSongEntity): Long

    @Query("DELETE FROM playlist_songs WHERE playlistId = :playlistId AND songId = :songId")
    abstract suspend fun deletePlaylistSong(playlistId: Long, songId: String): Int

    @Query("SELECT MAX(position) FROM playlist_songs WHERE playlistId = :playlistId")
    abstract suspend fun getMaxPosition(playlistId: Long): Int?

    @Query("UPDATE playlist_songs SET position = :newPosition WHERE playlistId = :playlistId AND songId = :songId")
    abstract suspend fun updateSongPosition(playlistId: Long, songId: String, newPosition: Int)

    @Query("UPDATE playlists SET updatedAt = :updatedAt WHERE id = :playlistId")
    abstract suspend fun updatePlaylistTimestamp(playlistId: Long, updatedAt: Long = System.currentTimeMillis())

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun upsertSongInternal(song: SongEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun upsertSongsInternal(songs: List<SongEntity>)

    @Transaction
    open suspend fun addSongToPlaylist(playlistId: Long, song: SongEntity): Boolean {
        upsertSongInternal(song)
        val currentMax = getMaxPosition(playlistId) ?: -1
        val newPosition = currentMax + 1
        val insertedId = insertPlaylistSong(
            PlaylistSongEntity(
                playlistId = playlistId,
                songId = song.songId,
                position = newPosition,
                addedAt = System.currentTimeMillis()
            )
        )
        if (insertedId > 0) {
            updatePlaylistTimestamp(playlistId)
            return true
        }
        return false
    }

    @Transaction
    open suspend fun addSongsToPlaylist(playlistId: Long, songs: List<SongEntity>): Int {
        if (songs.isEmpty()) return 0
        upsertSongsInternal(songs)
        var currentMax = getMaxPosition(playlistId) ?: -1
        var addedCount = 0
        val now = System.currentTimeMillis()
        songs.forEach { song ->
            val position = ++currentMax
            val id = insertPlaylistSong(
                PlaylistSongEntity(
                    playlistId = playlistId,
                    songId = song.songId,
                    position = position,
                    addedAt = now
                )
            )
            if (id > 0) {
                addedCount++
            }
        }
        if (addedCount > 0) {
            updatePlaylistTimestamp(playlistId, now)
        }
        return addedCount
    }

    @Transaction
    open suspend fun removeSongFromPlaylist(playlistId: Long, songId: String) {
        deletePlaylistSong(playlistId, songId)
        // Normalize positions
        val remainingSongs = getPlaylistSongsDirect(playlistId)
        remainingSongs.forEachIndexed { index, song ->
            updateSongPosition(playlistId, song.songId, index)
        }
        updatePlaylistTimestamp(playlistId)
    }

    @Transaction
    open suspend fun reorderSongs(playlistId: Long, songIdsInOrder: List<String>) {
        songIdsInOrder.forEachIndexed { index, songId ->
            updateSongPosition(playlistId, songId, index)
        }
        updatePlaylistTimestamp(playlistId)
    }
}
