package com.yuandao.music.ui

import java.io.FileNotFoundException
import org.junit.Assert.assertEquals
import org.junit.Test

class ScanMessageFormatterTest {
    @Test
    fun indexedMessageDescribesSuccessfulScan() {
        assertEquals(
            "已收录 3 首音频：本机曲库",
            ScanMessageFormatter.indexed("本机曲库", 3),
        )
    }

    @Test
    fun emptyScanSuggestsCheckingFolderAndFormat() {
        assertEquals(
            "选定文件夹中没有发现可识别的音频文件，请检查文件夹和文件格式。",
            ScanMessageFormatter.indexed("选定文件夹", 0),
        )
    }

    @Test
    fun permissionFailureSuggestsRegrantingFolderAccess() {
        assertEquals(
            "无法读取选定文件夹，请重新授予文件夹访问权限后重试。",
            ScanMessageFormatter.failed("选定文件夹", SecurityException()),
        )
    }

    @Test
    fun missingFolderFailureSuggestsSelectingAnotherFolder() {
        assertEquals(
            "找不到已保存文件夹，请重新选择有效的音乐文件夹。",
            ScanMessageFormatter.failed("已保存文件夹", FileNotFoundException()),
        )
    }

    @Test
    fun unknownFailureUsesSafeGenericMessage() {
        assertEquals(
            "扫描本机曲库失败，请检查访问权限和文件夹后重试。",
            ScanMessageFormatter.failed("本机曲库", IllegalStateException("internal details")),
        )
    }
}
