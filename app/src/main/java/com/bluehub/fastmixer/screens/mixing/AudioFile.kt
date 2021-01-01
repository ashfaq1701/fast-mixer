package com.bluehub.fastmixer.screens.mixing

import java.io.Serializable

enum class AudioFileType {
    RECORDED, IMPORTED
}

data class AudioFile(val path: String, val numSamples: Int, val fileType: AudioFileType): Serializable