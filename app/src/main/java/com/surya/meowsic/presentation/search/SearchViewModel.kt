package com.surya.meowsic.presentation.search

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

import com.surya.meowsic.BuildConfig
import com.surya.meowsic.data.remote.YouTubeApiService
import com.surya.meowsic.data.stream.YouTubeStreamResolver
import com.surya.meowsic.domain.Song

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SearchViewModel(application: Application) :
    AndroidViewModel(application) {

    private val _searchResults =
        MutableStateFlow<List<Song>>(emptyList())

    val searchResults: StateFlow<List<Song>> =
        _searchResults

    private val _isResolving =
        MutableStateFlow(false)

    val isResolving: StateFlow<Boolean> =
        _isResolving

    private val _playbackError =
        MutableStateFlow<String?>(null)

    val playbackError: StateFlow<String?> =
        _playbackError

    private val exoPlayer =
        ExoPlayer.Builder(application).build()

    private val api = Retrofit.Builder()
        .baseUrl("https://www.googleapis.com/youtube/v3/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(YouTubeApiService::class.java)

    private val apiKey = BuildConfig.YOUTUBE_API_KEY

    private var playbackJob: Job? = null

    private var currentSong: Song? = null

    private var retryCount = 0

    init {
        exoPlayer.addListener(object : Player.Listener {

            override fun onPlayerError(
                error: PlaybackException
            ) {
                val song = currentSong ?: return

                if (retryCount < 1) {
                    retryCount++

                    playbackJob?.cancel()

                    playbackJob = viewModelScope.launch {
                        resolveAndPlay(song)
                    }

                } else {
                    _playbackError.value =
                        "Playback failed: ${error.message}"
                }
            }
        })
    }

    fun searchYouTube(query: String) {

        if (query.isBlank()) return

        viewModelScope.launch {

            try {

                val response = api.searchSongs(
                    query = query,
                    apiKey = apiKey
                )

                val songs = response.items.mapNotNull { item ->

                    item.id.videoId?.let { videoId ->

                        Song(
                            id = videoId,
                            title = item.snippet.title,
                            artist = item.snippet.channelTitle,
                            thumbnailUrl =
                                item.snippet.thumbnails.high.url,
                            duration = ""
                        )
                    }
                }

                _searchResults.value = songs

            } catch (e: Exception) {

                e.printStackTrace()

            }
        }
    }

    fun playSong(song: Song) {

        playbackJob?.cancel()

        currentSong = song
        retryCount = 0

        _playbackError.value = null

        playbackJob = viewModelScope.launch {

            resolveAndPlay(song)

        }
    }

    private suspend fun resolveAndPlay(song: Song) {

        _isResolving.value = true
        _playbackError.value = null

        try {

            println(
                "MEOWSIC: Extracting audio for ${song.title}"
            )

            val audioUrl =
                YouTubeStreamResolver.resolveAudioUrl(song.id)

            println(
                "MEOWSIC: Audio URL successfully extracted"
            )

            val metadata = MediaMetadata.Builder()
                .setTitle(song.title)
                .setArtist(song.artist)
                .setArtworkUri(Uri.parse(song.thumbnailUrl))
                .build()

            val mediaItem = MediaItem.Builder()
                .setUri(audioUrl)
                .setMediaMetadata(metadata)
                .build()

            exoPlayer.setMediaItem(mediaItem)

            exoPlayer.prepare()

            exoPlayer.play()

            println("MEOWSIC: Playback started")

        } catch (e: CancellationException) {

            throw e

        } catch (e: Exception) {

            e.printStackTrace()

            _playbackError.value =
                "Unable to extract audio: ${e.message}"

        } finally {

            _isResolving.value = false

        }
    }

    override fun onCleared() {

        playbackJob?.cancel()

        exoPlayer.release()

        super.onCleared()
    }
}