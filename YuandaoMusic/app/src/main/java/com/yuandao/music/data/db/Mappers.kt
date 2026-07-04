package com.yuandao.music.data.db

import android.net.Uri
import com.yuandao.music.data.model.Album
import com.yuandao.music.data.model.Artist
import com.yuandao.music.data.model.Track

fun TrackEntity.toTrack(): Track =
    Track(
        id = id,
        source = source(),
        uri = Uri.parse(uri),
        displayPath = displayPath,
        fileName = fileName,
        title = title,
        artistId = artistId,
        artistName = artistName,
        albumId = albumId,
        albumTitle = albumTitle,
        albumArtistName = albumArtistName,
        durationMs = durationMs,
        sizeBytes = sizeBytes,
        mimeType = mimeType,
        format = format,
        sampleRateHz = sampleRateHz,
        bitDepth = bitDepth,
        channelCount = channelCount,
        bitrateKbps = bitrateKbps,
        coverUri = coverUri?.let(Uri::parse),
        dateModifiedMs = dateModifiedMs,
        indexedAtMs = indexedAtMs,
    )

fun AlbumEntity.toAlbum(): Album =
    Album(
        id = id,
        title = title,
        artistName = artistName,
        sourceType = sourceType,
        coverUri = coverUri?.let(Uri::parse),
        trackCount = trackCount,
        durationMs = durationMs,
    )

fun ArtistEntity.toArtist(): Artist =
    Artist(
        id = id,
        name = name,
        sourceType = sourceType,
        trackCount = trackCount,
        albumCount = albumCount,
    )

