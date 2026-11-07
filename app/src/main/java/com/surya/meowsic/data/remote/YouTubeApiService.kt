package com.surya.meowsic.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

interface YouTubeApiService {
    @GET("search")
    suspend fun searchSongs(
        @Query("part") part: String = "snippet",
        @Query("type") type: String = "video",
        @Query("maxResults") maxResults: Int = 25,
        @Query("q") query: String,
        @Query("key") apiKey: String
    ): YouTubeSearchResponse
}

data class YouTubeSearchResponse(
    @SerializedName("items")
    val items: List<YouTubeSearchItem>? = null,
    @SerializedName("nextPageToken")
    val nextPageToken: String? = null
)

data class YouTubeSearchItem(
    @SerializedName("id")
    val id: YouTubeVideoId? = null,
    @SerializedName("snippet")
    val snippet: YouTubeSnippet? = null
)

data class YouTubeVideoId(
    @SerializedName("videoId")
    val videoId: String? = null
)

data class YouTubeSnippet(
    @SerializedName("title")
    val title: String? = null,
    @SerializedName("channelTitle")
    val channelTitle: String? = null,
    @SerializedName("thumbnails")
    val thumbnails: YouTubeThumbnails? = null
)

data class YouTubeThumbnails(
    @SerializedName("high")
    val high: YouTubeThumbnailDetails? = null,
    @SerializedName("medium")
    val medium: YouTubeThumbnailDetails? = null,
    @SerializedName("default")
    val default: YouTubeThumbnailDetails? = null
)

data class YouTubeThumbnailDetails(
    @SerializedName("url")
    val url: String? = null
)
