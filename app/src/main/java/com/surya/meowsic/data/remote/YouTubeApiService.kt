package com.surya.meowsic.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface YouTubeApiService {
    @GET("search")
    suspend fun searchSongs(
        @Query("part") part: String = "snippet",
        @Query("type") type: String = "video",
        @Query("q") query: String,
        @Query("key") apiKey: String
    ): YouTubeSearchResponse
}

// DTO Response classes matching YouTube JSON structure
data class YouTubeSearchResponse(val items: List<SearchItem>)
data class SearchItem(val id: VideoId, val snippet: Snippet)
data class VideoId(val videoId: String)
data class Snippet(val title: String, val channelTitle: String, val thumbnails: Thumbnails)
data class Thumbnails(val high: ThumbnailDetails)
data class ThumbnailDetails(val url: String)