package com.yuandao.music.ui.screens

import android.graphics.BitmapFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yuandao.music.data.model.Album as MusicAlbum
import com.yuandao.music.data.model.Artist
import com.yuandao.music.data.model.AudioFormat
import com.yuandao.music.data.model.Track
import com.yuandao.music.lyrics.TimedLyrics
import com.yuandao.music.playback.OutputDevice
import com.yuandao.music.playback.PlaybackRepeatMode
import com.yuandao.music.playback.PlaybackQueueDrawerItem
import com.yuandao.music.playback.PlaybackQueueDrawerState
import com.yuandao.music.playback.PlaybackUiState
import com.yuandao.music.ui.ScanUiState
import com.yuandao.music.ui.theme.YuandaoBlue
import com.yuandao.music.ui.theme.YuandaoCyan
import com.yuandao.music.ui.theme.YuandaoGreen
import com.yuandao.music.ui.theme.YuandaoOrange
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.yuandao.music.ui.theme.YuandaoSurfaceHigh

@Composable
fun HomeScreen(
    tracks: List<Track>,
    albums: List<MusicAlbum>,
    artists: List<Artist>,
    recentlyPlayedTracks: List<Track>,
    playbackState: PlaybackUiState,
    lyrics: TimedLyrics?,
    scanState: ScanUiState,
    safRootCount: Int,
    outputDevices: List<OutputDevice>,
    onRequestScan: () -> Unit,
    onRequestSafFolder: () -> Unit,
    onRescanSafFolders: () -> Unit,
    onPlayTrack: (Track) -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Long) -> Unit,
    onShuffle: () -> Unit,
    onRepeat: () -> Unit,
    onStopPlayback: () -> Unit,
    onRefreshOutputs: () -> Unit,
) {
    var showQueueDrawer by remember { mutableStateOf(false) }
    var showNowPlaying by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF182435), Color(0xFF05070A)),
                    radius = 1100f,
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            item {
                Spacer(Modifier.height(18.dp))
                TopSegment()
                Spacer(Modifier.height(24.dp))
                Header(onRequestScan, onRequestSafFolder)
            }

            item {
                val firstPlayableTrack = tracks.firstOrNull { it.format.isFirstPassPlayable }
                ContinueCard(
                    playbackState = playbackState,
                    fallbackTrack = firstPlayableTrack ?: tracks.firstOrNull(),
                    lyrics = lyrics,
                    onPlayFallback = { (firstPlayableTrack ?: tracks.firstOrNull())?.let(onPlayTrack) },
                    onPlayPause = onPlayPause,
                    onPrevious = onPrevious,
                    onNext = onNext,
                    onSeek = onSeek,
                    onShuffle = onShuffle,
                    onRepeat = onRepeat,
                )
            }

            if (recentlyPlayedTracks.isNotEmpty()) {
                item {
                    RecentTracks(
                        title = "最近播放",
                        detail = "${recentlyPlayedTracks.size} 首",
                        tracks = recentlyPlayedTracks,
                        onPlayTrack = onPlayTrack,
                    )
                }
            }

            item {
                RecentTracks(
                    title = "最近添加",
                    detail = "${tracks.size} 首",
                    tracks = tracks,
                    onPlayTrack = onPlayTrack,
                )
            }

            item {
                HiResFormats(tracks = tracks)
            }

            item {
                LibraryOverview(
                    tracks = tracks,
                    albums = albums,
                    artists = artists,
                )
            }

            item {
                OutputCard(
                    outputDevices = outputDevices,
                    playbackState = playbackState,
                    onRefreshOutputs = onRefreshOutputs,
                )
            }

            item {
                ScanStatus(
                    scanState = scanState,
                    onRequestScan = onRequestScan,
                )
                SafRootScanControls(
                    safRootCount = safRootCount,
                    scanning = scanState.scanning,
                    onRescanSafFolders = onRescanSafFolders,
                )
                Spacer(Modifier.height(86.dp))
            }
        }

        MiniPlayerBar(
            playbackState = playbackState,
            onPlayPause = onPlayPause,
            onPrevious = onPrevious,
            onNext = onNext,
            onShuffle = onShuffle,
            onRepeat = onRepeat,
            onShowQueue = { showQueueDrawer = true },
            onStopPlayback = onStopPlayback,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .clickable { showNowPlaying = true },
        )

        if (showNowPlaying) {
            NowPlayingSheet(
                state = NowPlayingScreenStateProjector.project(playbackState, lyrics),
                onDismiss = { showNowPlaying = false },
                onPlayPause = onPlayPause,
                onPrevious = onPrevious,
                onNext = onNext,
                onSeek = onSeek,
                onShuffle = onShuffle,
                onRepeat = onRepeat,
                onStopPlayback = {
                    onStopPlayback()
                    showNowPlaying = false
                },
                onShowQueue = {
                    showNowPlaying = false
                    showQueueDrawer = true
                },
            )
        }

        if (showQueueDrawer) {
            QueueDrawer(
                state = playbackState.queueDrawerState,
                onDismiss = { showQueueDrawer = false },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NowPlayingSheet(
    state: NowPlayingScreenState,
    onDismiss: () -> Unit,
    onPlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSeek: (Long) -> Unit,
    onShuffle: () -> Unit,
    onRepeat: () -> Unit,
    onStopPlayback: () -> Unit,
    onShowQueue: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxHeight(0.96f),
        containerColor = Color(0xFF090D12),
        contentColor = Color.White,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text("正在播放", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                    Text(state.queueLabel, color = Color.White.copy(alpha = 0.50f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                TextButton(onClick = onDismiss) {
                    Text("关闭")
                }
            }

            val track = state.track
            if (!state.hasTrack || track == null) {
                GlassPanel {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(state.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text(state.lyricLine, color = Color.White.copy(alpha = 0.58f))
                    }
                }
                return@Column
            }

            CoverArtwork(
                track = track,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp),
            )

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = state.title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(state.artistName, color = Color.White.copy(alpha = 0.70f), style = MaterialTheme.typography.titleMedium)
                Text(state.albumTitle, color = Color.White.copy(alpha = 0.48f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Pill(state.sourceQualityLabel)
                if (state.runtimeQualityLabel.isNotBlank()) {
                    Pill(state.runtimeQualityLabel)
                }
            }

            LinearProgressIndicator(
                progress = { state.progress },
                modifier = Modifier.fillMaxWidth(),
                color = YuandaoGreen,
                trackColor = Color.White.copy(alpha = 0.12f),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(state.positionLabel, color = Color.White.copy(alpha = 0.64f))
                Text(state.durationLabel, color = Color.White.copy(alpha = 0.64f))
            }

            val sliderMax = state.durationMs.coerceAtLeast(1L).toFloat()
            var isSeeking by remember(track.id) { mutableStateOf(false) }
            var pendingSeekPosition by remember(track.id) {
                mutableFloatStateOf(state.positionMs.toFloat().coerceIn(0f, sliderMax))
            }
            LaunchedEffect(track.id, state.positionMs, sliderMax, isSeeking) {
                if (!isSeeking) {
                    pendingSeekPosition = state.positionMs.toFloat().coerceIn(0f, sliderMax)
                }
            }
            Slider(
                value = pendingSeekPosition,
                onValueChange = {
                    isSeeking = true
                    pendingSeekPosition = it
                },
                onValueChangeFinished = {
                    isSeeking = false
                    onSeek(pendingSeekPosition.toLong())
                },
                valueRange = 0f..sliderMax,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onShuffle) {
                    Icon(Icons.Rounded.Shuffle, contentDescription = "Shuffle")
                }
                IconButton(onClick = onPrevious) {
                    Icon(Icons.Rounded.SkipPrevious, contentDescription = "Previous")
                }
                IconButton(
                    onClick = onPlayPause,
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color.Black.copy(alpha = 0.48f), CircleShape),
                ) {
                    Icon(
                        imageVector = if (state.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = "Play or pause",
                        tint = YuandaoGreen,
                        modifier = Modifier.size(38.dp),
                    )
                }
                IconButton(onClick = onNext) {
                    Icon(Icons.Rounded.SkipNext, contentDescription = "Next")
                }
                IconButton(onClick = onRepeat) {
                    Icon(Icons.Rounded.Repeat, contentDescription = "Repeat")
                }
            }

            GlassPanel {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("歌词", color = Color.White.copy(alpha = 0.54f), style = MaterialTheme.typography.labelLarge)
                    Text(
                        text = state.lyricLine,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.82f),
                    )
                }
            }

            state.errorMessage?.let { message ->
                Text(message, color = YuandaoOrange, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = onShowQueue, modifier = Modifier.weight(1f)) {
                    Text("查看队列")
                }
                OutlinedButton(onClick = onStopPlayback, modifier = Modifier.weight(1f)) {
                    Text("关闭播放")
                }
            }
        }
    }
}

@Composable
private fun TopSegment() {
    GlassPanel(shape = RoundedCornerShape(28.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SegmentLabel("私有云", selected = false)
            SegmentLabel("流媒体", selected = false)
            SegmentLabel("本地曲库", selected = true)
        }
    }
}

@Composable
private fun SegmentLabel(text: String, selected: Boolean) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(if (selected) Color.White.copy(alpha = 0.09f) else Color.Transparent)
            .padding(horizontal = 28.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = text,
                color = if (selected) Color.White else Color.White.copy(alpha = 0.54f),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            )
            if (selected) {
                Spacer(Modifier.height(4.dp))
                Box(
                    Modifier
                        .width(60.dp)
                        .height(3.dp)
                        .background(YuandaoBlue, RoundedCornerShape(2.dp))
                )
            }
        }
    }
}

@Composable
private fun Header(
    onRequestScan: () -> Unit,
    onRequestSafFolder: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                text = "音乐",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "本机存储 · Hi-Res ready",
                color = Color.White.copy(alpha = 0.62f),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        Row {
            IconButton(onClick = onRequestScan) {
                Icon(Icons.Rounded.Refresh, contentDescription = "Scan local music")
            }
            IconButton(onClick = onRequestSafFolder) {
                Icon(Icons.Rounded.FolderOpen, contentDescription = "Add folder")
            }
            IconButton(onClick = {}) {
                Icon(Icons.Rounded.Search, contentDescription = "Search")
            }
            IconButton(onClick = {}) {
                Icon(Icons.Rounded.Settings, contentDescription = "Settings")
            }
        }
    }
}

@Composable
private fun ContinueCard(
    playbackState: PlaybackUiState,
    fallbackTrack: Track?,
    lyrics: TimedLyrics?,
    onPlayFallback: () -> Unit,
    onPlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSeek: (Long) -> Unit,
    onShuffle: () -> Unit,
    onRepeat: () -> Unit,
) {
    val track = playbackState.currentTrack ?: fallbackTrack
    GlassPanel {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = "继续播放",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(12.dp))
            if (track == null) {
                EmptyLibraryCallout(onPlayFallback)
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CoverArtwork(track, modifier = Modifier.size(132.dp))
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = track.title,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = track.artistName,
                            color = Color.White.copy(alpha = 0.66f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = track.albumTitle,
                            color = Color.White.copy(alpha = 0.46f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = "${playbackState.queueSummary.positionLabel} · ${playbackState.queueSummary.statusLabel} · ${playbackState.queueSummary.modeLabel}",
                            color = Color.White.copy(alpha = 0.44f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodySmall,
                        )
                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Pill(playbackState.audioInfo.source.formatName ?: track.format.displayName)
                            Pill(playbackState.audioInfo.qualityLabel.ifBlank { track.qualityLabel })
                            if (playbackState.queueSummary.upcomingCount > 0) {
                                Pill("待播 ${playbackState.queueSummary.upcomingCount} 首")
                            }
                        }
                    }
                    IconButton(
                        onClick = if (playbackState.currentTrack == null) onPlayFallback else onPlayPause,
                        modifier = Modifier
                            .size(58.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape),
                    ) {
                        Icon(
                            imageVector = if (playbackState.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = "Play",
                            tint = YuandaoGreen,
                            modifier = Modifier.size(34.dp),
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
                val duration = playbackState.durationMs.takeIf { it > 0 } ?: track.durationMs
                val progress = if (duration > 0) {
                    playbackState.positionMs.toFloat() / duration.toFloat()
                } else {
                    0f
                }.coerceIn(0f, 1f)
                val sliderMax = duration.coerceAtLeast(1L).toFloat()
                var isSeeking by remember(track.id) { mutableStateOf(false) }
                var pendingSeekPosition by remember(track.id) {
                    mutableFloatStateOf(playbackState.positionMs.toFloat().coerceIn(0f, sliderMax))
                }
                LaunchedEffect(track.id, playbackState.positionMs, sliderMax, isSeeking) {
                    if (!isSeeking) {
                        pendingSeekPosition = playbackState.positionMs.toFloat().coerceIn(0f, sliderMax)
                    }
                }
                val displayedPosition = if (isSeeking) {
                    pendingSeekPosition.toLong()
                } else {
                    playbackState.positionMs
                }
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth(),
                    color = YuandaoGreen,
                    trackColor = Color.White.copy(alpha = 0.14f),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(formatDuration(displayedPosition), color = Color.White.copy(alpha = 0.66f))
                    Text(formatDuration(duration), color = Color.White.copy(alpha = 0.66f))
                }
                Slider(
                    value = pendingSeekPosition,
                    onValueChange = {
                        isSeeking = true
                        pendingSeekPosition = it
                    },
                    onValueChangeFinished = {
                        isSeeking = false
                        onSeek(pendingSeekPosition.toLong())
                    },
                    valueRange = 0f..sliderMax,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onShuffle) {
                        Icon(Icons.Rounded.Shuffle, contentDescription = "Shuffle", tint = if (playbackState.shuffled) YuandaoBlue else Color.White.copy(alpha = 0.72f))
                    }
                    IconButton(onClick = onPrevious) {
                        Icon(Icons.Rounded.SkipPrevious, contentDescription = "Previous")
                    }
                    IconButton(onClick = onPlayPause) {
                        Icon(
                            imageVector = if (playbackState.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = "Play or pause",
                            tint = YuandaoGreen,
                        )
                    }
                    IconButton(onClick = onNext) {
                        Icon(Icons.Rounded.SkipNext, contentDescription = "Next")
                    }
                    IconButton(onClick = onRepeat) {
                        Icon(
                            Icons.Rounded.Repeat,
                            contentDescription = "Repeat",
                            tint = if (playbackState.repeatMode != PlaybackRepeatMode.NONE) YuandaoBlue else Color.White.copy(alpha = 0.72f),
                        )
                    }
                }
                if (playbackState.isBuffering) {
                    Text(
                        text = "正在缓冲本地音频...",
                        color = Color.White.copy(alpha = 0.58f),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                playbackState.errorMessage?.let { message ->
                    Text(
                        text = message,
                        color = YuandaoOrange,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                lyrics?.lineAt(playbackState.positionMs)?.let { line ->
                    Text(
                        text = line.text,
                        modifier = Modifier.padding(top = 6.dp),
                        color = Color.White.copy(alpha = 0.76f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyLibraryCallout(onRequestScan: () -> Unit) {
    Column(horizontalAlignment = Alignment.Start) {
        Text("还没有本地曲库", color = Color.White.copy(alpha = 0.72f))
        Spacer(Modifier.height(10.dp))
        Button(onClick = onRequestScan, colors = ButtonDefaults.buttonColors(containerColor = YuandaoBlue)) {
            Text("扫描本机音乐")
        }
    }
}

@Composable
private fun RecentTracks(
    title: String,
    detail: String,
    tracks: List<Track>,
    onPlayTrack: (Track) -> Unit,
) {
    SectionHeader(title, detail)
    Spacer(Modifier.height(8.dp))
    if (tracks.isEmpty()) {
        Text("扫描本机音乐后，这里会显示歌曲。", color = Color.White.copy(alpha = 0.55f))
        return
    }
    LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        items(tracks.take(12), key = { it.id }) { track ->
            Column(
                modifier = Modifier
                    .width(138.dp)
                    .clickable { onPlayTrack(track) }
            ) {
                CoverArtwork(track, modifier = Modifier.size(138.dp))
                Spacer(Modifier.height(8.dp))
                Text(track.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    track.artistName,
                    color = Color.White.copy(alpha = 0.54f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun MiniPlayerBar(
    playbackState: PlaybackUiState,
    onPlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onShuffle: () -> Unit,
    onRepeat: () -> Unit,
    onShowQueue: () -> Unit,
    onStopPlayback: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val track = playbackState.currentTrack ?: return
    val duration = playbackState.durationMs.takeIf { it > 0 } ?: track.durationMs
    val progress = if (duration > 0) {
        playbackState.positionMs.toFloat() / duration.toFloat()
    } else {
        0f
    }.coerceIn(0f, 1f)
    val sourceQuality = playbackState.audioInfo.sourceTechnicalLabel.ifBlank { track.qualityLabel }
    val runtimeQuality = playbackState.audioInfo.runtimeTechnicalLabel
    val quality = listOfNotNull(
        sourceQuality.takeIf { it.isNotBlank() },
        runtimeQuality.takeIf { it.isNotBlank() }?.let { "运行: $it" },
    ).joinToString(" · ")

    GlassPanel(modifier = modifier, shape = RoundedCornerShape(16.dp)) {
        Column {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
                color = YuandaoGreen,
                trackColor = Color.White.copy(alpha = 0.10f),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CoverArtwork(track, modifier = Modifier.size(48.dp))
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = playbackState.queueSummary.statusLabel,
                        color = Color.White.copy(alpha = 0.50f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Text(
                        text = "${playbackState.queueSummary.positionLabel} · ${playbackState.queueSummary.modeLabel}",
                        color = Color.White.copy(alpha = 0.46f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Text(
                        text = track.title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "${track.artistName} · $quality",
                        color = Color.White.copy(alpha = 0.56f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                IconButton(onClick = onShuffle) {
                    Icon(
                        Icons.Rounded.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (playbackState.shuffled) YuandaoBlue else Color.White.copy(alpha = 0.62f),
                    )
                }
                IconButton(onClick = onPrevious) {
                    Icon(Icons.Rounded.SkipPrevious, contentDescription = "Previous")
                }
                IconButton(
                    onClick = onPlayPause,
                    modifier = Modifier
                        .size(46.dp)
                        .background(Color.Black.copy(alpha = 0.42f), CircleShape),
                ) {
                    Icon(
                        imageVector = if (playbackState.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = "Play or pause",
                        tint = YuandaoGreen,
                    )
                }
                IconButton(onClick = onNext) {
                    Icon(Icons.Rounded.SkipNext, contentDescription = "Next")
                }
                IconButton(onClick = onRepeat) {
                    Icon(
                        Icons.Rounded.Repeat,
                        contentDescription = "Repeat",
                        tint = if (playbackState.repeatMode != PlaybackRepeatMode.NONE) YuandaoBlue else Color.White.copy(alpha = 0.62f),
                    )
                }
                IconButton(onClick = onShowQueue) {
                    Icon(
                        Icons.AutoMirrored.Rounded.QueueMusic,
                        contentDescription = "Queue",
                        tint = Color.White.copy(alpha = 0.62f),
                    )
                }
                IconButton(onClick = onStopPlayback) {
                    Icon(
                        Icons.Rounded.Close,
                        contentDescription = "Close player",
                        tint = Color.White.copy(alpha = 0.62f),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QueueDrawer(
    state: PlaybackQueueDrawerState,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF10151C),
        contentColor = Color.White,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text("播放队列", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                    Text(state.modeLabel, color = Color.White.copy(alpha = 0.56f))
                }
                TextButton(onClick = onDismiss) {
                    Text("关闭")
                }
            }
            Spacer(Modifier.height(12.dp))
            if (state.isEmpty) {
                Text("当前没有播放队列", color = Color.White.copy(alpha = 0.56f))
            } else {
                state.current?.let { current ->
                    QueueDrawerSection("当前播放")
                    QueueDrawerItemRow(current)
                }
                if (state.upNext.isNotEmpty()) {
                    QueueDrawerSection("即将播放")
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 280.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(state.upNext, key = { it.id }) { item ->
                            QueueDrawerItemRow(item)
                        }
                    }
                }
                if (state.previous.isNotEmpty()) {
                    QueueDrawerSection("已播放")
                    state.previous.takeLast(3).forEach { item ->
                        QueueDrawerItemRow(item)
                    }
                }
            }
        }
    }
}

@Composable
private fun QueueDrawerSection(title: String) {
    Text(
        text = title,
        modifier = Modifier.padding(top = 12.dp, bottom = 6.dp),
        color = Color.White.copy(alpha = 0.58f),
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
    )
}

@Composable
private fun QueueDrawerItemRow(item: PlaybackQueueDrawerItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (item.isCurrent) YuandaoBlue.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.06f))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .background(if (item.isCurrent) YuandaoBlue.copy(alpha = 0.24f) else Color.White.copy(alpha = 0.08f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = item.positionLabel.ifBlank { "-" },
                color = if (item.isCurrent) YuandaoBlue else Color.White.copy(alpha = 0.58f),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(item.title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold)
            Text(item.artistName, maxLines = 1, overflow = TextOverflow.Ellipsis, color = Color.White.copy(alpha = 0.54f))
        }
        Text(item.qualityLabel, color = Color.White.copy(alpha = 0.58f), style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun HiResFormats(tracks: List<Track>) {
    SectionHeader("Hi-Res / 无损", "本地格式")
    Spacer(Modifier.height(8.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        FormatTile(AudioFormat.FLAC, tracks.count { it.format == AudioFormat.FLAC }, YuandaoGreen, Modifier.weight(1f))
        FormatTile(AudioFormat.DSD, tracks.count { it.format == AudioFormat.DSD }, YuandaoOrange, Modifier.weight(1f))
        FormatTile(AudioFormat.WAV, tracks.count { it.format == AudioFormat.WAV }, YuandaoBlue, Modifier.weight(1f))
        FormatTile(AudioFormat.ALAC, tracks.count { it.format == AudioFormat.ALAC }, YuandaoCyan, Modifier.weight(1f))
    }
}

@Composable
private fun FormatTile(
    format: AudioFormat,
    count: Int,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    GlassPanel(modifier = modifier, shape = RoundedCornerShape(8.dp)) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Rounded.MusicNote, contentDescription = null, tint = tint)
            Spacer(Modifier.width(10.dp))
            Column {
                Text(format.displayName, fontWeight = FontWeight.SemiBold)
                Text("$count 首", color = Color.White.copy(alpha = 0.56f))
            }
        }
    }
}

@Composable
private fun LibraryOverview(
    tracks: List<Track>,
    albums: List<MusicAlbum>,
    artists: List<Artist>,
) {
    SectionHeader("资料库概览", "第一阶段")
    Spacer(Modifier.height(8.dp))
    GlassPanel {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            OverviewMetric(Icons.Rounded.MusicNote, "歌曲", tracks.size.toString(), YuandaoBlue)
            OverviewMetric(Icons.Rounded.People, "歌手", artists.size.toString(), YuandaoGreen)
            OverviewMetric(Icons.Rounded.Album, "专辑", albums.size.toString(), Color(0xFF8D72FF))
            OverviewMetric(Icons.Rounded.MoreHoriz, "云文件", "预留", YuandaoOrange)
        }
    }
}

@Composable
private fun OutputCard(
    outputDevices: List<OutputDevice>,
    playbackState: PlaybackUiState,
    onRefreshOutputs: () -> Unit,
) {
    val active = outputDevices.firstOrNull()
    GlassPanel {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(10.dp)
                    .background(YuandaoGreen, CircleShape)
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(active?.name ?: "本机输出", fontWeight = FontWeight.SemiBold)
                Text(
                    playbackState.audioInfo.technicalLabel
                        .ifBlank { playbackState.currentTrack?.qualityLabel.orEmpty() }
                        .ifBlank { "系统输出 · USB 独占预留" },
                    color = Color.White.copy(alpha = 0.55f),
                )
            }
            OutlinedButton(onClick = onRefreshOutputs) {
                Text("刷新")
            }
        }
    }
}

@Composable
private fun ScanStatus(
    scanState: ScanUiState,
    onRequestScan: () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (scanState.scanning) {
            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            Spacer(Modifier.width(8.dp))
        }
        Text(
            text = scanState.message ?: "云端与流媒体入口已预留，第一版先完成本地曲库主链路。",
            color = Color.White.copy(alpha = 0.56f),
            modifier = Modifier.weight(1f),
        )
        if (!scanState.scanning) {
            Button(onClick = onRequestScan, colors = ButtonDefaults.buttonColors(containerColor = YuandaoSurfaceHigh)) {
                Text("扫描")
            }
        }
    }
}

@Composable
private fun SafRootScanControls(
    safRootCount: Int,
    scanning: Boolean,
    onRescanSafFolders: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "已保存文件夹：$safRootCount",
            color = Color.White.copy(alpha = 0.48f),
            style = MaterialTheme.typography.bodySmall,
        )
        if (safRootCount > 0) {
            OutlinedButton(
                onClick = onRescanSafFolders,
                enabled = !scanning,
            ) {
                Text("重新扫描文件夹")
            }
        }
    }
}

@Composable
private fun OverviewMetric(icon: ImageVector, label: String, value: String, tint: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(30.dp))
        Spacer(Modifier.height(6.dp))
        Text(label, color = Color.White.copy(alpha = 0.56f))
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun SectionHeader(title: String, detail: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(detail, color = Color.White.copy(alpha = 0.48f))
    }
}

@Composable
private fun CoverArtwork(
    track: Track,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val coverUri = track.coverUri
    val coverUriString = coverUri?.toString()
    var imageBitmap by remember(coverUriString) {
        mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null)
    }

    LaunchedEffect(coverUriString) {
        imageBitmap = null
        if (CoverArtPolicy.canLoad(coverUriString) && coverUri != null) {
            imageBitmap = withContext(Dispatchers.IO) {
                runCatching {
                    context.contentResolver.openInputStream(coverUri)?.use { input ->
                        BitmapFactory.decodeStream(input)?.asImageBitmap()
                    }
                }.getOrNull()
            }
        }
    }

    val bitmap = imageBitmap
    if (bitmap != null) {
        Image(
            bitmap = bitmap,
            contentDescription = "${track.title} cover",
            modifier = modifier.clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop,
        )
    } else {
        CoverPlaceholder(track, modifier)
    }
}

@Composable
private fun CoverPlaceholder(
    track: Track,
    modifier: Modifier = Modifier,
) {
    val colors = remember(track.id) {
        listOf(
            Color(0xFF183B4E),
            if (track.format.isHiResCandidate) YuandaoBlue.copy(alpha = 0.72f) else Color(0xFF313641),
            Color(0xFF0D1118),
        )
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Brush.linearGradient(colors)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            Icons.Rounded.PlayArrow,
            contentDescription = null,
            tint = YuandaoGreen,
            modifier = Modifier
                .size(52.dp)
                .background(Color.Black.copy(alpha = 0.45f), CircleShape)
                .padding(10.dp),
        )
    }
}

@Composable
private fun Pill(text: String) {
    Box(
        Modifier
            .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(5.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text, color = Color.White.copy(alpha = 0.78f), style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun GlassPanel(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(14.dp),
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.08f),
                        Color.White.copy(alpha = 0.035f),
                    )
                )
            )
            .heightIn(min = 1.dp),
    ) {
        content()
    }
}

private fun formatDuration(durationMs: Long): String {
    val totalSeconds = (durationMs / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "$minutes:${seconds.toString().padStart(2, '0')}"
}
