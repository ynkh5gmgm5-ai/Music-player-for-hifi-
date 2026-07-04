package com.yuandao.music.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.yuandao.music.data.model.AudioFormat
import com.yuandao.music.data.model.AudioSource
import com.yuandao.music.data.model.AudioSourceType

@Entity(
    tableName = "tracks",
    indices = [
        Index("sourceType"),
        Index("sourceId"),
        Index("albumId"),
        Index("artistId"),
        Index("format"),
    ],
)
data class TrackEntity(
    @PrimaryKey val id: String,
    val sourceType: AudioSourceType,
    val sourceId: String,
    val sourceLabel: String,
    val uri: String,
    val displayPath: String?,
    val fileName: String,
    val title: String,
    val artistId: String,
    val artistName: String,
    val albumId: String,
    val albumTitle: String,
    val albumArtistName: String,
    val durationMs: Long,
    val sizeBytes: Long,
    val mimeType: String?,
    val format: AudioFormat,
    val sampleRateHz: Int?,
    val bitDepth: Int?,
    val channelCount: Int?,
    val bitrateKbps: Int?,
    val coverUri: String?,
    val dateModifiedMs: Long,
    val indexedAtMs: Long,
)

@Entity(tableName = "albums", indices = [Index("sourceType"), Index("sortTitle")])
data class AlbumEntity(
    @PrimaryKey val id: String,
    val title: String,
    val artistName: String,
    val sortTitle: String,
    val sourceType: AudioSourceType,
    val coverUri: String?,
    val trackCount: Int,
    val durationMs: Long,
)

@Entity(tableName = "artists", indices = [Index("sourceType"), Index("sortName")])
data class ArtistEntity(
    @PrimaryKey val id: String,
    val name: String,
    val sortName: String,
    val sourceType: AudioSourceType,
    val trackCount: Int,
    val albumCount: Int,
)

@Entity(tableName = "playback_state")
data class PlaybackStateEntity(
    @PrimaryKey val id: Int = 0,
    val currentTrackId: String?,
    val currentIndex: Int,
    val positionMs: Long,
    val repeatMode: String,
    val shuffled: Boolean,
    val updatedAtMs: Long,
)

@Entity(tableName = "playback_queue")
data class QueueItemEntity(
    @PrimaryKey val position: Int,
    val trackId: String,
)

@Entity(
    tableName = "playback_history",
    indices = [
        Index("lastPlayedAtMs"),
        Index("playCount"),
    ],
)
data class PlaybackHistoryEntity(
    @PrimaryKey val trackId: String,
    val firstPlayedAtMs: Long,
    val lastPlayedAtMs: Long,
    val playCount: Int,
)

@Entity(
    tableName = "library_roots",
    indices = [
        Index("type"),
        Index("enabled"),
    ],
)
data class LibraryRootEntity(
    @PrimaryKey val uri: String,
    val displayName: String,
    val type: String,
    val enabled: Boolean,
    val createdAtMs: Long,
    val updatedAtMs: Long,
    val lastScannedAtMs: Long?,
)

fun TrackEntity.source(): AudioSource =
    AudioSource(sourceType, sourceId, sourceLabel)
