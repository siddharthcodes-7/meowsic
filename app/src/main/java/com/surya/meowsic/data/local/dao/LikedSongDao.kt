package com.surya.meowsic.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.surya.meowsic.data.local.entities.LikedSongEntity
import com.surya.meowsic.data.local.entities.SongEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class LikedSongDao {

    @Query("""
        SELECT s.* FROM songs s 
        INNER JOIN liked_songs l ON s.songId = l.songId 
        ORDER BY l.likedAt DESC
    """)
    abstract fun observeLikedSongs(): Flow<List<SongEntity>>

    @Query("SELECT COUNT(*) FROM liked_songs")
    abstract fun observeLikedCount(): Flow<Int>

    @Query("SELECT EXISTS(SELECT 1 FROM liked_songs WHERE songId = :songId)")
    abstract fun isLiked(songId: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM liked_songs WHERE songId = :songId)")
    abstract suspend fun isLikedDirect(songId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insertLikedSong(likedSong: LikedSongEntity): Long

    @Query("DELETE FROM liked_songs WHERE songId = :songId")
    abstract suspend fun deleteLikedSong(songId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun upsertSongInternal(song: SongEntity)

    @Transaction
    open suspend fun likeSong(song: SongEntity) {
        upsertSongInternal(song)
        insertLikedSong(LikedSongEntity(songId = song.songId, likedAt = System.currentTimeMillis()))
    }

    @Transaction
    open suspend fun unlikeSong(songId: String) {
        deleteLikedSong(songId)
    }
}
