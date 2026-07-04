package com.yuandao.music.playback

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MediaSessionAccessPolicyTest {
    @Test
    fun allowsTrustedController() {
        assertTrue(
            MediaSessionAccessPolicy.allowsController(
                controllerPackageName = "com.android.systemui",
                appPackageName = "com.yuandao.music",
                trusted = true,
            )
        )
    }

    @Test
    fun allowsOwnAppController() {
        assertTrue(
            MediaSessionAccessPolicy.allowsController(
                controllerPackageName = "com.yuandao.music",
                appPackageName = "com.yuandao.music",
                trusted = false,
            )
        )
    }

    @Test
    fun rejectsUntrustedExternalController() {
        assertFalse(
            MediaSessionAccessPolicy.allowsController(
                controllerPackageName = "com.example.remote",
                appPackageName = "com.yuandao.music",
                trusted = false,
            )
        )
    }
}
