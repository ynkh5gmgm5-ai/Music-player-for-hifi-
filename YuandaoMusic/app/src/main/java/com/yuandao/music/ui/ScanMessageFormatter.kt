package com.yuandao.music.ui

import java.io.FileNotFoundException

object ScanMessageFormatter {
    const val mediaStoreStarted = "正在扫描本机音乐..."
    const val selectedFolderStarted = "正在扫描选定文件夹..."
    const val savedFoldersStarted = "正在重新扫描已保存文件夹..."

    fun indexed(sourceLabel: String, trackCount: Int): String =
        if (trackCount == 0) {
            "${sourceLabel}中没有发现可识别的音频文件，请检查文件夹和文件格式。"
        } else {
            "已收录 $trackCount 首音频：$sourceLabel"
        }

    fun failed(sourceLabel: String, error: Throwable): String = when (error) {
        is SecurityException -> "无法读取${sourceLabel}，请重新授予文件夹访问权限后重试。"
        is FileNotFoundException -> "找不到${sourceLabel}，请重新选择有效的音乐文件夹。"
        else -> "扫描${sourceLabel}失败，请检查访问权限和文件夹后重试。"
    }
}
