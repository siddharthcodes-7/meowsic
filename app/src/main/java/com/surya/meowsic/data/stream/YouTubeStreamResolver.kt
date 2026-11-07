package com.surya.meowsic.data.stream

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.MediaFormat
import org.schabi.newpipe.extractor.stream.StreamInfo
import java.util.concurrent.ConcurrentHashMap

object YouTubeStreamResolver {

    private const val TAG = "MEOWSIC"

    private const val CACHE_DURATION =
        30 * 60 * 1000L

    private data class CachedStream(
        val url: String,
        val timestamp: Long
    )

    private val streamCache =
        ConcurrentHashMap<String, CachedStream>()

    @Volatile
    private var initialized = false

    private fun initialize() {

        if (initialized) return

        synchronized(this) {

            if (initialized) return

            NewPipe.init(NewPipeDownloader())

            initialized = true

            Log.d(TAG, "NewPipe initialized successfully")
        }
    }

    suspend fun resolveAudioUrl(
        videoUrl: String
    ): String? = withContext(Dispatchers.IO) {

        try {

            initialize()

            // Check cache first

            val cached = streamCache[videoUrl]

            if (cached != null) {

                val age =
                    System.currentTimeMillis() - cached.timestamp

                if (age < CACHE_DURATION) {

                    Log.d(TAG, "Using cached audio URL")

                    return@withContext cached.url
                }

                streamCache.remove(videoUrl)
            }

            Log.d(TAG, "Extracting audio: $videoUrl")

            // Get YouTube service

            val service = NewPipe.getServiceByUrl(videoUrl)

            // Extract stream information

            val streamInfo = StreamInfo.getInfo(
                service,
                videoUrl
            )

            val audioStreams = streamInfo.audioStreams

            if (audioStreams.isEmpty()) {

                Log.e(TAG, "No audio streams found")

                return@withContext null
            }

            // Prefer M4A audio streams

            val m4aStreams = audioStreams.filter {
                it.format == MediaFormat.M4A
            }

            val selectedStream = (
                    if (m4aStreams.isNotEmpty()) {
                        m4aStreams
                    } else {
                        audioStreams
                    }
                    ).maxByOrNull {
                    it.averageBitrate
                }

            if (selectedStream == null) {

                Log.e(TAG, "Unable to select audio stream")

                return@withContext null
            }

            val audioUrl = selectedStream.content

            if (audioUrl.isBlank()) {

                Log.e(TAG, "Extracted audio URL is empty")

                return@withContext null
            }

            // Save URL in cache

            streamCache[videoUrl] = CachedStream(
                url = audioUrl,
                timestamp = System.currentTimeMillis()
            )

            Log.d(TAG, "Audio URL extracted successfully")

            Log.d(TAG, "Format: ${selectedStream.format}")

            Log.d(TAG, "Bitrate: ${selectedStream.averageBitrate}")

            return@withContext audioUrl

        } catch (e: Exception) {

            Log.e(
                TAG,
                "Audio extraction failed",
                e
            )

            return@withContext null
        }
    }

    fun clearCache() {

        streamCache.clear()

        Log.d(TAG, "Stream cache cleared")
    }
}