package com.bluehub.fastmixer.common.audio

import com.bluehub.fastmixer.screens.recording.RecordingScreenViewModel

class AudioEngineProxy {
    companion object {
        private val instance: AudioEngineProxy = AudioEngineProxy()

        public fun getInstance(): AudioEngineProxy {
            return instance
        }
    }

    fun create(appPathStr: String,
               recordingSessionIdStr: String,
               recordingScreenViewModelPassed: Boolean = false): Boolean =
        AudioEngine.create(appPathStr, recordingSessionIdStr, recordingScreenViewModelPassed)

    fun delete() = AudioEngine.delete()

    fun startRecording() = AudioEngine.startRecording()

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

    fun getCurrentMax(): Int = AudioEngine.getCurrentMax()

    fun resetCurrentMax() = AudioEngine.resetCurrentMax()

    fun getTotalRecordedFrames() = AudioEngine.getTotalRecordedFrames()

    fun getCurrentPlaybackProgress() = AudioEngine.getCurrentPlaybackProgress()

    fun setPlayHead(position: Int) = AudioEngine.setPlayHead(position)

    fun getDurationInSeconds(): Int = AudioEngine.getDurationInSeconds()
}