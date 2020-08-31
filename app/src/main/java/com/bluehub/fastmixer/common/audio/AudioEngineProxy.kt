package com.bluehub.fastmixer.common.audio

class AudioEngineProxy {
    companion object {
        private val instance: AudioEngineProxy = AudioEngineProxy()

        public fun getInstance(): AudioEngineProxy {
            return instance
        }
    }

    fun create(appPathStr: String, recordingSessionIdStr: String): Boolean =
        AudioEngine.create(appPathStr, recordingSessionIdStr)

    fun delete() = AudioEngine.delete()

    fun startRecording(audioSessionId: Int) = AudioEngine.startRecording(audioSessionId)

    fun stopRecording() = AudioEngine.stopRecording()

    fun pauseRecording() = AudioEngine.pauseRecording()

    fun startLivePlayback() = AudioEngine.startLivePlayback()

    fun stopLivePlayback() = AudioEngine.stopLivePlayback()

    fun pauseLivePlayback() = AudioEngine.pauseLivePlayback()

    fun startPlayback() = AudioEngine.startPlayback()

    fun stopPlayback() = AudioEngine.stopPlayback()

    fun pausePlayback() = AudioEngine.pausePlayback()

    fun flushWriteBuffer() = AudioEngine.flushWriteBuffer()

    fun restartPlayback() = AudioEngine.restartPlayback()
}