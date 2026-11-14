package com.surya.meowsic.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey
    val songId: String,
    val title: String,
    val artist: String,
    val thumbnailUrl: String,
    val duration: String = "",
    val durationMs: Long = 0L
)
