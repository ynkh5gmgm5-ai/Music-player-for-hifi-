package com.yuandao.music.ui.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test

class YuandaoThemePolicyTest {
    @Test
    fun rootSurfaceUsesDarkBackgroundAndWhiteContent() {
        assertEquals(YuandaoBackground, YuandaoThemePolicy.rootBackgroundColor)
        assertEquals(Color.White, YuandaoThemePolicy.rootContentColor)
    }
}
