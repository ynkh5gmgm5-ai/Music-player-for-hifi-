package com.yuandao.music.playback

import android.content.Context
import android.media.AudioAttributes as PlatformAudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.PlaybackException
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DecoderReuseEvaluation
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import com.yuandao.music.data.db.MusicDao
import com.yuandao.music.data.db.PlaybackStateEntity
import com.yuandao.music.data.db.QueueItemEntity
import com.yuandao.music.data.model.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@androidx.annotation.OptIn(UnstableApi::class)
class PlaybackController(
    context: Context,
    private val dao: MusicDao,
) : Player.Listener, AudioManager.OnAudioFocusChangeListener, PlaybackGateway {
    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val environmentConfig = PlaybackEnvironmentPolicy.defaultConfig
    private val audioManager = appContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
        .setAudioAttributes(
            PlatformAudioAttributes.Builder()
                .setUsage(PlatformAudioAttributes.USAGE_MEDIA)
                .setContentType(PlatformAudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
        )
        .setOnAudioFocusChangeListener(this)
        .build()
    private val player = ExoPlayer.Builder(context)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .build(),
            environmentConfig.handleAudioFocus,
        )
        .setWakeMode(environmentConfig.wakeMode)
        .build()
        .apply { setHandleAudioBecomingNoisy(environmentConfig.pauseWhenAudioBecomesNoisy) }

    private var linearQueue: List<Track> = emptyList()
    private var queue: List<Track> = emptyList()
    private var shuffleEnabled = false
    private var restored = false
    private var playbackErrorMessage: String? = null
    private var lastPeriodicPersistAtMs = 0L
    private val playbackHistoryGate = PlaybackHistoryGate()
    private var lastMediaItemIndex = C.INDEX_UNSET
    private var activeAudioFormat: Format? = null
    private var activeDecoderName: String? = null
    private var resumeOnFocusGain = false

    private val _state = MutableStateFlow(PlaybackUiState())
    override val state: StateFlow<PlaybackUiState> = _state
    val sessionPlayer: Player = player

    init {
        player.addListener(this)
        player.addAnalyticsListener(
            object : AnalyticsListener {
                override fun onAudioInputFormatChanged(
                    eventTime: AnalyticsListener.EventTime,
                    format: Format,
                    decoderReuseEvaluation: DecoderReuseEvaluation?,
                ) {
                    activeAudioFormat = format
                    publishState()
                }

                override fun onAudioDecoderInitialized(
                    eventTime: AnalyticsListener.EventTime,
                    decoderName: String,
                    initializedTimestampMs: Long,
                    initializationDurationMs: Long,
                ) {
                    activeDecoderName = decoderName
                    publishState()
                }

                override fun onAudioDisabled(
                    eventTime: AnalyticsListener.EventTime,
                    decoderCounters: androidx.media3.exoplayer.DecoderCounters,
                ) {
                    activeAudioFormat = null
                    activeDecoderName = null
                    publishState()
                }
            }
        )
        scope.launch {
            while (isActive) {
                publishState()
                recordCurrentPlaybackIfEligible()
                persistPositionIfNeeded()
                delay(500)
            }
        }
    }

    override suspend fun restoreIfPossible(availableTracks: List<Track>) {
        if (restored || availableTracks.isEmpty()) return
        restored = true

        val saved = withContext(Dispatchers.IO) { dao.getPlaybackState() } ?: return
        val savedQueue = withContext(Dispatchers.IO) { dao.getQueue() }
        val tracksById = availableTracks.associateBy { it.id }
        val restoredQueue = PlaybackQueuePlanner.restoreTracks(
            savedQueue = savedQueue.mapNotNull { tracksById[it.trackId] },
            currentTrackId = saved.currentTrackId,
            currentIndex = saved.currentIndex,
        ) ?: return

        linearQueue = if (saved.shuffled) {
            PlaybackQueuePlanner.restoreLinearTrackOrder(
                availableTracks = availableTracks,
                savedQueue = restoredQueue.tracks,
            )
        } else {
            restoredQueue.tracks
        }
        queue = restoredQueue.tracks
        shuffleEnabled = saved.shuffled
        player.setMediaItems(restoredQueue.tracks.map { it.toMediaItem() })
        player.repeatMode = saved.repeatMode.toPlayerRepeatMode()
        player.shuffleModeEnabled = false
        player.prepare()
        player.seekTo(restoredQueue.startIndex, saved.positionMs.coerceAtLeast(0L))
        lastMediaItemIndex = restoredQueue.startIndex
        publishState()
        updateHistoryCandidate()
    }

    override fun playQueue(tracks: List<Track>, startIndex: Int) {
        when (val plan = PlaybackQueuePlanner.planTracks(tracks, startIndex)) {
            is PlaybackQueuePlan.Rejected -> {
                playbackErrorMessage = plan.reason
                publishState()
                return
            }
            is PlaybackQueuePlan.Ready -> {
                playbackErrorMessage = null
                linearQueue = plan.queue.tracks
                val plannedQueue = if (shuffleEnabled) {
                    PlaybackQueuePlanner.randomizeKeepingCurrent(plan.queue.tracks, plan.queue.startIndex)
                        ?: plan.queue
                } else {
                    plan.queue
                }
                queue = plannedQueue.tracks
                player.setMediaItems(plannedQueue.tracks.map { it.toMediaItem() })
                player.seekTo(plannedQueue.startIndex, C.TIME_UNSET)
                lastMediaItemIndex = plannedQueue.startIndex
            }
        }
        player.prepare()
        if (!playWhenFocusAvailable()) return
        publishState()
        updateHistoryCandidate()
        persistState()
    }

    override fun playQueueTrack(trackId: String) {
        val targetIndex = queue.indexOfFirst { it.id == trackId }
        if (targetIndex < 0) return

        val currentTrackId = _state.value.currentTrack?.id
        val targetPositionMs = if (currentTrackId == trackId) {
            player.currentPosition.coerceAtLeast(0L)
        } else {
            0L
        }
        playbackErrorMessage = null
        player.setMediaItems(queue.map { it.toMediaItem() }, targetIndex, targetPositionMs)
        lastMediaItemIndex = targetIndex
        player.prepare()
        if (!playWhenFocusAvailable()) return
        publishState()
        persistState()
    }

    override fun removeQueueTrack(trackId: String) {
        val edit = PlaybackQueueEditor.remove(
            tracks = queue,
            currentIndex = player.currentMediaItemIndex,
            removedTrackId = trackId,
            trackId = Track::id,
        ) ?: return

        playbackErrorMessage = null
        val wasPlaying = player.playWhenReady
        val currentPositionMs = player.currentPosition.coerceAtLeast(0L)
        linearQueue = linearQueue.filter { it.id != trackId }

        if (edit.tracks.isEmpty()) {
            stopPlayback()
            return
        }

        queue = edit.tracks
        val nextIndex = edit.currentIndex.coerceIn(0, queue.lastIndex)
        val nextPositionMs = if (edit.removedCurrent) 0L else currentPositionMs
        player.setMediaItems(queue.map { it.toMediaItem() }, nextIndex, nextPositionMs)
        lastMediaItemIndex = nextIndex
        player.prepare()
        if (wasPlaying && !playWhenFocusAvailable()) return
        publishState()
        persistState()
    }

    override fun clearQueue() {
        stopPlayback()
    }

    override fun togglePlayPause() {
        if (queue.isEmpty()) {
            playbackErrorMessage = PlaybackErrorMessage.noLocalTracks
            publishState()
            return
        }
        playbackErrorMessage = null
        if (player.isPlaying) {
            player.pause()
            abandonAudioFocus()
        } else {
            if (!playWhenFocusAvailable()) return
        }
        publishState()
        persistState()
    }

    override fun next() {
        if (queue.isEmpty()) return
        playbackErrorMessage = null
        when {
            prepareNextShuffleCycle() -> Unit
            player.hasNextMediaItem() -> player.seekToNextMediaItem()
            queue.isNotEmpty() -> player.seekTo(0, C.TIME_UNSET)
        }
        if (!playWhenFocusAvailable()) return
        publishState()
        persistState()
    }

    override fun previous() {
        if (queue.isEmpty()) return
        playbackErrorMessage = null
        if (player.currentPosition > PREVIOUS_RESTART_THRESHOLD_MS) {
            player.seekTo(0)
        } else {
            player.seekToPreviousMediaItem()
        }
        if (!playWhenFocusAvailable()) return
        publishState()
        persistState()
    }

    override fun seekTo(positionMs: Long) {
        if (queue.isEmpty()) return
        player.seekTo(positionMs.coerceAtLeast(0L))
        publishState()
        persistState()
    }

    override fun cycleRepeatMode() {
        playbackErrorMessage = null
        val next = when (_state.value.repeatMode) {
            PlaybackRepeatMode.NONE -> PlaybackRepeatMode.ALL
            PlaybackRepeatMode.ALL -> PlaybackRepeatMode.ONE
            PlaybackRepeatMode.ONE -> PlaybackRepeatMode.NONE
        }
        player.repeatMode = next.toPlayerRepeatMode()
        publishState()
        persistState()
    }

    override fun toggleShuffle() {
        playbackErrorMessage = null
        if (queue.isEmpty()) return

        val currentTrackId = _state.value.currentTrack?.id
        val currentPositionMs = player.currentPosition.coerceAtLeast(0L)
        val wasPlaying = player.playWhenReady
        shuffleEnabled = !shuffleEnabled

        val nextPlan = if (shuffleEnabled) {
            val currentIndex = queue.indexOfFirst { it.id == currentTrackId }.coerceAtLeast(0)
            PlaybackQueuePlanner.randomizeKeepingCurrent(queue, currentIndex)
        } else {
            val restoredLinearQueue = linearQueue.takeIf { it.isNotEmpty() } ?: queue
            val restoredIndex = restoredLinearQueue.indexOfFirst { it.id == currentTrackId }
                .takeIf { it >= 0 }
                ?: 0
            PlannedPlaybackQueue(restoredLinearQueue, restoredIndex)
        } ?: return

        queue = nextPlan.tracks
        player.setMediaItems(queue.map { it.toMediaItem() }, nextPlan.startIndex, currentPositionMs)
        lastMediaItemIndex = nextPlan.startIndex
        player.prepare()
        if (wasPlaying) {
            if (!playWhenFocusAvailable()) return
        }
        publishState()
        persistState()
    }

    override fun stopPlayback() {
        playbackErrorMessage = null
        player.stop()
        player.clearMediaItems()
        abandonAudioFocus()
        queue = emptyList()
        linearQueue = emptyList()
        lastMediaItemIndex = C.INDEX_UNSET
        activeAudioFormat = null
        activeDecoderName = null
        publishState()
        persistState()
        YuandaoPlaybackService.stop(appContext)
    }

    override fun onAudioFocusChange(focusChange: Int) {
        val decision = AudioFocusPolicy.decide(
            focusChange = focusChange,
            wasPlayingBeforeLoss = player.isPlaying || player.playWhenReady,
            resumeOnFocusGain = resumeOnFocusGain,
        )
        resumeOnFocusGain = decision.resumeOnFocusGain

        when (decision.action) {
            AudioFocusAction.Pause -> {
                player.pause()
                if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                    abandonAudioFocus()
                }
            }
            AudioFocusAction.Duck -> player.volume = DUCK_VOLUME
            AudioFocusAction.RestoreVolume -> player.volume = DEFAULT_VOLUME
            AudioFocusAction.Resume -> {
                player.volume = DEFAULT_VOLUME
                YuandaoPlaybackService.start(appContext)
                player.play()
            }
            AudioFocusAction.NoOp -> Unit
        }

        publishState()
        persistState()
    }

    private fun playWhenFocusAvailable(): Boolean {
        if (!requestAudioFocus()) {
            playbackErrorMessage = PlaybackErrorMessage.audioFocusUnavailable
            publishState()
            persistState()
            return false
        }
        player.volume = DEFAULT_VOLUME
        playbackErrorMessage = null
        YuandaoPlaybackService.start(appContext)
        player.play()
        return true
    }

    private fun requestAudioFocus(): Boolean =
        audioManager.requestAudioFocus(audioFocusRequest) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED

    private fun abandonAudioFocus() {
        resumeOnFocusGain = false
        audioManager.abandonAudioFocusRequest(audioFocusRequest)
    }

    private fun prepareNextShuffleCycle(): Boolean =
        prepareNextShuffleCycleFromQueueIndex(player.currentMediaItemIndex)

    private fun prepareNextShuffleCycleFromQueueIndex(completedQueueIndex: Int): Boolean {
        if (!shuffleEnabled || queue.isEmpty() || completedQueueIndex != queue.lastIndex) {
            return false
        }

        val sourceQueue = linearQueue.takeIf { it.isNotEmpty() } ?: queue
        val completedTrackId = queue.getOrNull(completedQueueIndex)?.id
        val sourceIndex = sourceQueue.indexOfFirst { it.id == completedTrackId }
            .takeIf { it >= 0 }
            ?: 0
        val nextPlan = PlaybackQueuePlanner.randomizeForNextCycle(sourceQueue, sourceIndex)
            ?: return false

        queue = nextPlan.tracks
        player.setMediaItems(queue.map { it.toMediaItem() }, nextPlan.startIndex, C.TIME_UNSET)
        lastMediaItemIndex = nextPlan.startIndex
        player.prepare()
        return true
    }

    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        val previousIndex = lastMediaItemIndex
        val currentIndex = player.currentMediaItemIndex
        if (
            ShuffleCyclePolicy.shouldStartNextCycle(
                shuffleEnabled = shuffleEnabled,
                repeatMode = player.repeatMode,
                transitionReason = reason,
                previousIndex = previousIndex,
                currentIndex = currentIndex,
                queueSize = queue.size,
            )
        ) {
            prepareNextShuffleCycleFromQueueIndex(previousIndex)
        } else {
            lastMediaItemIndex = currentIndex
        }
    }

    override fun onEvents(player: Player, events: Player.Events) {
        if (events.contains(Player.EVENT_MEDIA_ITEM_TRANSITION)) {
            activeAudioFormat = null
        }
        publishState()
        if (events.contains(Player.EVENT_MEDIA_ITEM_TRANSITION)) {
            updateHistoryCandidate()
        }
        if (
            events.containsAny(
                Player.EVENT_MEDIA_ITEM_TRANSITION,
                Player.EVENT_PLAY_WHEN_READY_CHANGED,
                Player.EVENT_IS_PLAYING_CHANGED,
                Player.EVENT_REPEAT_MODE_CHANGED,
                Player.EVENT_SHUFFLE_MODE_ENABLED_CHANGED,
                Player.EVENT_POSITION_DISCONTINUITY,
            )
        ) {
            persistState()
        }
    }

    override fun onPlayerError(error: PlaybackException) {
        playbackErrorMessage = PlaybackErrorMessage.fromPlaybackException(
            error = error,
            trackTitle = _state.value.currentTrack?.title,
        )
        publishState()
        persistState()
    }

    private fun publishState() {
        val currentIndex = player.currentMediaItemIndex.takeIf { it >= 0 } ?: -1
        val currentTrack = queue.getOrNull(currentIndex)
        _state.value = PlaybackUiState(
            queue = queue,
            currentIndex = currentIndex,
            currentTrack = currentTrack,
            isPlaying = player.isPlaying,
            isBuffering = player.playbackState == Player.STATE_BUFFERING,
            positionMs = player.currentPosition.coerceAtLeast(0L),
            durationMs = PlaybackDurationResolver.resolve(
                trackDurationMs = currentTrack?.durationMs,
                playerDurationMs = player.duration,
            ),
            audioInfo = currentTrack.toPlaybackAudioInfo(activeAudioFormat, activeDecoderName),
            repeatMode = player.repeatMode.toPlaybackRepeatMode(),
            shuffled = shuffleEnabled,
            errorMessage = playbackErrorMessage,
        )
    }

    private fun persistPositionIfNeeded() {
        val snapshot = _state.value
        if (!snapshot.hasQueue) return

        val now = System.currentTimeMillis()
        if (now - lastPeriodicPersistAtMs < POSITION_PERSIST_INTERVAL_MS) return

        lastPeriodicPersistAtMs = now
        persistState(snapshot)
    }

    private fun persistState(snapshot: PlaybackUiState = _state.value) {
        scope.launch(Dispatchers.IO) {
            dao.replaceQueue(
                state = PlaybackStateEntity(
                    currentTrackId = snapshot.currentTrack?.id,
                    currentIndex = snapshot.currentIndex,
                    positionMs = snapshot.positionMs,
                    repeatMode = snapshot.repeatMode.name,
                    shuffled = shuffleEnabled,
                    updatedAtMs = System.currentTimeMillis(),
                ),
                items = snapshot.queue.mapIndexed { index, track ->
                    QueueItemEntity(index, track.id)
                },
            )
        }
    }

    private fun updateHistoryCandidate() {
        playbackHistoryGate.onCurrentTrackChanged(
            trackId = _state.value.currentTrack?.id,
            nowMs = System.currentTimeMillis(),
        )
    }

    private fun recordCurrentPlaybackIfEligible() {
        val snapshot = _state.value
        if (!snapshot.isPlaying) return
        val trackId = snapshot.currentTrack?.id ?: return
        if (
            !playbackHistoryGate.shouldRecord(
                trackId = trackId,
                positionMs = snapshot.positionMs,
                durationMs = snapshot.durationMs,
                nowMs = System.currentTimeMillis(),
            )
        ) {
            return
        }
        scope.launch(Dispatchers.IO) {
            dao.recordPlayback(trackId, System.currentTimeMillis())
        }
    }

    private fun Track.toMediaItem(): MediaItem =
        MediaItem.Builder()
            .setUri(uri)
            .setMediaId(id)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(title)
                    .setArtist(artistName)
                    .setAlbumTitle(albumTitle)
                    .setArtworkUri(coverUri)
                    .build()
            )
            .build()

    private fun Track?.toPlaybackAudioInfo(format: Format?, decoderName: String?): PlaybackAudioInfo {
        return PlaybackAudioInfo(
            source = SourceAudioInfo(
                formatName = this?.format?.displayName,
                sampleRateHz = this?.sampleRateHz,
                bitDepth = this?.bitDepth,
                channelCount = this?.channelCount,
                bitrateKbps = this?.bitrateKbps,
            ),
            runtime = RuntimeAudioInfo(
                codecName = format?.sampleMimeType?.substringAfter('/'),
                sampleRateHz = format?.sampleRate?.takeIf { it > 0 },
                bitDepth = format?.pcmEncoding.toBitDepth(),
                channelCount = format?.channelCount?.takeIf { it > 0 },
                bitrateKbps = format?.averageBitrate?.takeIf { it > 0 }?.div(1000),
                decoderName = decoderName,
            ),
        )
    }

    private fun Int?.toBitDepth(): Int? =
        when (this) {
            C.ENCODING_PCM_8BIT -> 8
            C.ENCODING_PCM_16BIT,
            C.ENCODING_PCM_16BIT_BIG_ENDIAN -> 16
            C.ENCODING_PCM_24BIT,
            C.ENCODING_PCM_24BIT_BIG_ENDIAN -> 24
            C.ENCODING_PCM_32BIT,
            C.ENCODING_PCM_32BIT_BIG_ENDIAN,
            C.ENCODING_PCM_FLOAT -> 32
            else -> null
        }

    private fun String.toPlayerRepeatMode(): Int =
        runCatching { PlaybackRepeatMode.valueOf(this) }
            .getOrDefault(PlaybackRepeatMode.NONE)
            .toPlayerRepeatMode()

    private fun PlaybackRepeatMode.toPlayerRepeatMode(): Int =
        when (this) {
            PlaybackRepeatMode.NONE -> Player.REPEAT_MODE_OFF
            PlaybackRepeatMode.ONE -> Player.REPEAT_MODE_ONE
            PlaybackRepeatMode.ALL -> Player.REPEAT_MODE_ALL
        }

    private fun Int.toPlaybackRepeatMode(): PlaybackRepeatMode =
        when (this) {
            Player.REPEAT_MODE_ONE -> PlaybackRepeatMode.ONE
            Player.REPEAT_MODE_ALL -> PlaybackRepeatMode.ALL
            else -> PlaybackRepeatMode.NONE
        }

    private companion object {
        const val PREVIOUS_RESTART_THRESHOLD_MS = 3000L
        const val POSITION_PERSIST_INTERVAL_MS = 5000L
        const val DEFAULT_VOLUME = 1f
        const val DUCK_VOLUME = 0.2f
    }
}
