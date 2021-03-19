package com.bluehub.fastmixer.screens.mixing

import com.bluehub.fastmixer.common.models.AudioFileUiState
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioFileStore @Inject constructor() {
    val audioFiles: MutableList<AudioFileUiState> = mutableListOf()

    fun findAudioFileByPath(filePath: String) : AudioFileUiState? {
        return audioFiles.find { it.path == filePath }
    }
}
