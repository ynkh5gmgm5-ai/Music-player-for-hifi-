package com.yuandao.music.playback

import org.junit.Assert.assertEquals
import org.junit.Test

class PlaybackErrorMessageTest {
    @Test
    fun fileNotFoundMessageSuggestsRescanning() {
        assertEquals(
            "找不到音频文件“慢冷”，请重新扫描曲库。",
            PlaybackErrorMessage.fromErrorCode(2005, "慢冷"),
        )
    }

    @Test
    fun permissionMessageSuggestsSelectingFolderAgain() {
        assertEquals(
            "没有读取音频文件“慢冷”的权限，请重新选择音乐文件夹。",
            PlaybackErrorMessage.fromErrorCode(2006, "慢冷"),
        )
    }

    @Test
    fun decoderFailuresUseFormatGuidance() {
        assertEquals(
            "无法解码音频“慢冷”，请确认文件完整且格式受支持。",
            PlaybackErrorMessage.fromErrorCode(4003, "慢冷"),
        )
    }

    @Test
    fun unknownFailuresDoNotExposeTechnicalExceptionText() {
        assertEquals(
            "播放音频“慢冷”失败，请检查文件并重新扫描。",
            PlaybackErrorMessage.fromErrorCode(1000, "慢冷"),
        )
    }
}
