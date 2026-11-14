package com.surya.meowsic.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.surya.meowsic.data.local.dao.LikedSongDao
import com.surya.meowsic.data.local.dao.PlaylistDao
import com.surya.meowsic.data.local.dao.SongDao
import com.surya.meowsic.data.local.entities.LikedSongEntity
import com.surya.meowsic.data.local.entities.PlaylistEntity
import com.surya.meowsic.data.local.entities.PlaylistSongEntity
import com.surya.meowsic.data.local.entities.SongEntity

@Database(
    entities = [
        SongEntity::class,
        LikedSongEntity::class,
        PlaylistEntity::class,
        PlaylistSongEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class MeowsicDatabase : RoomDatabase() {

    abstract fun songDao(): SongDao
    abstract fun likedSongDao(): LikedSongDao
    abstract fun playlistDao(): PlaylistDao

    companion object {
        private const val DATABASE_NAME = "meowsic.db"

        @Volatile
        private var INSTANCE: MeowsicDatabase? = null

        fun getInstance(context: Context): MeowsicDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context.applicationContext).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): MeowsicDatabase {
            return Room.databaseBuilder(
                context,
                MeowsicDatabase::class.java,
                DATABASE_NAME
            )
            // No fallbackToDestructiveMigration - all future schema changes use proper Migrations
            .build()
        }
    }
}
