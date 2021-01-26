package com.bluehub.fastmixer.screens.mixing

class AudioFileStore {
    val audioFiles: MutableList<AudioFile> = mutableListOf()

    fun findAudioFileByPath(filePath: String) : AudioFile? {
        return audioFiles.find { it.path == filePath }
    }
}
