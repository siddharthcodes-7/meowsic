package com.surya.meowsic.data.repository

import com.surya.meowsic.data.remote.*
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class YouTubeRepositoryTest {

    private class FakeYouTubeApiService(
        private val response: YouTubeSearchResponse? = null,
        private val exception: Exception? = null
    ) : YouTubeApiService {
        override suspend fun searchSongs(
            part: String,
            type: String,
            maxResults: Int,
            query: String,
            apiKey: String
        ): YouTubeSearchResponse {
            if (exception != null) throw exception
            return response ?: YouTubeSearchResponse(emptyList())
        }
    }

    @Test
    fun testNormalSearchSuccessful() = runBlocking {
        val fakeResponse = YouTubeSearchResponse(
            items = listOf(
                YouTubeSearchItem(
                    id = YouTubeVideoId(videoId = "vid123"),
                    snippet = YouTubeSnippet(
                        title = "Test Song &amp; Artist",
                        channelTitle = "Test Channel",
                        thumbnails = YouTubeThumbnails(
                            high = YouTubeThumbnailDetails("https://example.com/high.jpg")
                        )
                    )
                )
            )
        )

        val repository = YouTubeRepository(
            apiService = FakeYouTubeApiService(fakeResponse),
            apiKey = "VALID_KEY"
        )

        val result = repository.searchSongs("test")
        assertTrue(result.isSuccess)
        val songs = result.getOrNull()
        assertNotNull(songs)
        assertEquals(1, songs!!.size)
        assertEquals("vid123", songs[0].id)
        assertEquals("Test Song & Artist", songs[0].title) // Decoded HTML
        assertEquals("Test Channel", songs[0].artist)
        assertEquals("https://example.com/high.jpg", songs[0].thumbnailUrl)
    }

    @Test
    fun testEmptyQueryReturnsEmptyList() = runBlocking {
        val repository = YouTubeRepository(
            apiService = FakeYouTubeApiService(),
            apiKey = "VALID_KEY"
        )

        val result = repository.searchSongs("  ")
        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull()?.size)
    }

    @Test
    fun testMissingApiKeyFails() = runBlocking {
        val repository = YouTubeRepository(
            apiService = FakeYouTubeApiService(),
            apiKey = ""
        )

        val result = repository.searchSongs("test query")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalStateException)
    }

    @Test
    fun testNetworkFailureHandled() = runBlocking {
        val repository = YouTubeRepository(
            apiService = FakeYouTubeApiService(exception = IOException("No internet connection")),
            apiKey = "VALID_KEY"
        )

        val result = repository.searchSongs("query")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Network error") == true)
    }

    @Test
    fun testQuotaExceededHandled() = runBlocking {
        val errorResponseBody = "{\"error\": {\"code\": 403, \"message\": \"quotaExceeded\"}}"
            .toResponseBody("application/json".toMediaTypeOrNull())
        val httpException = HttpException(Response.error<YouTubeSearchResponse>(403, errorResponseBody))

        val repository = YouTubeRepository(
            apiService = FakeYouTubeApiService(exception = httpException),
            apiKey = "VALID_KEY"
        )

        val result = repository.searchSongs("query")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("quota exceeded", ignoreCase = true) == true)
    }

    @Test
    fun testNoResultsHandled() = runBlocking {
        val fakeResponse = YouTubeSearchResponse(items = emptyList())
        val repository = YouTubeRepository(
            apiService = FakeYouTubeApiService(fakeResponse),
            apiKey = "VALID_KEY"
        )

        val result = repository.searchSongs("rare non existing query")
        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull()?.size)
    }
}
