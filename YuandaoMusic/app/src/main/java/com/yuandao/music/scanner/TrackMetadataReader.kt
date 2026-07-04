package com.yuandao.music.scanner

import android.content.Context
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.net.Uri
import com.yuandao.music.data.db.TrackEntity
import com.yuandao.music.data.model.AudioFormat
import com.yuandao.music.data.model.AudioSourceType
import com.yuandao.music.data.model.Ids
import com.yuandao.music.data.model.orUnknownAlbum
import com.yuandao.music.data.model.orUnknownArtist
import com.yuandao.music.data.model.orUnknownTitle

class TrackMetadataReader(
    private val context: Context,
    private val coverStore: CoverStore,
) {
    fun readLocal(raw: RawAudioFile): TrackEntity {
        val uriString = raw.uri.toString()
        val trackId = Ids.track(AudioSourceType.LOCAL, raw.sourceId, uriString)

        val retrieverValues = readRetrieverValues(raw.uri)
        val title = raw.title?.takeIf { it.isNotBlank() }
            ?: retrieverValues.title.orUnknownTitle(raw.fileName)
        val artistName = raw.artistName?.takeIf { it.isNotBlank() }
            ?: retrieverValues.artist.orUnknownArtist()
        val albumTitle = raw.albumTitle?.takeIf { it.isNotBlank() }
            ?: retrieverValues.album.orUnknownAlbum()
        val albumArtistName = raw.albumArtistName?.takeIf { it.isNotBlank() } ?: artistName
        val durationMs = raw.durationMs ?: retrieverValues.durationMs ?: 0L
        val mimeType = raw.mimeType ?: retrieverValues.mimeType
        val audioProperties = readAudioProperties(raw.uri)
        val format = AudioFormat.infer(audioProperties.codecMimeType ?: mimeType, raw.fileName)
        val artistId = Ids.artist(AudioSourceType.LOCAL, artistName)
        val albumId = Ids.album(AudioSourceType.LOCAL, albumTitle, albumArtistName)
        val coverUri = coverStore.store(trackId, retrieverValues.cover)

        return TrackEntity(
            id = trackId,
            sourceType = AudioSourceType.LOCAL,
            sourceId = raw.sourceId,
            sourceLabel = raw.sourceLabel,
            uri = uriString,
            displayPath = raw.displayPath,
            fileName = raw.fileName,
            title = title,
            artistId = artistId,
            artistName = artistName,
            albumId = albumId,
            albumTitle = albumTitle,
            albumArtistName = albumArtistName,
            durationMs = durationMs,
            sizeBytes = raw.sizeBytes,
            mimeType = mimeType,
            format = format,
            sampleRateHz = audioProperties.sampleRateHz,
            bitDepth = audioProperties.bitDepth,
            channelCount = audioProperties.channelCount,
            bitrateKbps = audioProperties.bitrateKbps ?: retrieverValues.bitrateKbps,
            coverUri = coverUri,
            dateModifiedMs = raw.dateModifiedMs,
            indexedAtMs = System.currentTimeMillis(),
        )
    }

    private fun readRetrieverValues(uri: Uri): RetrieverValues {
        val retriever = MediaMetadataRetriever()
        return runCatching {
            retriever.setDataSource(context, uri)
            RetrieverValues(
                title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE),
                artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST),
                album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM),
                mimeType = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE),
                durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull(),
                bitrateKbps = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)
                    ?.toIntOrNull()
                    ?.div(1000),
                cover = retriever.embeddedPicture,
            )
        }.getOrDefault(RetrieverValues()).also {
            runCatching { retriever.release() }
        }
    }

    private fun readAudioProperties(uri: Uri): AudioProperties {
        val extractor = MediaExtractor()
        return runCatching {
            extractor.setDataSource(context, uri, null)
            for (index in 0 until extractor.trackCount) {
                val format = extractor.getTrackFormat(index)
                val mime = format.getString(MediaFormat.KEY_MIME).orEmpty()
                if (mime.startsWith("audio/")) {
                    return@runCatching AudioProperties(
                        sampleRateHz = format.getOptionalInt(MediaFormat.KEY_SAMPLE_RATE),
                        channelCount = format.getOptionalInt(MediaFormat.KEY_CHANNEL_COUNT),
                        bitrateKbps = format.getOptionalInt(MediaFormat.KEY_BIT_RATE)?.div(1000),
                        bitDepth = format.getOptionalPcmBitDepth(),
                        codecMimeType = mime,
                    )
                }
            }
            AudioProperties()
        }.getOrDefault(AudioProperties()).also {
            extractor.release()
        }
    }

    private data class RetrieverValues(
        val title: String? = null,
        val artist: String? = null,
        val album: String? = null,
        val mimeType: String? = null,
        val durationMs: Long? = null,
        val bitrateKbps: Int? = null,
        val cover: ByteArray? = null,
    )

    private data class AudioProperties(
        val sampleRateHz: Int? = null,
        val bitDepth: Int? = null,
        val channelCount: Int? = null,
        val bitrateKbps: Int? = null,
        val codecMimeType: String? = null,
    )
}

private fun MediaFormat.getOptionalInt(key: String): Int? =
    if (containsKey(key)) runCatching { getInteger(key) }.getOrNull() else null

private fun MediaFormat.getOptionalPcmBitDepth(): Int? {
    val pcmEncoding = getOptionalInt(MediaFormat.KEY_PCM_ENCODING) ?: return null
    return when (pcmEncoding) {
        android.media.AudioFormat.ENCODING_PCM_8BIT -> 8
        android.media.AudioFormat.ENCODING_PCM_16BIT -> 16
        android.media.AudioFormat.ENCODING_PCM_24BIT_PACKED -> 24
        android.media.AudioFormat.ENCODING_PCM_32BIT -> 32
        android.media.AudioFormat.ENCODING_PCM_FLOAT -> 32
        else -> null
    }
}
