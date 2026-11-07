package com.surya.meowsic.presentation.search

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.surya.meowsic.data.repository.YouTubeRepository
import com.surya.meowsic.data.stream.YouTubeStreamResolver
import com.surya.meowsic.domain.Song
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class SearchViewModel @JvmOverloads constructor(
    application: Application,
    private val repository: YouTubeRepository = YouTubeRepository()
) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "SearchViewModel"
        private const val USER_AGENT = "MeowMusic/1.0 (Android)"
    }

    // ------------------------------------------------
    // EXOPLAYER INITIALIZATION
    // ------------------------------------------------

    private val httpDataSourceFactory = DefaultHttpDataSource.Factory()
        .setUserAgent(USER_AGENT)
        .setConnectTimeoutMs(20_000)
        .setReadTimeoutMs(20_000)
        .setAllowCrossProtocolRedirects(true)

    val player: ExoPlayer = ExoPlayer.Builder(application)
        .setMediaSourceFactory(DefaultMediaSourceFactory(httpDataSourceFactory))
        .build()

    // ------------------------------------------------
    // OBSERVABLE UI STATE
    // ------------------------------------------------

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Song>>(emptyList())
    val searchResults: StateFlow<List<Song>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _isResolving = MutableStateFlow(false)
    val isResolving: StateFlow<Boolean> = _isResolving.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _playbackPosition = MutableStateFlow(0L)
    val playbackPosition: StateFlow<Long> = _playbackPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    // ------------------------------------------------
    // INTERNAL COROUTINE JOBS
    // ------------------------------------------------

    private var searchJob: Job? = null
    private var playbackJob: Job? = null
    private var progressJob: Job? = null
    private var hasRetriedCurrentSong = false

    // ------------------------------------------------
    // PLAYER LISTENER
    // ------------------------------------------------

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.value = isPlaying
            if (isPlaying) {
                startProgressTracker()
            } else {
                stopProgressTracker()
            }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_READY -> {
                    _duration.value = player.duration.coerceAtLeast(0L)
                    _isResolving.value = false
                }
                Player.STATE_ENDED -> {
                    _isPlaying.value = false
                    stopProgressTracker()
                }
                Player.STATE_BUFFERING -> {
                    // Buffering can happen during playback
                }
                Player.STATE_IDLE -> {
                    // Idle state
                }
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            Log.e(TAG, "ExoPlayer playback error: ${error.errorCodeName}", error)
            _isResolving.value = false
            _isPlaying.value = false

            val failedSong = _currentSong.value
            if (failedSong != null && !hasRetriedCurrentSong) {
                hasRetriedCurrentSong = true
                Log.w(TAG, "Stream playback failed. Invalidating cache and retrying extraction for: ${failedSong.title}")
                YouTubeStreamResolver.invalidate(failedSong.id)
                playSong(failedSong, isRetry = true)
                return
            }

            _errorMessage.value = "Playback failed: ${error.localizedMessage ?: error.message}"
        }
    }

    init {
        YouTubeStreamResolver.initialize()
        player.addListener(playerListener)
        Log.d(TAG, "SearchViewModel initialized")
    }

    // ------------------------------------------------
    // SEARCH OPERATION
    // ------------------------------------------------

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
        searchYouTube(newQuery)
    }

    fun searchYouTube(query: String) {
        searchJob?.cancel()

        val trimmed = query.trim()
        if (trimmed.length < 2) {
            _searchResults.value = emptyList()
            _isSearching.value = false
            _errorMessage.value = null
            return
        }

        searchJob = viewModelScope.launch {
            // Debounce to prevent unnecessary network requests while typing
            delay(400)

            _isSearching.value = true
            _errorMessage.value = null

            try {
                Log.d(TAG, "Searching YouTube: $trimmed")
                val result = repository.searchSongs(trimmed)

                result.onSuccess { songs ->
                    _searchResults.value = songs
                    Log.d(TAG, "Search completed: ${songs.size} songs found")
                    if (songs.isEmpty()) {
                        _errorMessage.value = "No songs found for '$trimmed'"
                    }
                }.onFailure { throwable ->
                    Log.e(TAG, "Search failed", throwable)
                    _errorMessage.value = throwable.localizedMessage ?: "Search failed"
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error in search", e)
                _errorMessage.value = "Search error: ${e.message}"
            } finally {
                _isSearching.value = false
            }
        }
    }

    // ------------------------------------------------
    // PLAYBACK OPERATIONS
    // ------------------------------------------------

    fun playSong(song: Song, isRetry: Boolean = false) {
        if (!isRetry) {
            hasRetriedCurrentSong = false
        }

        Log.d(TAG, "Playing song: ${song.title} (${song.id})")
        playbackJob?.cancel()

        playbackJob = viewModelScope.launch {
            _isResolving.value = true
            _errorMessage.value = null
            _currentSong.value = song

            try {
                val audioUrl = YouTubeStreamResolver.resolveAudioUrl(
                    videoIdOrUrl = song.id,
                    forceRefresh = isRetry
                )

                if (audioUrl.isNullOrBlank()) {
                    throw IllegalStateException("Unable to extract playable audio stream for ${song.title}")
                }

                Log.d(TAG, "Extracted audio stream URL successfully. Preparing ExoPlayer.")

                val metadata = MediaMetadata.Builder()
                    .setTitle(song.title)
                    .setArtist(song.artist)
                    .setArtworkUri(if (song.thumbnailUrl.isNotBlank()) Uri.parse(song.thumbnailUrl) else null)
                    .build()

                val mediaItem = MediaItem.Builder()
                    .setUri(audioUrl)
                    .setMediaId(song.id)
                    .setMediaMetadata(metadata)
                    .build()

                player.setMediaItem(mediaItem)
                player.prepare()
                player.play()

                Log.d(TAG, "ExoPlayer playback initiated")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Failed to resolve and play song", e)
                _errorMessage.value = "Cannot play ${song.title}: ${e.localizedMessage ?: e.message}"
                _isResolving.value = false
            }
        }
    }

    fun togglePlayPause() {
        if (player.isPlaying) {
            player.pause()
        } else {
            if (player.playbackState == Player.STATE_IDLE && _currentSong.value != null) {
                playSong(_currentSong.value!!)
            } else {
                player.play()
            }
        }
    }

    fun seekTo(positionMs: Long) {
        player.seekTo(positionMs)
        _playbackPosition.value = positionMs
    }

    fun stopPlayback() {
        playbackJob?.cancel()
        stopProgressTracker()
        player.stop()
        _isResolving.value = false
        _isPlaying.value = false
    }

    fun clearError() {
        _errorMessage.value = null
    }

    private fun startProgressTracker() {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            while (isActive) {
                _playbackPosition.value = player.currentPosition.coerceAtLeast(0L)
                _duration.value = player.duration.coerceAtLeast(0L)
                delay(500)
            }
        }
    }

    private fun stopProgressTracker() {
        progressJob?.cancel()
        progressJob = null
    }

    override fun onCleared() {
        searchJob?.cancel()
        playbackJob?.cancel()
        stopProgressTracker()
        player.removeListener(playerListener)
        player.release()
        Log.d(TAG, "SearchViewModel cleared and ExoPlayer released")
        super.onCleared()
    }
}
