package com.yuandao.music.data.repository

import android.net.Uri
import com.yuandao.music.data.db.AlbumEntity
import com.yuandao.music.data.db.ArtistEntity
import com.yuandao.music.data.db.LibraryRootEntity
import com.yuandao.music.data.db.MusicDao
import com.yuandao.music.data.db.TrackEntity
import com.yuandao.music.data.db.toAlbum
import com.yuandao.music.data.db.toArtist
import com.yuandao.music.data.db.toTrack
import com.yuandao.music.data.model.Album
import com.yuandao.music.data.model.Artist
import com.yuandao.music.data.model.AudioSourceType
import com.yuandao.music.data.model.LibraryRoot
import com.yuandao.music.data.model.Track
import com.yuandao.music.data.model.normalizedKey
import com.yuandao.music.scanner.AudioScanner
import com.yuandao.music.scanner.SafAudioScanner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class MusicRepository(
    private val dao: MusicDao,
    private val mediaStoreScanner: AudioScanner,
    private val safScanner: SafAudioScanner,
    private val clock: () -> Long = System::currentTimeMillis,
    private val scanPersistedSafRoots: suspend (List<String>) -> List<TrackEntity> = { roots ->
        safScanner.scanRoots(roots.map(Uri::parse))
    },
) {
    val tracks: Flow<List<Track>> =
        dao.observeTracks().map { entities -> entities.map { it.toTrack() } }

    val albums: Flow<List<Album>> =
        dao.observeAlbums().map { entities -> entities.map { it.toAlbum() } }

    val artists: Flow<List<Artist>> =
        dao.observeArtists().map { entities -> entities.map { it.toArtist() } }

    val recentlyPlayedTracks: Flow<List<Track>> =
        dao.observeRecentlyPlayedTracks(20).map { entities -> entities.map { it.toTrack() } }

    val safRoots: Flow<List<LibraryRoot>> =
        dao.observeLibraryRoots().map { entities ->
            entities
                .filter { it.type == SAF_ROOT_TYPE }
                .map { it.toLibraryRoot() }
        }

    suspend fun scanMediaStore(): ScanSummary =
        withContext(Dispatchers.IO) {
            val tracks = mediaStoreScanner.scan()
            persistScan(tracks, reconcileMediaStore = true)
            ScanSummary(source = AudioSourceType.LOCAL, scannedTracks = tracks.size)
        }

    suspend fun scanSafRoots(roots: List<Uri>): ScanSummary =
        withContext(Dispatchers.IO) {
            val tracks = safScanner.scanRoots(roots)
            persistScan(tracks, reconcileMediaStore = false)
            ScanSummary(source = AudioSourceType.LOCAL, scannedTracks = tracks.size)
        }

    suspend fun addSafRoot(uri: Uri, displayName: String? = null) {
        addSafRoot(
            uri = uri.toString(),
            displayName = displayName ?: uri.lastPathSegment,
        )
    }

    suspend fun addSafRoot(uri: String, displayName: String? = null) {
        withContext(Dispatchers.IO) {
            val now = clock()
            val existing = dao.getLibraryRoot(uri)
            dao.upsertLibraryRoot(
                LibraryRootEntity(
                    uri = uri,
                    displayName = displayName.displayNameOrDefault(uri),
                    type = SAF_ROOT_TYPE,
                    enabled = true,
                    createdAtMs = existing?.createdAtMs ?: now,
                    updatedAtMs = now,
                    lastScannedAtMs = existing?.lastScannedAtMs,
                )
            )
        }
    }

    suspend fun rescanSafRoots(): ScanSummary =
        withContext(Dispatchers.IO) {
            val roots = dao.getEnabledLibraryRoots(SAF_ROOT_TYPE)
            if (roots.isEmpty()) {
                return@withContext ScanSummary(source = AudioSourceType.LOCAL, scannedTracks = 0)
            }

            val tracks = scanPersistedSafRoots(roots.map { it.uri })
            persistScan(tracks, reconcileMediaStore = false)
            val scannedAtMs = clock()
            roots.forEach { dao.markLibraryRootScanned(it.uri, scannedAtMs) }
            ScanSummary(source = AudioSourceType.LOCAL, scannedTracks = tracks.size)
        }

    private suspend fun persistScan(tracks: List<TrackEntity>, reconcileMediaStore: Boolean) {
        if (tracks.isEmpty()) return

        dao.upsertTracks(tracks)
        if (reconcileMediaStore) {
            dao.deleteTracksOutsideScan(
                sourceType = AudioSourceType.LOCAL,
                sourceLabel = MEDIASTORE_SOURCE_LABEL,
                retainedTrackIds = tracks.map { it.id },
            )
        }
        rebuildLibraryAggregates(dao.getAllTracks())
    }

    private suspend fun rebuildLibraryAggregates(allTracks: List<TrackEntity>) {
        val artists = allTracks.groupBy { it.artistId }.map { (artistId, artistTracks) ->
            val name = artistTracks.first().artistName
            ArtistEntity(
                id = artistId,
                name = name,
                sortName = name.normalizedKey(),
                sourceType = artistTracks.first().sourceType,
                trackCount = artistTracks.size,
                albumCount = artistTracks.map { it.albumId }.distinct().size,
            )
        }

        val albums = allTracks.groupBy { it.albumId }.map { (albumId, albumTracks) ->
            val first = albumTracks.first()
            AlbumEntity(
                id = albumId,
                title = first.albumTitle,
                artistName = first.albumArtistName,
                sortTitle = first.albumTitle.normalizedKey(),
                sourceType = first.sourceType,
                coverUri = albumTracks.firstNotNullOfOrNull { it.coverUri },
                trackCount = albumTracks.size,
                durationMs = albumTracks.sumOf { it.durationMs },
            )
        }

        dao.replaceLibraryAggregates(artists, albums)
    }

    private fun LibraryRootEntity.toLibraryRoot(): LibraryRoot =
        LibraryRoot(
            uri = uri,
            displayName = displayName,
            type = type,
            enabled = enabled,
            createdAtMs = createdAtMs,
            updatedAtMs = updatedAtMs,
            lastScannedAtMs = lastScannedAtMs,
        )

    private fun String?.displayNameOrDefault(uri: String): String {
        val trimmed = this?.trim().orEmpty()
        if (trimmed.isNotEmpty()) return trimmed

        return uri
            .substringAfterLast('/')
            .substringAfterLast(':')
            .takeIf { it.isNotBlank() }
            ?: DEFAULT_SAF_ROOT_NAME
    }

    private companion object {
        const val MEDIASTORE_SOURCE_LABEL = "MediaStore"
        const val SAF_ROOT_TYPE = "SAF"
        const val DEFAULT_SAF_ROOT_NAME = "Music Folder"
    }
}

data class ScanSummary(
    val source: AudioSourceType,
    val scannedTracks: Int,
)
