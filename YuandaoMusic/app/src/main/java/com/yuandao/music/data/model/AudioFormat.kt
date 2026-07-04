package com.yuandao.music.data.model

enum class AudioFormat(
    val displayName: String,
    val isHiResCandidate: Boolean,
    val isFirstPassPlayable: Boolean,
) {
    MP3("MP3", false, true),
    AAC("AAC", false, true),
    FLAC("FLAC", true, true),
    WAV("WAV", true, true),
    ALAC("ALAC", true, true),
    OGG("OGG", false, true),
    OPUS("OPUS", false, true),
    APE("APE", true, false),
    DSD("DSD", true, false),
    CUE("CUE", true, false),
    UNKNOWN("Unknown", false, false);

    companion object {
        fun infer(mimeType: String?, fileName: String?): AudioFormat {
            val lowerMime = mimeType.orEmpty().lowercase()
            val extension = fileName?.substringAfterLast('.', missingDelimiterValue = "")
                ?.lowercase()
                .orEmpty()

            return when {
                lowerMime.contains("flac") || extension == "flac" -> FLAC
                lowerMime.contains("wav") || extension == "wav" -> WAV
                lowerMime.contains("alac") || lowerMime.contains("lossless") -> ALAC
                lowerMime.contains("mpeg") || extension == "mp3" -> MP3
                lowerMime.contains("aac") || lowerMime.contains("mp4a") || extension == "aac" -> AAC
                lowerMime.contains("ogg") || extension == "ogg" -> OGG
                lowerMime.contains("opus") || extension == "opus" -> OPUS
                extension == "ape" -> APE
                extension == "dsf" || extension == "dff" || extension == "dsd" -> DSD
                extension == "cue" -> CUE
                extension == "m4a" || extension == "mp4" -> AAC
                else -> UNKNOWN
            }
        }
    }
}
