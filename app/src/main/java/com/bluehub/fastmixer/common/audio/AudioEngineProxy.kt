package com.bluehub.fastmixer.common.audio

class AudioEngineProxy {
    fun create(): Boolean = AudioEngine.create()

    fun delete() = AudioEngine.delete()

    fun startRecording() = AudioEngine.startRecording()

    fun stopRecording() = AudioEngine.stopRecording()

    fun pauseRecording() = AudioEngine.pauseRecording()
}