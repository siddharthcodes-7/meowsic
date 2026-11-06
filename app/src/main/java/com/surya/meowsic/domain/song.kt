package com.surya.meowsic.domain

data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val thumbnailUrl: String,
    val duration: String
)