package com.yuandao.music.scanner

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.yuandao.music.data.db.TrackEntity
import com.yuandao.music.data.model.AudioFormat
import com.yuandao.music.data.model.Ids

class DocumentTreeAudioScanner(
    private val context: Context,
    private val metadataReader: TrackMetadataReader,
) : SafAudioScanner {
    override suspend fun scanRoots(roots: List<Uri>): List<TrackEntity> {
        val tracks = mutableListOf<TrackEntity>()
        roots.forEach { rootUri ->
            DocumentFile.fromTreeUri(context, rootUri)?.let { root ->
                root.walkAudioFiles().forEach { file ->
                    val uri = file.uri
                    val fileName = file.name ?: return@forEach
                    val raw = RawAudioFile(
                        sourceId = Ids.sourceId("saf", uri.toString()),
                        sourceLabel = "SAF Folder",
                        uri = uri,
                        displayPath = file.uri.toString(),
                        fileName = fileName,
                        title = null,
                        artistName = null,
                        albumTitle = null,
                        albumArtistName = null,
                        durationMs = null,
                        sizeBytes = file.length(),
                        mimeType = file.type,
                        dateModifiedMs = file.lastModified(),
                    )
                    tracks += metadataReader.readLocal(raw)
                }
            }
        }
        return tracks
    }
}

private fun DocumentFile.walkAudioFiles(): Sequence<DocumentFile> = sequence {
    if (isFile && isSupportedAudio()) {
        yield(this@walkAudioFiles)
    } else if (isDirectory) {
        listFiles().forEach { child ->
            yieldAll(child.walkAudioFiles())
        }
    }
}

private fun DocumentFile.isSupportedAudio(): Boolean {
    val name = name ?: return false
    val format = AudioFormat.infer(type, name)
    return format != AudioFormat.UNKNOWN && format != AudioFormat.CUE
}
