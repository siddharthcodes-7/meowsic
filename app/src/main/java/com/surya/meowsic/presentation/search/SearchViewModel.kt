package com.surya.meowsic.presentation.search

import android.app.Application
import android.util.Log

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope

import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

import com.surya.meowsic.BuildConfig
import com.surya.meowsic.data.stream.YouTubeStreamResolver
import com.surya.meowsic.domain.Song

import com.google.gson.annotations.SerializedName

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query


class SearchViewModel(
    application: Application
) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "MEOWSIC"

        private const val BASE_URL =
            "https://www.googleapis.com/youtube/v3/"
    }

    // ------------------------------------------------
    // RETROFIT API
    // ------------------------------------------------

    private interface YouTubeApiService {

        @GET("search")
        suspend fun searchSongs(
            @Query("part") part: String = "snippet",
            @Query("q") query: String,
            @Query("type") type: String = "video",
            @Query("maxResults") maxResults: Int = 25,
            @Query("key") apiKey: String
        ): YouTubeSearchResponse
    }

    // ------------------------------------------------
    // API RESPONSE MODELS
    // ------------------------------------------------

    private data class YouTubeSearchResponse(
        @SerializedName("items")
        val items: List<YouTubeItem>?
    )

    private data class YouTubeItem(
        @SerializedName("id")
        val id: YouTubeVideoId?,

        @SerializedName("snippet")
        val snippet: YouTubeSnippet?
    )

    private data class YouTubeVideoId(
        @SerializedName("videoId")
        val videoId: String?
    )

    private data class YouTubeSnippet(
        @SerializedName("title")
        val title: String?,

        @SerializedName("channelTitle")
        val channelTitle: String?,

        @SerializedName("thumbnails")
        val thumbnails: YouTubeThumbnails?
    )

    private data class YouTubeThumbnails(
        @SerializedName("high")
        val high: YouTubeThumbnail?,

        @SerializedName("medium")
        val medium: YouTubeThumbnail?,

        @SerializedName("default")
        val default: YouTubeThumbnail?
    )

    private data class YouTubeThumbnail(
        @SerializedName("url")
        val url: String?
    )

    // ------------------------------------------------
    // RETROFIT INITIALIZATION
    // ------------------------------------------------

    private val api: YouTubeApiService by lazy {

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .build()
            .create(YouTubeApiService::class.java)
    }

    private val apiKey = BuildConfig.YOUTUBE_API_KEY

    // ------------------------------------------------
    // EXOPLAYER
    // ------------------------------------------------

    val player: ExoPlayer =
        ExoPlayer.Builder(application).build()

    // ------------------------------------------------
    // SEARCH STATE
    // ------------------------------------------------

    private val _searchResults =
        MutableStateFlow<List<Song>>(emptyList())

    val searchResults: StateFlow<List<Song>> =
        _searchResults

    private val _isSearching =
        MutableStateFlow(false)

    val isSearching: StateFlow<Boolean> =
        _isSearching

    private val _isResolving =
        MutableStateFlow(false)

    val isResolving: StateFlow<Boolean> =
        _isResolving

    private val _errorMessage =
        MutableStateFlow<String?>(null)

    val errorMessage: StateFlow<String?> =
        _errorMessage

    private val _currentSong =
        MutableStateFlow<Song?>(null)

    val currentSong: StateFlow<Song?> =
        _currentSong

    private val _isPlaying =
        MutableStateFlow(false)

    val isPlaying: StateFlow<Boolean> =
        _isPlaying

    // ------------------------------------------------
    // COROUTINE JOBS
    // ------------------------------------------------

    private var searchJob: Job? = null

    private var playbackJob: Job? = null

    // ------------------------------------------------
    // PLAYER LISTENER
    // ------------------------------------------------

    private val playerListener =
        object : Player.Listener {

            override fun onIsPlayingChanged(
                isPlaying: Boolean
            ) {
                _isPlaying.value = isPlaying
            }

            override fun onPlayerError(
                error: PlaybackException
            ) {

                Log.e(
                    TAG,
                    "ExoPlayer playback error",
                    error
                )

                _errorMessage.value =
                    "Playback failed: ${error.message}"

                _isResolving.value = false
            }
        }

    init {

        player.addListener(playerListener)

        Log.d(TAG, "SearchViewModel initialized")

    }

    // ------------------------------------------------
    // YOUTUBE SEARCH
    // ------------------------------------------------

    fun searchYouTube(query: String) {

        searchJob?.cancel()

        val searchText = query.trim()

        if (searchText.length < 3) {

            _searchResults.value = emptyList()

            _isSearching.value = false

            return
        }

        searchJob = viewModelScope.launch {

            delay(400)

            _isSearching.value = true

            _errorMessage.value = null

            try {

                Log.d(
                    TAG,
                    "Searching YouTube: $searchText"
                )

                if (apiKey.isBlank()) {

                    throw IllegalStateException(
                        "YouTube API key is missing"
                    )
                }

                val response = api.searchSongs(
                    query = searchText,
                    apiKey = apiKey
                )

                val songs = response.items
                    .orEmpty()
                    .mapNotNull { item ->

                        val videoId =
                            item.id?.videoId

                        val snippet =
                            item.snippet

                        if (
                            videoId.isNullOrBlank() ||
                            snippet == null
                        ) {
                            return@mapNotNull null
                        }

                        val thumbnail =
                            snippet.thumbnails?.high?.url
                                ?: snippet.thumbnails?.medium?.url
                                ?: snippet.thumbnails?.default?.url
                                ?: ""

                        Song(
                            id = videoId,

                            title = snippet.title
                                ?: "Unknown title",

                            artist = snippet.channelTitle
                                ?: "Unknown artist",

                            thumbnailUrl = thumbnail,

                            duration = ""
                        )
                    }

                _searchResults.value = songs

                Log.d(
                    TAG,
                    "Search completed: ${songs.size} songs found"
                )

                if (songs.isEmpty()) {

                    _errorMessage.value =
                        "No songs found"
                }

            } catch (e: CancellationException) {

                throw e

            } catch (e: Exception) {

                Log.e(
                    TAG,
                    "YouTube search failed",
                    e
                )

                _errorMessage.value =
                    "Search failed: ${e.message}"

            } finally {

                _isSearching.value = false
            }
        }
    }

    // ------------------------------------------------
    // PLAY SONG
    // ------------------------------------------------

    fun playSong(song: Song) {

        Log.d(
            TAG,
            "Song selected: ${song.title}"
        )

        // Cancel previous extraction

        playbackJob?.cancel()

        // Start new playback

        playbackJob = viewModelScope.launch {

            resolveAndPlay(song)
        }
    }

    // ------------------------------------------------
    // NEWPIPE AUDIO EXTRACTION
    // ------------------------------------------------

    private suspend fun resolveAndPlay(
        song: Song
    ) {

        try {

            _isResolving.value = true

            _errorMessage.value = null

            _currentSong.value = song

            Log.d(
                TAG,
                "Extracting audio for ${song.title}"
            )

            // Extract actual audio URL

            val audioUrl =
                YouTubeStreamResolver.resolveAudioUrl(
                    song.id
                )

            if (audioUrl.isNullOrBlank()) {

                throw IllegalStateException(
                    "NewPipe could not extract audio URL"
                )
            }

            Log.d(
                TAG,
                "Audio URL extracted successfully"
            )

            // Create media metadata

            val metadata = MediaMetadata.Builder()
                .setTitle(song.title)
                .setArtist(song.artist)
                .build()

            // Create MediaItem

            val mediaItem = MediaItem.Builder()
                .setUri(audioUrl)
                .setMediaMetadata(metadata)
                .build()

            // Send audio to ExoPlayer

            player.setMediaItem(mediaItem)

            player.prepare()

            player.play()

            Log.d(
                TAG,
                "ExoPlayer started: ${song.title}"
            )

        } catch (e: CancellationException) {

            throw e

        } catch (e: Exception) {

            Log.e(
                TAG,
                "Playback failed",
                e
            )

            _errorMessage.value =
                "Unable to play ${song.title}: ${e.message}"

        } finally {

            _isResolving.value = false
        }
    }

    // ------------------------------------------------
    // PLAY / PAUSE
    // ------------------------------------------------

    fun togglePlayPause() {

        if (player.isPlaying) {

            player.pause()

        } else {

            player.play()
        }
    }

    // ------------------------------------------------
    // STOP
    // ------------------------------------------------

    fun stopPlayback() {

        playbackJob?.cancel()

        player.stop()

        _isResolving.value = false

        _isPlaying.value = false
    }

    // ------------------------------------------------
    // CLEAR ERROR
    // ------------------------------------------------

    fun clearError() {

        _errorMessage.value = null
    }

    // ------------------------------------------------
    // CLEANUP
    // ------------------------------------------------

    override fun onCleared() {

        searchJob?.cancel()

        playbackJob?.cancel()

        player.removeListener(playerListener)

        player.release()

        Log.d(
            TAG,
            "SearchViewModel released"
        )

        super.onCleared()
    }
}