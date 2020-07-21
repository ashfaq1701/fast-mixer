package com.bluehub.fastmixer.common.audio

import android.content.res.AssetManager

class AudioEngineProxy {
    fun create(assetManager: AssetManager, appPathStr: String, recordingSessionIdStr: String): Boolean =
        AudioEngine.create(assetManager, appPathStr, recordingSessionIdStr)

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
}