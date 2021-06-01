package com.bluehub.mixi.screens.mixing

import com.bluehub.mixi.common.models.AudioFileUiState
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioFileStore @Inject constructor() {
    val audioFiles: MutableList<AudioFileUiState> = mutableListOf()

    fun findAudioFileByPath(filePath: String) : AudioFileUiState? {
        return audioFiles.find { it.path == filePath }
    }

    fun clearSources() {
        audioFiles.clear()
    }
}
