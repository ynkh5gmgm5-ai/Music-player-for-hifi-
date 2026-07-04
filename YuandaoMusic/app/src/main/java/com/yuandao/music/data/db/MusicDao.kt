package com.yuandao.music.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.yuandao.music.data.model.AudioSourceType
import kotlinx.coroutines.flow.Flow

@Dao
abstract class MusicDao {
    @Query("SELECT * FROM tracks ORDER BY dateModifiedMs DESC, title COLLATE NOCASE ASC")
    abstract fun observeTracks(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM albums ORDER BY sortTitle COLLATE NOCASE ASC")
    abstract fun observeAlbums(): Flow<List<AlbumEntity>>

    @Query("SELECT * FROM artists ORDER BY sortName COLLATE NOCASE ASC")
    abstract fun observeArtists(): Flow<List<ArtistEntity>>

    @Query("SELECT * FROM tracks WHERE id IN (:ids)")
    abstract suspend fun getTracksByIds(ids: List<String>): List<TrackEntity>

    @Query("SELECT * FROM tracks WHERE id = :id LIMIT 1")
    abstract suspend fun getTrackById(id: String): TrackEntity?

    @Query("SELECT * FROM tracks")
    abstract suspend fun getAllTracks(): List<TrackEntity>

    @Query("SELECT * FROM library_roots ORDER BY createdAtMs ASC")
    abstract fun observeLibraryRoots(): Flow<List<LibraryRootEntity>>

    @Query("SELECT * FROM library_roots WHERE type = :type AND enabled = 1 ORDER BY createdAtMs ASC")
    abstract suspend fun getEnabledLibraryRoots(type: String): List<LibraryRootEntity>

    @Query("SELECT * FROM library_roots WHERE uri = :uri LIMIT 1")
    abstract suspend fun getLibraryRoot(uri: String): LibraryRootEntity?

    @Query(
        """
        SELECT tracks.* FROM playback_history
        INNER JOIN tracks ON playback_history.trackId = tracks.id
        ORDER BY playback_history.lastPlayedAtMs DESC
        LIMIT :limit
        """
    )
    abstract fun observeRecentlyPlayedTracks(limit: Int): Flow<List<TrackEntity>>

    @Upsert
    abstract suspend fun upsertTracks(tracks: List<TrackEntity>)

    @Upsert
    abstract suspend fun upsertLibraryRoot(root: LibraryRootEntity)

    @Query("UPDATE library_roots SET lastScannedAtMs = :scannedAtMs, updatedAtMs = :scannedAtMs WHERE uri = :uri")
    abstract suspend fun markLibraryRootScanned(uri: String, scannedAtMs: Long)

    @Query("DELETE FROM library_roots WHERE uri = :uri")
    abstract suspend fun deleteLibraryRoot(uri: String)

    @Query(
        """
        DELETE FROM tracks
        WHERE sourceType = :sourceType
        AND sourceLabel = :sourceLabel
        AND id NOT IN (:retainedTrackIds)
        """
    )
    abstract suspend fun deleteTracksOutsideScan(
        sourceType: AudioSourceType,
        sourceLabel: String,
        retainedTrackIds: List<String>,
    )

    @Upsert
    abstract suspend fun upsertAlbums(albums: List<AlbumEntity>)

    @Upsert
    abstract suspend fun upsertArtists(artists: List<ArtistEntity>)

    @Query("DELETE FROM albums")
    protected abstract suspend fun clearAlbums()

    @Query("DELETE FROM artists")
    protected abstract suspend fun clearArtists()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsertPlaybackState(state: PlaybackStateEntity)

    @Query("SELECT * FROM playback_state WHERE id = 0 LIMIT 1")
    abstract suspend fun getPlaybackState(): PlaybackStateEntity?

    @Query("SELECT * FROM playback_queue ORDER BY position ASC")
    abstract suspend fun getQueue(): List<QueueItemEntity>

    @Query("DELETE FROM playback_queue")
    abstract suspend fun clearQueue()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertQueue(items: List<QueueItemEntity>)

    @Query("SELECT * FROM playback_history WHERE trackId = :trackId LIMIT 1")
    protected abstract suspend fun getPlaybackHistory(trackId: String): PlaybackHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun upsertPlaybackHistory(item: PlaybackHistoryEntity)

    @Transaction
    open suspend fun replaceQueue(state: PlaybackStateEntity, items: List<QueueItemEntity>) {
        upsertPlaybackState(state)
        clearQueue()
        if (items.isNotEmpty()) {
            insertQueue(items)
        }
    }

    @Transaction
    open suspend fun replaceLibraryAggregates(artists: List<ArtistEntity>, albums: List<AlbumEntity>) {
        clearArtists()
        clearAlbums()
        if (artists.isNotEmpty()) {
            upsertArtists(artists)
        }
        if (albums.isNotEmpty()) {
            upsertAlbums(albums)
        }
    }

    @Transaction
    open suspend fun recordPlayback(trackId: String, playedAtMs: Long) {
        val existing = getPlaybackHistory(trackId)
        upsertPlaybackHistory(
            PlaybackHistoryEntity(
                trackId = trackId,
                firstPlayedAtMs = existing?.firstPlayedAtMs ?: playedAtMs,
                lastPlayedAtMs = playedAtMs,
                playCount = (existing?.playCount ?: 0) + 1,
            )
        )
    }
}
