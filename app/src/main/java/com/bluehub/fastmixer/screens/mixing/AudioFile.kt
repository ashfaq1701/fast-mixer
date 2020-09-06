package com.bluehub.fastmixer.screens.mixing

enum class AudioFileType {
    RECORDED, IMPORTED
}

data class AudioFile(val path: String, val fileType: AudioFileType)