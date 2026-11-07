package com.surya.meowsic.data.repository

import android.os.Build
import android.text.Html
import com.surya.meowsic.BuildConfig
import com.surya.meowsic.data.remote.YouTubeApiService
import com.surya.meowsic.domain.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

class YouTubeRepository(
    private val apiService: YouTubeApiService? = null,
    private val apiKey: String = BuildConfig.YOUTUBE_API_KEY
) {

    private val api: YouTubeApiService by lazy {
        apiService ?: createDefaultService()
    }

    suspend fun searchSongs(query: String): Result<List<Song>> = withContext(Dispatchers.IO) {
        val trimmedQuery = query.trim()
        if (trimmedQuery.length < 2) {
            return@withContext Result.success(emptyList())
        }

        if (apiKey.isBlank()) {
            return@withContext Result.failure(
                IllegalStateException("YouTube API key is missing. Please configure YOUTUBE_API_KEY.")
            )
        }

        try {
            val response = api.searchSongs(
                query = trimmedQuery,
                apiKey = apiKey
            )

            val songs = response.items.orEmpty().mapNotNull { item ->
                val videoId = item.id?.videoId
                val snippet = item.snippet
                if (videoId.isNullOrBlank() || snippet == null) {
                    return@mapNotNull null
                }

                val rawTitle = snippet.title ?: "Unknown Title"
                val cleanedTitle = decodeHtml(rawTitle)
                val rawArtist = snippet.channelTitle ?: "Unknown Artist"
                val cleanedArtist = decodeHtml(rawArtist)

                val thumbnailUrl = snippet.thumbnails?.high?.url
                    ?: snippet.thumbnails?.medium?.url
                    ?: snippet.thumbnails?.default?.url
                    ?: ""

                Song(
                    id = videoId,
                    title = cleanedTitle,
                    artist = cleanedArtist,
                    thumbnailUrl = thumbnailUrl,
                    duration = ""
                )
            }

            Result.success(songs)
        } catch (e: HttpException) {
            val message = when (e.code()) {
                400 -> "Invalid YouTube API request or API key."
                403 -> "YouTube API quota exceeded or access forbidden."
                404 -> "YouTube service not found."
                else -> "YouTube API error (${e.code()}): ${e.message()}"
            }
            Result.failure(IOException(message, e))
        } catch (e: IOException) {
            Result.failure(IOException("Network error: ${e.message}", e))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun decodeHtml(text: String): String {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY).toString()
            } else {
                @Suppress("DEPRECATION")
                Html.fromHtml(text).toString()
            }
        } catch (_: Throwable) {
            // Fallback for tests or environments where Android Html is not mocked
            text.replace("&amp;", "&")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
        }
    }

    companion object {
        private const val BASE_URL = "https://www.googleapis.com/youtube/v3/"

        private fun createDefaultService(): YouTubeApiService {
            val client = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(YouTubeApiService::class.java)
        }
    }
}
