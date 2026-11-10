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
import com.surya.meowsic.data.stream.YouTubeStreamResolver
import com.surya.meowsic.domain.Song
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.stream.StreamInfoItem

enum class RepeatMode {
    OFF,
    ALL,
    ONE
}

class SearchViewModel(
    application: Application
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

    private val _isBuffering = MutableStateFlow(false)
    val isBuffering: StateFlow<Boolean> = _isBuffering.asStateFlow()

    private val _repeatMode = MutableStateFlow(RepeatMode.OFF)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()

    private val _isShuffle = MutableStateFlow(false)
    val isShuffle: StateFlow<Boolean> = _isShuffle.asStateFlow()

    private val _queue = MutableStateFlow<List<Song>>(emptyList())
    val queue: StateFlow<List<Song>> = _queue.asStateFlow()

    private val _currentIndex = MutableStateFlow(-1)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    private val _isFullScreenOpen = MutableStateFlow(false)
    val isFullScreenOpen: StateFlow<Boolean> = _isFullScreenOpen.asStateFlow()

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
                    _isBuffering.value = false
                }
                Player.STATE_BUFFERING -> {
                    _isBuffering.value = true
                }
                Player.STATE_ENDED -> {
                    _isPlaying.value = false
                    _isBuffering.value = false
                    stopProgressTracker()
                    if (_repeatMode.value == RepeatMode.ONE) {
                        seekTo(0)
                        player.play()
                    } else {
                        playNext(auto = true)
                    }
                }
                Player.STATE_IDLE -> {
                    _isBuffering.value = false
                }
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            Log.e(TAG, "ExoPlayer playback error: ${error.errorCodeName}", error)
            _isResolving.value = false
            _isBuffering.value = false
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
    }

    /**
     * Executes a YouTube search using NewPipe's InnerTube extractor.
     * Called from SearchScreen with 500ms debounce via LaunchedEffect.
     */
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
            _isSearching.value = true
            _errorMessage.value = null

            try {
                Log.d(TAG, "Searching YouTube via NewPipe InnerTube: $trimmed")
                val songs = searchWithNewPipe(trimmed)
                _searchResults.value = songs
                Log.d(TAG, "Search completed: ${songs.size} songs found")
                if (songs.isEmpty()) {
                    _errorMessage.value = "No songs found for '$trimmed'"
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Search failed", e)
                _errorMessage.value = "Search failed: ${e.localizedMessage ?: e.message}"
                _searchResults.value = emptyList()
            } finally {
                _isSearching.value = false
            }
        }
    }

    private suspend fun searchWithNewPipe(query: String): List<Song> = withContext(Dispatchers.IO) {
        val extractor = ServiceList.YouTube.getSearchExtractor(query)
        extractor.fetchPage()
        val items = extractor.initialPage?.items ?: emptyList()
        items.filterIsInstance<StreamInfoItem>().mapNotNull { item ->
            try {
                val videoId = extractVideoId(item.url) ?: return@mapNotNull null
                val thumbnailUrl = item.thumbnails.firstOrNull()?.url ?: ""
                val durationStr = formatDuration(item.duration)
                Song(
                    id = videoId,
                    title = item.name ?: "Unknown",
                    artist = item.uploaderName ?: "Unknown",
                    thumbnailUrl = thumbnailUrl,
                    duration = durationStr
                )
            } catch (e: Exception) {
                Log.w(TAG, "Failed to map search result item: ${e.message}")
                null
            }
        }
    }

    private fun extractVideoId(url: String): String? {
        return try {
            val uri = Uri.parse(url)
            // Standard: https://www.youtube.com/watch?v=VIDEO_ID
            uri.getQueryParameter("v")
                // Short URL: https://youtu.be/VIDEO_ID
                ?: if (uri.host?.contains("youtu.be") == true) uri.pathSegments.firstOrNull() else null
                // Shorts: https://www.youtube.com/shorts/VIDEO_ID
                ?: if (url.contains("/shorts/")) uri.pathSegments.lastOrNull() else null
        } catch (e: Exception) {
            Log.w(TAG, "Failed to extract video ID from URL: $url")
            null
        }
    }

    private fun formatDuration(seconds: Long): String {
        if (seconds <= 0) return ""
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        return if (hours > 0) {
            "$hours:${String.format("%02d", minutes)}:${String.format("%02d", secs)}"
        } else {
            "$minutes:${String.format("%02d", secs)}"
        }
    }

    // ------------------------------------------------
    // PLAYBACK OPERATIONS
    // ------------------------------------------------

    fun openFullScreen() {
        _isFullScreenOpen.value = true
    }

    fun closeFullScreen() {
        _isFullScreenOpen.value = false
    }

    fun toggleShuffle() {
        _isShuffle.value = !_isShuffle.value
    }

    fun toggleRepeat() {
        _repeatMode.value = when (_repeatMode.value) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
    }

    fun playSong(song: Song, playlist: List<Song>? = null, isRetry: Boolean = false) {
        if (!isRetry) {
            hasRetriedCurrentSong = false
        }

        if (playlist != null && playlist.isNotEmpty()) {
            _queue.value = playlist
            val idx = playlist.indexOfFirst { it.id == song.id }
            _currentIndex.value = if (idx >= 0) idx else 0
        } else {
            val currentList = _queue.value
            val idx = currentList.indexOfFirst { it.id == song.id }
            if (idx >= 0) {
                _currentIndex.value = idx
            } else {
                _queue.value = currentList + song
                _currentIndex.value = _queue.value.lastIndex
            }
        }

        Log.d(TAG, "Playing song: ${song.title} (${song.id}) at index ${_currentIndex.value}")
        playbackJob?.cancel()

        playbackJob = viewModelScope.launch {
            _isResolving.value = true
            _errorMessage.value = null
            _currentSong.value = song
            _playbackPosition.value = 0L
            _duration.value = 0L

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

    fun playNext(auto: Boolean = false) {
        val currentList = _queue.value
        if (currentList.isEmpty()) {
            val current = _currentSong.value
            if (current != null) {
                seekTo(0)
                player.play()
            }
            return
        }

        if (_repeatMode.value == RepeatMode.ONE && auto) {
            seekTo(0)
            player.play()
            return
        }

        val currentIdx = _currentIndex.value
        val nextIdx = when {
            _isShuffle.value && currentList.size > 1 -> {
                val validIndices = currentList.indices.filter { it != currentIdx }
                if (validIndices.isNotEmpty()) validIndices.random() else currentIdx
            }
            currentIdx + 1 < currentList.size -> currentIdx + 1
            _repeatMode.value == RepeatMode.ALL -> 0
            else -> -1
        }

        if (nextIdx != -1 && nextIdx in currentList.indices) {
            _currentIndex.value = nextIdx
            playSong(currentList[nextIdx], playlist = currentList)
        } else {
            if (!auto) {
                _currentIndex.value = 0
                playSong(currentList[0], playlist = currentList)
            } else {
                _isPlaying.value = false
                stopProgressTracker()
            }
        }
    }

    fun playPrevious() {
        if (player.currentPosition > 3000L) {
            seekTo(0)
            player.play()
            return
        }

        val currentList = _queue.value
        if (currentList.isEmpty()) {
            seekTo(0)
            player.play()
            return
        }

        val currentIdx = _currentIndex.value
        val prevIdx = when {
            _isShuffle.value && currentList.size > 1 -> {
                val validIndices = currentList.indices.filter { it != currentIdx }
                if (validIndices.isNotEmpty()) validIndices.random() else currentIdx
            }
            currentIdx - 1 >= 0 -> currentIdx - 1
            _repeatMode.value == RepeatMode.ALL -> currentList.size - 1
            else -> 0
        }

        if (prevIdx in currentList.indices && prevIdx != currentIdx) {
            _currentIndex.value = prevIdx
            playSong(currentList[prevIdx], playlist = currentList)
        } else {
            seekTo(0)
            player.play()
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
