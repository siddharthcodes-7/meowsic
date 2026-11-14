package com.surya.meowsic.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "liked_songs",
    foreignKeys = [
        ForeignKey(
            entity = SongEntity::class,
            parentColumns = ["songId"],
            childColumns = ["songId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["songId"])
    ]
)
data class LikedSongEntity(
    @PrimaryKey
    val songId: String,
    val likedAt: Long = System.currentTimeMillis()
)
