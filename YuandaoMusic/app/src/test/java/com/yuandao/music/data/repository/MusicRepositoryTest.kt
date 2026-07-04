package com.yuandao.music.data.repository

import android.net.Uri
import com.yuandao.music.data.db.AlbumEntity
import com.yuandao.music.data.db.ArtistEntity
import com.yuandao.music.data.db.LibraryRootEntity
import com.yuandao.music.data.db.MusicDao
import com.yuandao.music.data.db.PlaybackHistoryEntity
import com.yuandao.music.data.db.PlaybackStateEntity
import com.yuandao.music.data.db.QueueItemEntity
import com.yuandao.music.data.db.TrackEntity
import com.yuandao.music.data.model.AudioFormat
import com.yuandao.music.data.model.AudioSourceType
import com.yuandao.music.scanner.AudioScanner
import com.yuandao.music.scanner.SafAudioScanner
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MusicRepositoryTest {
    @Test
    fun safRootsFlowExposesPersistedSafFolders() = runBlocking {
        val dao = FakeMusicDao(
            initialTracks = emptyList(),
            initialRoots = listOf(
                libraryRoot(uri = "content://tree/music", displayName = "Music"),
            ),
        )
        val repository = MusicRepository(
            dao = dao,
            mediaStoreScanner = FakeAudioScanner(emptyList()),
            safScanner = FakeSafAudioScanner(emptyList()),
        )

        val roots = repository.safRoots.first()

        assertEquals(listOf("content://tree/music"), roots.map { it.uri })
        assertEquals(listOf("Music"), roots.map { it.displayName })
        assertEquals(listOf(true), roots.map { it.enabled })
    }

    @Test
    fun addSafRootStoresEnabledFolderAndPreservesExistingScanState() = runBlocking {
        val dao = FakeMusicDao(
            initialTracks = emptyList(),
            initialRoots = listOf(
                libraryRoot(
                    uri = "content://tree/music",
                    displayName = "Old Music",
                    enabled = false,
                    createdAtMs = 10L,
                    updatedAtMs = 20L,
                    lastScannedAtMs = 30L,
                ),
            ),
        )
        val repository = MusicRepository(
            dao = dao,
            mediaStoreScanner = FakeAudioScanner(emptyList()),
            safScanner = FakeSafAudioScanner(emptyList()),
            clock = { 99L },
        )

        repository.addSafRoot(
            uri = "content://tree/music",
            displayName = "Music",
        )

        val root = dao.root("content://tree/music")
        assertEquals("Music", root.displayName)
        assertEquals("SAF", root.type)
        assertTrue(root.enabled)
        assertEquals(10L, root.createdAtMs)
        assertEquals(99L, root.updatedAtMs)
        assertEquals(30L, root.lastScannedAtMs)
    }

    @Test
    fun rescanSafRootsScansEnabledFoldersAndMarksScanTime() = runBlocking {
        val safTrack = track(
            id = "saf-track",
            sourceId = "saf_track",
            sourceLabel = "SAF Folder",
            title = "Folder Track",
            artist = "Folder Artist",
            album = "Folder Album",
            indexedAtMs = 10L,
        )
        var scannedRoots: List<String> = emptyList()
        val dao = FakeMusicDao(
            initialTracks = emptyList(),
            initialRoots = listOf(
                libraryRoot(uri = "content://tree/music", displayName = "Music", enabled = true),
                libraryRoot(uri = "content://tree/disabled", displayName = "Disabled", enabled = false),
            ),
        )
        val repository = MusicRepository(
            dao = dao,
            mediaStoreScanner = FakeAudioScanner(emptyList()),
            safScanner = FakeSafAudioScanner(emptyList()),
            clock = { 500L },
            scanPersistedSafRoots = { roots ->
                scannedRoots = roots
                listOf(safTrack)
            },
        )

        val summary = repository.rescanSafRoots()

        assertEquals(1, summary.scannedTracks)
        assertEquals(listOf("content://tree/music"), scannedRoots)
        assertEquals(500L, dao.root("content://tree/music").lastScannedAtMs)
        assertEquals(null, dao.root("content://tree/disabled").lastScannedAtMs)
        assertEquals(listOf("saf-track"), dao.trackIds())
    }

    @Test
    fun mediaStoreScanPrunesMissingMediaStoreTracksAndRebuildsAggregates() = runBlocking {
        val staleMediaStore = track(
            id = "stale-media",
            sourceId = "mediastore_stale",
            sourceLabel = "MediaStore",
            title = "Old Track",
            artist = "Old Artist",
            album = "Old Album",
            indexedAtMs = 10L,
        )
        val safTrack = track(
            id = "saf-track",
            sourceId = "saf_track",
            sourceLabel = "SAF Folder",
            title = "Folder Track",
            artist = "Folder Artist",
            album = "Folder Album",
            indexedAtMs = 10L,
        )
        val freshMediaStore = track(
            id = "fresh-media",
            sourceId = "mediastore_fresh",
            sourceLabel = "MediaStore",
            title = "Fresh Track",
            artist = "Fresh Artist",
            album = "Fresh Album",
            indexedAtMs = 10L,
        )
        val dao = FakeMusicDao(listOf(staleMediaStore, safTrack))
        val repository = MusicRepository(
            dao = dao,
            mediaStoreScanner = FakeAudioScanner(listOf(freshMediaStore)),
            safScanner = FakeSafAudioScanner(emptyList()),
        )

        val summary = repository.scanMediaStore()

        assertEquals(1, summary.scannedTracks)
        assertEquals(listOf("fresh-media", "saf-track"), dao.trackIds())
        assertEquals(listOf("Folder Artist", "Fresh Artist"), dao.artistNames())
        assertEquals(listOf("Folder Album", "Fresh Album"), dao.albumTitles())
        assertFalse(dao.trackIds().contains("stale-media"))
    }

    @Test
    fun emptyMediaStoreScanDoesNotPruneExistingLibrary() = runBlocking {
        val staleMediaStore = track(
            id = "stale-media",
            sourceId = "mediastore_stale",
            sourceLabel = "MediaStore",
            title = "Old Track",
            artist = "Old Artist",
            album = "Old Album",
            indexedAtMs = 10L,
        )
        val dao = FakeMusicDao(listOf(staleMediaStore))
        val repository = MusicRepository(
            dao = dao,
            mediaStoreScanner = FakeAudioScanner(emptyList()),
            safScanner = FakeSafAudioScanner(emptyList()),
        )

        val summary = repository.scanMediaStore()

        assertEquals(0, summary.scannedTracks)
        assertEquals(listOf("stale-media"), dao.trackIds())
    }

    @Test
    fun safScanDoesNotPruneMediaStoreTracks() = runBlocking {
        val mediaStoreTrack = track(
            id = "media-track",
            sourceId = "mediastore_track",
            sourceLabel = "MediaStore",
            title = "Media Track",
            artist = "Media Artist",
            album = "Media Album",
            indexedAtMs = 10L,
        )
        val safTrack = track(
            id = "saf-track",
            sourceId = "saf_track",
            sourceLabel = "SAF Folder",
            title = "Folder Track",
            artist = "Folder Artist",
            album = "Folder Album",
            indexedAtMs = 10L,
        )
        val dao = FakeMusicDao(listOf(mediaStoreTrack))
        val repository = MusicRepository(
            dao = dao,
            mediaStoreScanner = FakeAudioScanner(emptyList()),
            safScanner = FakeSafAudioScanner(listOf(safTrack)),
        )

        val summary = repository.scanSafRoots(emptyList())

        assertEquals(1, summary.scannedTracks)
        assertEquals(listOf("media-track", "saf-track"), dao.trackIds())
    }

    private class FakeAudioScanner(
        private val result: List<TrackEntity>,
    ) : AudioScanner {
        override suspend fun scan(): List<TrackEntity> = result
    }

    private class FakeSafAudioScanner(
        private val result: List<TrackEntity>,
    ) : SafAudioScanner {
        override suspend fun scanRoots(roots: List<Uri>): List<TrackEntity> = result
    }

    private class FakeMusicDao(
        initialTracks: List<TrackEntity>,
        initialRoots: List<LibraryRootEntity> = emptyList(),
    ) : MusicDao() {
        private val tracksById = linkedMapOf<String, TrackEntity>()
        private val albumsById = linkedMapOf<String, AlbumEntity>()
        private val artistsById = linkedMapOf<String, ArtistEntity>()
        private val rootsByUri = linkedMapOf<String, LibraryRootEntity>()
        private val queue = mutableListOf<QueueItemEntity>()

        init {
            initialTracks.forEach { tracksById[it.id] = it }
            initialRoots.forEach { rootsByUri[it.uri] = it }
        }

        fun trackIds(): List<String> = tracksById.keys.sorted()

        fun root(uri: String): LibraryRootEntity = checkNotNull(rootsByUri[uri])

        fun artistNames(): List<String> = artistsById.values.map { it.name }.sorted()

        fun albumTitles(): List<String> = albumsById.values.map { it.title }.sorted()

        override fun observeTracks(): Flow<List<TrackEntity>> = flowOf(tracksById.values.toList())

        override fun observeAlbums(): Flow<List<AlbumEntity>> = flowOf(albumsById.values.toList())

        override fun observeArtists(): Flow<List<ArtistEntity>> = flowOf(artistsById.values.toList())

        override fun observeLibraryRoots(): Flow<List<LibraryRootEntity>> =
            flowOf(rootsByUri.values.toList())

        override fun observeRecentlyPlayedTracks(limit: Int): Flow<List<TrackEntity>> = flowOf(emptyList())

        override suspend fun getTracksByIds(ids: List<String>): List<TrackEntity> =
            ids.mapNotNull { tracksById[it] }

        override suspend fun getTrackById(id: String): TrackEntity? = tracksById[id]

        override suspend fun getAllTracks(): List<TrackEntity> = tracksById.values.toList()

        override suspend fun getLibraryRoot(uri: String): LibraryRootEntity? = rootsByUri[uri]

        override suspend fun getEnabledLibraryRoots(type: String): List<LibraryRootEntity> =
            rootsByUri.values.filter { it.type == type && it.enabled }

        override suspend fun upsertTracks(tracks: List<TrackEntity>) {
            tracks.forEach { tracksById[it.id] = it }
        }

        override suspend fun upsertLibraryRoot(root: LibraryRootEntity) {
            rootsByUri[root.uri] = root
        }

        override suspend fun markLibraryRootScanned(uri: String, scannedAtMs: Long) {
            val existing = rootsByUri[uri] ?: return
            rootsByUri[uri] = existing.copy(
                lastScannedAtMs = scannedAtMs,
                updatedAtMs = scannedAtMs,
            )
        }

        override suspend fun deleteLibraryRoot(uri: String) {
            rootsByUri.remove(uri)
        }

        override suspend fun deleteTracksOutsideScan(
            sourceType: AudioSourceType,
            sourceLabel: String,
            retainedTrackIds: List<String>,
        ) {
            val retained = retainedTrackIds.toSet()
            val staleIds = tracksById.values
                .filter { it.sourceType == sourceType && it.sourceLabel == sourceLabel && it.id !in retained }
                .map { it.id }
            staleIds.forEach { tracksById.remove(it) }
        }

        override suspend fun upsertAlbums(albums: List<AlbumEntity>) {
            albums.forEach { albumsById[it.id] = it }
        }

        override suspend fun upsertArtists(artists: List<ArtistEntity>) {
            artists.forEach { artistsById[it.id] = it }
        }

        override suspend fun replaceLibraryAggregates(artists: List<ArtistEntity>, albums: List<AlbumEntity>) {
            artistsById.clear()
            albumsById.clear()
            artists.forEach { artistsById[it.id] = it }
            albums.forEach { albumsById[it.id] = it }
        }

        override suspend fun clearAlbums() {
            albumsById.clear()
        }

        override suspend fun clearArtists() {
            artistsById.clear()
        }

        override suspend fun upsertPlaybackState(state: PlaybackStateEntity) = Unit

        override suspend fun getPlaybackState(): PlaybackStateEntity? = null

        override suspend fun getQueue(): List<QueueItemEntity> = queue.toList()

        override suspend fun clearQueue() {
            queue.clear()
        }

        override suspend fun insertQueue(items: List<QueueItemEntity>) {
            queue += items
        }

        override suspend fun getPlaybackHistory(trackId: String): PlaybackHistoryEntity? = null

        override suspend fun upsertPlaybackHistory(item: PlaybackHistoryEntity) = Unit
    }

    private fun track(
        id: String,
        sourceId: String,
        sourceLabel: String,
        title: String,
        artist: String,
        album: String,
        indexedAtMs: Long,
    ): TrackEntity =
        TrackEntity(
            id = id,
            sourceType = AudioSourceType.LOCAL,
            sourceId = sourceId,
            sourceLabel = sourceLabel,
            uri = "content://$sourceId/$id",
            displayPath = "/music/$id.flac",
            fileName = "$id.flac",
            title = title,
            artistId = "artist_$artist",
            artistName = artist,
            albumId = "album_$album",
            albumTitle = album,
            albumArtistName = artist,
            durationMs = 180_000L,
            sizeBytes = 10_000L,
            mimeType = "audio/flac",
            format = AudioFormat.FLAC,
            sampleRateHz = 96_000,
            bitDepth = 24,
            channelCount = 2,
            bitrateKbps = 2_400,
            coverUri = null,
            dateModifiedMs = indexedAtMs,
            indexedAtMs = indexedAtMs,
        )

    private fun libraryRoot(
        uri: String,
        displayName: String,
        enabled: Boolean = true,
        createdAtMs: Long = 1L,
        updatedAtMs: Long = 1L,
        lastScannedAtMs: Long? = null,
    ): LibraryRootEntity =
        LibraryRootEntity(
            uri = uri,
            displayName = displayName,
            type = "SAF",
            enabled = enabled,
            createdAtMs = createdAtMs,
            updatedAtMs = updatedAtMs,
            lastScannedAtMs = lastScannedAtMs,
        )
}
