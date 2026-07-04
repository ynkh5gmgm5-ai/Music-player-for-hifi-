package com.yuandao.music.data.model

import java.security.MessageDigest

object Ids {
    fun track(sourceType: AudioSourceType, sourceId: String, uri: String): String =
        "track_" + sha256("${sourceType.name}|$sourceId|$uri")

    fun artist(sourceType: AudioSourceType, name: String): String =
        "artist_" + sha256("${sourceType.name}|${name.normalizedKey()}")

    fun album(sourceType: AudioSourceType, title: String, artistName: String): String =
        "album_" + sha256("${sourceType.name}|${title.normalizedKey()}|${artistName.normalizedKey()}")

    fun sourceId(prefix: String, value: String): String =
        prefix + "_" + sha256(value)

    private fun sha256(value: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(value.toByteArray())
        return digest.take(16).joinToString(separator = "") { byte -> "%02x".format(byte) }
    }
}

fun String?.orUnknownArtist(): String =
    this?.trim()?.takeIf { it.isNotEmpty() } ?: "Unknown Artist"

fun String?.orUnknownAlbum(): String =
    this?.trim()?.takeIf { it.isNotEmpty() } ?: "Unknown Album"

fun String?.orUnknownTitle(fileName: String?): String =
    this?.trim()?.takeIf { it.isNotEmpty() }
        ?: fileName?.substringBeforeLast('.')?.takeIf { it.isNotEmpty() }
        ?: "Unknown Track"

fun String.normalizedKey(): String =
    trim().lowercase().replace(Regex("\\s+"), " ")
