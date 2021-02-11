package com.bluehub.fastmixer.screens.mixing

import com.bluehub.fastmixer.common.models.AudioFileUiState

class AudioFileStore {
    val audioFiles: MutableList<AudioFileUiState> = mutableListOf()

    fun findAudioFileByPath(filePath: String) : AudioFileUiState? {
        return audioFiles.find { it.path == filePath }
    }
}
