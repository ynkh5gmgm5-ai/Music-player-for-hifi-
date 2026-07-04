package com.yuandao.music.scanner

import android.content.Context
import android.net.Uri
import java.io.File

class CoverStore(private val context: Context) {
    private val coverDir: File by lazy {
        File(context.cacheDir, "covers").apply { mkdirs() }
    }

    fun store(trackId: String, bytes: ByteArray?): String? {
        if (bytes == null || bytes.isEmpty()) return null
        val target = File(coverDir, "$trackId.jpg")
        return runCatching {
            target.outputStream().use { it.write(bytes) }
            Uri.fromFile(target).toString()
        }.getOrNull()
    }
}

