package com.bluehub.fastmixer.screens.mixing

import com.bluehub.fastmixer.common.models.AudioFileUiState
import dagger.hilt.android.scopes.ActivityRetainedScoped
import javax.inject.Inject

@ActivityRetainedScoped
class AudioFileStore @Inject constructor() {
    val audioFiles: MutableList<AudioFileUiState> = mutableListOf()

    fun findAudioFileByPath(filePath: String) : AudioFileUiState? {
        return audioFiles.find { it.path == filePath }
    }
}
