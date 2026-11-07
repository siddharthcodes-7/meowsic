package com.surya.meowsic.data.stream

import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.MediaFormat
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.localization.ContentCountry
import org.schabi.newpipe.extractor.localization.Localization
import org.schabi.newpipe.extractor.stream.StreamInfo
import org.schabi.newpipe.extractor.stream.VideoStream
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.abs

object YouTubeStreamResolver {

    private const val TAG = "YouTubeStreamResolver"
    private const val DEFAULT_CACHE_DURATION_MS = 20 * 60 * 1000L // 20 minutes

    private data class CachedStream(
        val url: String,
        val timestamp: Long,
        val expiresAtMs: Long?
    ) {
        fun isExpired(): Boolean {
            val now = System.currentTimeMillis()
            if (expiresAtMs != null && now >= expiresAtMs) {
                return true
            }
            return (now - timestamp) > DEFAULT_CACHE_DURATION_MS
        }
    }

    private val streamCache = ConcurrentHashMap<String, CachedStream>()

    @Volatile
    private var initialized = false

    /**
     * Initializes NewPipe exactly once with US localization.
     */
    fun initialize() {
        if (initialized) return
        synchronized(this) {
            if (initialized) return
            try {
                NewPipe.init(
                    NewPipeDownloader(),
                    Localization("en", "US"),
                    ContentCountry("US")
                )
                initialized = true
                Log.d(TAG, "NewPipe initialized successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize NewPipe", e)
            }
        }
    }

    /**
     * Normalizes a video ID or URL into a canonical YouTube watch URL.
     */
    fun toCanonicalUrl(videoIdOrUrl: String): String {
        val trimmed = videoIdOrUrl.trim()
        return when {
            trimmed.startsWith("http://") || trimmed.startsWith("https://") -> trimmed
            else -> "https://www.youtube.com/watch?v=$trimmed"
        }
    }

    /**
     * Resolves the best playable stream URL for a given YouTube video ID or URL.
     */
    suspend fun resolveAudioUrl(videoIdOrUrl: String, forceRefresh: Boolean = false): String? =
        withContext(Dispatchers.IO) {
            val canonicalUrl = toCanonicalUrl(videoIdOrUrl)
            initialize()

            if (!forceRefresh) {
                val cached = streamCache[canonicalUrl]
                if (cached != null) {
                    if (!cached.isExpired()) {
                        Log.d(TAG, "Using valid cached audio URL for: $canonicalUrl")
                        return@withContext cached.url
                    } else {
                        Log.d(TAG, "Cached audio URL expired for: $canonicalUrl")
                        streamCache.remove(canonicalUrl)
                    }
                }
            } else {
                streamCache.remove(canonicalUrl)
            }

            try {
                Log.d(TAG, "Extracting audio stream for: $canonicalUrl")

                val service = NewPipe.getServiceByUrl(canonicalUrl)
                val streamInfo = StreamInfo.getInfo(service, canonicalUrl)

                // 1. Try dedicated audio streams first
                val audioStreams = streamInfo.audioStreams.orEmpty()
                var selectedUrl: String? = null

                if (audioStreams.isNotEmpty()) {
                    // Prefer M4A streams for best Android Media3/ExoPlayer audio compatibility
                    val m4aStreams = audioStreams.filter { it.format == MediaFormat.M4A }
                    val stream = (m4aStreams.ifEmpty { audioStreams }).maxByOrNull { it.averageBitrate }
                    selectedUrl = stream?.content
                    if (!selectedUrl.isNullOrBlank()) {
                        Log.d(TAG, "Selected dedicated audio stream: format=${stream?.format}, bitrate=${stream?.averageBitrate}")
                    }
                }

                // 2. Fallback to progressive video streams (contains complete audio track) if audioStreams is empty
                if (selectedUrl.isNullOrBlank()) {
                    val videoStreams = streamInfo.videoStreams.orEmpty()
                    if (videoStreams.isNotEmpty()) {
                        val progressiveStream = selectBestProgressiveStream(videoStreams)
                        selectedUrl = progressiveStream?.content
                        Log.d(TAG, "Falling back to progressive stream with audio track: ${progressiveStream?.resolution}")
                    }
                }

                if (selectedUrl.isNullOrBlank()) {
                    Log.e(TAG, "No playable audio or progressive stream found for: $canonicalUrl")
                    return@withContext null
                }

                val expiresAtMs = parseExpirationTimestamp(selectedUrl)
                streamCache[canonicalUrl] = CachedStream(
                    url = selectedUrl,
                    timestamp = System.currentTimeMillis(),
                    expiresAtMs = expiresAtMs
                )

                Log.d(TAG, "Audio stream successfully extracted")
                return@withContext selectedUrl

            } catch (e: Exception) {
                Log.e(TAG, "Audio extraction failed for $canonicalUrl: ${e.message}", e)
                return@withContext null
            }
        }

    private fun selectBestProgressiveStream(streams: List<VideoStream>): VideoStream? {
        val mp4Streams = streams.filter { it.format == MediaFormat.MPEG_4 }
        val candidateStreams = if (mp4Streams.isNotEmpty()) mp4Streams else streams
        return candidateStreams.minByOrNull { stream ->
            val resNumber = stream.resolution.filter { it.isDigit() }.toIntOrNull() ?: 360
            abs(resNumber - 360)
        } ?: streams.firstOrNull()
    }

    private fun parseExpirationTimestamp(url: String): Long? {
        return try {
            val uri = Uri.parse(url)
            val expireParam = uri.getQueryParameter("expire")
            if (!expireParam.isNullOrBlank()) {
                // expire param is in seconds; leave 60s margin
                val expireSec = expireParam.toLong()
                (expireSec * 1000L) - 60_000L
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Evicts a URL from the cache, e.g. when ExoPlayer reports a playback failure.
     */
    fun invalidate(videoIdOrUrl: String) {
        val canonicalUrl = toCanonicalUrl(videoIdOrUrl)
        streamCache.remove(canonicalUrl)
        Log.d(TAG, "Invalidated cache for: $canonicalUrl")
    }

    fun clearCache() {
        streamCache.clear()
        Log.d(TAG, "Stream cache cleared completely")
    }
}
