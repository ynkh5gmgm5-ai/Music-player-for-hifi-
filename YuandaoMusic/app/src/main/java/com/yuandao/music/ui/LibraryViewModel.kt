package com.yuandao.music.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yuandao.music.core.AppContainer
import com.yuandao.music.data.model.Album
import com.yuandao.music.data.model.Artist
import com.yuandao.music.data.model.LibraryRoot
import com.yuandao.music.data.model.Track
import com.yuandao.music.data.repository.MusicRepository
import com.yuandao.music.lyrics.LyricsRepository
import com.yuandao.music.lyrics.TimedLyrics
import com.yuandao.music.playback.OutputDevice
import com.yuandao.music.playback.OutputDeviceManager
import com.yuandao.music.playback.PlaybackGateway
import com.yuandao.music.playback.PlaybackUiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class LibraryViewModel(
    private val musicRepository: MusicRepository,
    private val playbackController: PlaybackGateway,
    private val lyricsRepository: LyricsRepository,
    private val outputDeviceManager: OutputDeviceManager,
) : ViewModel() {
    val tracks: StateFlow<List<Track>> =
        musicRepository.tracks.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val albums: StateFlow<List<Album>> =
        musicRepository.albums.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val artists: StateFlow<List<Artist>> =
        musicRepository.artists.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentlyPlayedTracks: StateFlow<List<Track>> =
        musicRepository.recentlyPlayedTracks.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val safRoots: StateFlow<List<LibraryRoot>> =
        musicRepository.safRoots.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val playbackState: StateFlow<PlaybackUiState> = playbackController.state

    val lyrics: StateFlow<TimedLyrics?> =
        playbackController.state
            .mapLatest { state -> lyricsRepository.loadLyrics(state.currentTrack) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val outputDevices: StateFlow<List<OutputDevice>> = outputDeviceManager.devices

    private val _scanState = MutableStateFlow(ScanUiState())
    val scanState: StateFlow<ScanUiState> = _scanState

    init {
        viewModelScope.launch {
            tracks.collect { availableTracks ->
                playbackController.restoreIfPossible(availableTracks)
            }
        }
    }

    fun scanMediaStore() {
        viewModelScope.launch {
            _scanState.value = ScanUiState(scanning = true, message = "Scanning local library")
            runCatching { musicRepository.scanMediaStore() }
                .onSuccess { summary ->
                    _scanState.value = ScanUiState(
                        scanning = false,
                        message = "Indexed ${summary.scannedTracks} local tracks",
                    )
                }
                .onFailure { error ->
                    _scanState.value = ScanUiState(
                        scanning = false,
                        message = error.message ?: "Scan failed",
                    )
                }
        }
    }

    fun scanSafFolder(uri: Uri, displayName: String? = null) {
        viewModelScope.launch {
            _scanState.value = ScanUiState(scanning = true, message = "Scanning selected folder")
            runCatching {
                musicRepository.addSafRoot(uri, displayName)
                musicRepository.rescanSafRoots()
            }
                .onSuccess { summary ->
                    _scanState.value = ScanUiState(
                        scanning = false,
                        message = "Indexed ${summary.scannedTracks} folder tracks",
                    )
                }
                .onFailure { error ->
                    _scanState.value = ScanUiState(
                        scanning = false,
                        message = error.message ?: "Folder scan failed",
                    )
                }
        }
    }

    fun rescanSafRoots() {
        viewModelScope.launch {
            _scanState.value = ScanUiState(scanning = true, message = "Rescanning saved folders")
            runCatching { musicRepository.rescanSafRoots() }
                .onSuccess { summary ->
                    _scanState.value = ScanUiState(
                        scanning = false,
                        message = "Indexed ${summary.scannedTracks} folder tracks",
                    )
                }
                .onFailure { error ->
                    _scanState.value = ScanUiState(
                        scanning = false,
                        message = error.message ?: "Folder rescan failed",
                    )
                }
        }
    }

    fun playAll(startTrack: Track? = null) {
        val queue = tracks.value
        val startIndex = startTrack?.let { track -> queue.indexOfFirst { it.id == track.id } } ?: 0
        playbackController.playQueue(queue, startIndex.coerceAtLeast(0))
    }

    fun togglePlayPause() = playbackController.togglePlayPause()
    fun next() = playbackController.next()
    fun previous() = playbackController.previous()
    fun seekTo(positionMs: Long) = playbackController.seekTo(positionMs)
    fun cycleRepeatMode() = playbackController.cycleRepeatMode()
    fun toggleShuffle() = playbackController.toggleShuffle()
    fun stopPlayback() = playbackController.stopPlayback()

    fun refreshOutputs() = outputDeviceManager.refresh()

    override fun onCleared() {
        outputDeviceManager.release()
        super.onCleared()
    }

    class Factory(private val container: AppContainer) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            LibraryViewModel(
                musicRepository = container.musicRepository,
                playbackController = container.playbackController,
                lyricsRepository = container.lyricsRepository,
                outputDeviceManager = container.outputDeviceManager,
            ) as T
    }
}

data class ScanUiState(
    val scanning: Boolean = false,
    val message: String? = null,
)
