package com.surya.meowsic.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.surya.meowsic.data.local.entities.SongEntity

@Dao
interface SongDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSong(song: SongEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSongs(songs: List<SongEntity>)

    @Query("SELECT * FROM songs WHERE songId = :songId")
    suspend fun getSongById(songId: String): SongEntity?
}
