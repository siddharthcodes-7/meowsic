package com.surya.meomusic.data.stream

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.stream.StreamInfo
import org.schabi.newpipe.extractor.MediaFormat

object YouTubeStreamResolver {

    @Volatile
    private var initialized = false

    private fun initialize() {
        if (!initialized) {
            synchronized(this) {
                if (!initialized) {
                    NewPipe.init(NewPipeDownloader())
                    initialized = true
                }
            }
        }
    }

    suspend fun resolveAudioUrl(videoId: String): String =
        withContext(Dispatchers.IO) {

            initialize()

            val videoUrl =
                "https://www.youtube.com/watch?v=$videoId"

            val streamInfo = StreamInfo.getInfo(videoUrl)

            val audioStreams = streamInfo.audioStreams
                .filter { it.isUrl }

            val selectedStream = audioStreams
                .filter {
                    it.format == MediaFormat.M4A
                }
                .maxByOrNull { it.averageBitrate }
                ?: audioStreams.maxByOrNull {
                    it.averageBitrate
                }

            selectedStream?.content
                ?: throw IllegalStateException(
                    "No playable audio stream found"
                )
        }
}