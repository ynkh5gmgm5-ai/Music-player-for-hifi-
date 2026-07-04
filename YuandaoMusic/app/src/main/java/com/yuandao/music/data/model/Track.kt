package com.yuandao.music.data.model

import android.net.Uri

data class Track(
    val id: String,
    val source: AudioSource,
    val uri: Uri,
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
    val coverUri: Uri?,
    val dateModifiedMs: Long,
    val indexedAtMs: Long,
) {
    val qualityLabel: String
        get() {
            val depth = bitDepth?.let { "$it-bit" }
            val rate = sampleRateHz?.let { "${it / 1000.0}".trimEndZero() + "kHz" }
            return listOfNotNull(depth, rate).joinToString(" / ").ifBlank { format.displayName }
        }
}

data class Album(
    val id: String,
    val title: String,
    val artistName: String,
    val sourceType: AudioSourceType,
    val coverUri: Uri?,
    val trackCount: Int,
    val durationMs: Long,
)

data class Artist(
    val id: String,
    val name: String,
    val sourceType: AudioSourceType,
    val trackCount: Int,
    val albumCount: Int,
)

private fun String.trimEndZero(): String =
    trimEnd('0').trimEnd('.')

