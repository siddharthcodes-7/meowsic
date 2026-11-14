package com.surya.meowsic.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "playlist_songs",
    foreignKeys = [
        ForeignKey(
            entity = PlaylistEntity::class,
            parentColumns = ["id"],
            childColumns = ["playlistId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = SongEntity::class,
            parentColumns = ["songId"],
            childColumns = ["songId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["playlistId", "songId"], unique = true),
        Index(value = ["songId"])
    ]
)
data class PlaylistSongEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val playlistId: Long,
    val songId: String,
    val position: Int,
    val addedAt: Long = System.currentTimeMillis()
)
