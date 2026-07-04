package com.yuandao.music.scanner

import android.net.Uri
import com.yuandao.music.data.db.TrackEntity

interface AudioScanner {
    suspend fun scan(): List<TrackEntity>
}

interface SafAudioScanner {
    suspend fun scanRoots(roots: List<Uri>): List<TrackEntity>
}

data class RawAudioFile(
    val sourceId: String,
    val sourceLabel: String,
    val uri: Uri,
    val displayPath: String?,
    val fileName: String,
    val title: String?,
    val artistName: String?,
    val albumTitle: String?,
    val albumArtistName: String?,
    val durationMs: Long?,
    val sizeBytes: Long,
    val mimeType: String?,
    val dateModifiedMs: Long,
)

