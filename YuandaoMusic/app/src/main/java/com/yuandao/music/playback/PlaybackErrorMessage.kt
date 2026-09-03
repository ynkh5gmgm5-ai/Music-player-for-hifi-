package com.yuandao.music.playback

import androidx.media3.common.PlaybackException
import com.yuandao.music.data.model.AudioFormat

object PlaybackErrorMessage {
    const val noLocalTracks = "当前没有可播放的本地音乐，请先扫描或添加文件夹。"
    const val noPlayableTracks = "曲库中没有当前支持播放的格式，请检查音乐文件。"
    const val audioFocusUnavailable = "系统音频焦点暂不可用，请稍后重试。"

    fun unsupportedTrack(title: String, format: AudioFormat): String =
        "${format.displayName} 暂不支持播放：${title.ifBlank { "未命名曲目" }}"

    fun fromPlaybackException(error: PlaybackException, trackTitle: String?): String {
        return fromErrorCode(error.errorCode, trackTitle)
    }

    fun fromErrorCode(errorCode: Int, trackTitle: String?): String {
        val track = trackTitle
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?.let { "“$it”" }
            ?: "当前曲目"

        return when (errorCode) {
            PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND ->
                "找不到音频文件${track}，请重新扫描曲库。"

            PlaybackException.ERROR_CODE_IO_NO_PERMISSION ->
                "没有读取音频文件${track}的权限，请重新选择音乐文件夹。"

            PlaybackException.ERROR_CODE_IO_READ_POSITION_OUT_OF_RANGE ->
                "音频文件${track}可能不完整，请重新获取文件。"

            PlaybackException.ERROR_CODE_PARSING_CONTAINER_MALFORMED,
            PlaybackException.ERROR_CODE_PARSING_CONTAINER_UNSUPPORTED,
            PlaybackException.ERROR_CODE_DECODER_INIT_FAILED,
            PlaybackException.ERROR_CODE_DECODER_QUERY_FAILED,
            PlaybackException.ERROR_CODE_DECODING_FAILED,
            PlaybackException.ERROR_CODE_DECODING_FORMAT_EXCEEDS_CAPABILITIES,
            PlaybackException.ERROR_CODE_DECODING_FORMAT_UNSUPPORTED,
            -> "无法解码音频${track}，请确认文件完整且格式受支持。"

            PlaybackException.ERROR_CODE_AUDIO_TRACK_INIT_FAILED,
            PlaybackException.ERROR_CODE_AUDIO_TRACK_WRITE_FAILED,
            PlaybackException.ERROR_CODE_AUDIO_TRACK_OFFLOAD_INIT_FAILED,
            PlaybackException.ERROR_CODE_AUDIO_TRACK_OFFLOAD_WRITE_FAILED,
            -> "系统音频输出失败，请检查输出设备后重试。"

            PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED,
            PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT,
            PlaybackException.ERROR_CODE_TIMEOUT,
            -> "读取音频${track}超时，请检查文件是否仍可访问。"

            else -> "播放音频${track}失败，请检查文件并重新扫描。"
        }
    }
}
