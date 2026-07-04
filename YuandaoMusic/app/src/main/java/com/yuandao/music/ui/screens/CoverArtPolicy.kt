package com.yuandao.music.ui.screens

internal object CoverArtPolicy {
    fun canLoad(coverUri: String?): Boolean = !coverUri.isNullOrBlank()
}
