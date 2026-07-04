package com.yuandao.music.scanner

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.os.Build
import android.provider.MediaStore
import com.yuandao.music.data.db.TrackEntity
import com.yuandao.music.data.model.Ids

class MediaStoreAudioScanner(
    private val context: Context,
    private val metadataReader: TrackMetadataReader,
) : AudioScanner {
    override suspend fun scan(): List<TrackEntity> {
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = buildProjection()
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.DATE_MODIFIED} DESC"
        val tracks = mutableListOf<TrackEntity>()

        context.contentResolver.query(collection, projection, selection, null, sortOrder)?.use { cursor ->
            while (cursor.moveToNext()) {
                val raw = cursor.toRawAudioFile() ?: continue
                tracks += metadataReader.readLocal(raw)
            }
        }

        return tracks
    }

    private fun buildProjection(): Array<String> =
        buildList {
            add(MediaStore.Audio.Media._ID)
            add(MediaStore.Audio.Media.DISPLAY_NAME)
            add(MediaStore.Audio.Media.TITLE)
            add(MediaStore.Audio.Media.ARTIST)
            add(MediaStore.Audio.Media.ALBUM)
            add(MediaStore.Audio.Media.DURATION)
            add(MediaStore.Audio.Media.SIZE)
            add(MediaStore.Audio.Media.MIME_TYPE)
            add(MediaStore.Audio.Media.DATE_MODIFIED)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                add(MediaStore.Audio.Media.RELATIVE_PATH)
            }
            @Suppress("DEPRECATION")
            add(MediaStore.Audio.Media.DATA)
        }.toTypedArray()

    private fun Cursor.toRawAudioFile(): RawAudioFile? {
        val id = getLong(MediaStore.Audio.Media._ID) ?: return null
        val uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
        val fileName = getStringValue(MediaStore.Audio.Media.DISPLAY_NAME) ?: "track_$id"
        val dateModifiedMs = (getLong(MediaStore.Audio.Media.DATE_MODIFIED) ?: 0L) * 1000
        @Suppress("DEPRECATION")
        val absolutePath = getStringValue(MediaStore.Audio.Media.DATA)
        val path = absolutePath ?: if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            getStringValue(MediaStore.Audio.Media.RELATIVE_PATH)?.let { "$it$fileName" }
        } else {
            null
        }

        return RawAudioFile(
            sourceId = Ids.sourceId("mediastore", id.toString()),
            sourceLabel = "MediaStore",
            uri = uri,
            displayPath = path,
            fileName = fileName,
            title = getStringValue(MediaStore.Audio.Media.TITLE),
            artistName = getStringValue(MediaStore.Audio.Media.ARTIST),
            albumTitle = getStringValue(MediaStore.Audio.Media.ALBUM),
            albumArtistName = null,
            durationMs = getLong(MediaStore.Audio.Media.DURATION),
            sizeBytes = getLong(MediaStore.Audio.Media.SIZE) ?: 0L,
            mimeType = getStringValue(MediaStore.Audio.Media.MIME_TYPE),
            dateModifiedMs = dateModifiedMs,
        )
    }
}

private fun Cursor.getStringValue(columnName: String): String? {
    val index = getColumnIndex(columnName)
    return if (index >= 0 && !isNull(index)) getString(index) else null
}

private fun Cursor.getLong(columnName: String): Long? {
    val index = getColumnIndex(columnName)
    return if (index >= 0 && !isNull(index)) getLong(index) else null
}
