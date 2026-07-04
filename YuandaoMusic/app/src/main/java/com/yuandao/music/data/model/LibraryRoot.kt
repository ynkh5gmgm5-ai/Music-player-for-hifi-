package com.yuandao.music.data.model

data class LibraryRoot(
    val uri: String,
    val displayName: String,
    val type: String,
    val enabled: Boolean,
    val createdAtMs: Long,
    val updatedAtMs: Long,
    val lastScannedAtMs: Long?,
)
