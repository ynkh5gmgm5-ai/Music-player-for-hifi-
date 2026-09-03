package com.yuandao.music.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yuandao.music.ui.screens.HomeScreen

@Composable
fun YuandaoMusicApp(
    viewModel: LibraryViewModel,
    onRequestScan: () -> Unit,
    onRequestSafFolder: () -> Unit,
) {
    HomeScreen(
        tracks = viewModel.tracks.collectAsStateWithLifecycle().value,
        albums = viewModel.albums.collectAsStateWithLifecycle().value,
        artists = viewModel.artists.collectAsStateWithLifecycle().value,
        recentlyPlayedTracks = viewModel.recentlyPlayedTracks.collectAsStateWithLifecycle().value,
        playbackState = viewModel.playbackState.collectAsStateWithLifecycle().value,
        lyrics = viewModel.lyrics.collectAsStateWithLifecycle().value,
        scanState = viewModel.scanState.collectAsStateWithLifecycle().value,
        safRootCount = viewModel.safRoots.collectAsStateWithLifecycle().value.size,
        outputDevices = viewModel.outputDevices.collectAsStateWithLifecycle().value,
        searchQuery = viewModel.searchQuery.collectAsStateWithLifecycle().value,
        searchResults = viewModel.searchResults.collectAsStateWithLifecycle().value,
        onRequestScan = onRequestScan,
        onRequestSafFolder = onRequestSafFolder,
        onSearchQueryChange = viewModel::updateSearchQuery,
        onClearSearch = viewModel::clearSearch,
        onRescanSafFolders = viewModel::rescanSafRoots,
        onPlayTrack = { track -> viewModel.playAll(track) },
        onPlayPause = viewModel::togglePlayPause,
        onNext = viewModel::next,
        onPrevious = viewModel::previous,
        onSeek = viewModel::seekTo,
        onShuffle = viewModel::toggleShuffle,
        onRepeat = viewModel::cycleRepeatMode,
        onStopPlayback = viewModel::stopPlayback,
        onPlayQueueTrack = viewModel::playQueueTrack,
        onRemoveQueueTrack = viewModel::removeQueueTrack,
        onClearQueue = viewModel::clearQueue,
        onRefreshOutputs = viewModel::refreshOutputs,
    )
}
