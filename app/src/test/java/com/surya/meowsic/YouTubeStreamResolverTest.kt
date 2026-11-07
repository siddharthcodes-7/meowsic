package com.surya.meowsic

import com.surya.meowsic.data.stream.YouTubeStreamResolver
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class YouTubeStreamResolverTest {

    @Before
    fun setUp() {
        YouTubeStreamResolver.initialize()
    }

    @Test
    fun testCanonicalUrlNormalization() {
        assertEquals(
            "https://www.youtube.com/watch?v=kJQP7kiw5Fk",
            YouTubeStreamResolver.toCanonicalUrl("kJQP7kiw5Fk")
        )
        assertEquals(
            "https://www.youtube.com/watch?v=kJQP7kiw5Fk",
            YouTubeStreamResolver.toCanonicalUrl("https://www.youtube.com/watch?v=kJQP7kiw5Fk")
        )
    }

    @Test
    fun testResolveValidVideoAudioUrl() = runBlocking {
        val videoId = "kJQP7kiw5Fk"
        val url = YouTubeStreamResolver.resolveAudioUrl(videoId)

        assertNotNull("Resolved URL should not be null", url)
        assertTrue("URL should start with https://", url!!.startsWith("https://"))

        // Test caching returns the exact same URL immediately
        val cachedUrl = YouTubeStreamResolver.resolveAudioUrl(videoId)
        assertEquals("Cached stream should match", url, cachedUrl)

        // Test invalidation clears cache and force refresh re-extracts
        YouTubeStreamResolver.invalidate(videoId)
        val refreshedUrl = YouTubeStreamResolver.resolveAudioUrl(videoId, forceRefresh = true)
        assertNotNull("Refreshed stream should not be null", refreshedUrl)
    }

    @Test
    fun testInvalidVideoIdReturnsNull() = runBlocking {
        val invalidId = "invalid_id_not_exist_xyz123"
        val result = YouTubeStreamResolver.resolveAudioUrl(invalidId)
        assertNull("Invalid video ID should fail gracefully and return null", result)
    }
}
