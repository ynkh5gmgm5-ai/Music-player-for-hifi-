package com.yuandao.music.ui.screens

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CoverArtPolicyTest {
    @Test
    fun loadsOnlyWhenCoverUriIsPresent() {
        assertFalse(CoverArtPolicy.canLoad(null))
        assertFalse(CoverArtPolicy.canLoad(""))
        assertFalse(CoverArtPolicy.canLoad("   "))
        assertTrue(CoverArtPolicy.canLoad("content://covers/track"))
    }
}
