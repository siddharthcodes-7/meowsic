package com.surya.meowsic.data.local.model

import androidx.room.Embedded
import com.surya.meowsic.data.local.entities.PlaylistEntity

data class PlaylistWithCount(
    @Embedded val playlist: PlaylistEntity,
    val songCount: Int
)

data class PlaylistSongThumbnail(
    val playlistId: Long,
    val thumbnailUrl: String
)

data class PlaylistSummary(
    val id: Long,
    val name: String,
    val coverImagePath: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val songCount: Int,
    val thumbnails: List<String>
)
