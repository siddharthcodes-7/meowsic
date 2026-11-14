package com.surya.meowsic.data.local.mapper

import com.surya.meowsic.data.local.entities.SongEntity
import com.surya.meowsic.domain.Song

fun SongEntity.toDomain(): Song = Song(
    id = songId,
    title = title,
    artist = artist,
    thumbnailUrl = thumbnailUrl,
    duration = duration
)

fun Song.toEntity(durationMs: Long = 0L): SongEntity = SongEntity(
    songId = id,
    title = title,
    artist = artist,
    thumbnailUrl = thumbnailUrl,
    duration = duration,
    durationMs = durationMs
)
