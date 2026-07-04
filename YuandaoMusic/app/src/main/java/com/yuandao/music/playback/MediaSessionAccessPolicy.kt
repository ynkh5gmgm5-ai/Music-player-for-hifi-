package com.yuandao.music.playback

internal object MediaSessionAccessPolicy {
    fun allowsController(
        controllerPackageName: String,
        appPackageName: String,
        trusted: Boolean,
    ): Boolean =
        trusted || controllerPackageName == appPackageName
}
