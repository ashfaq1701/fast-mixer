package com.bluehub.fastmixer.common.audio

import com.bluehub.fastmixer.screens.recording.RecordingScreenViewModel


class AudioEngine {
    companion object {
        init {
            System.loadLibrary("audioEngine")
        }

        @JvmStatic external fun create(
            appPathStr: String,
            recordingSessionIdStr: String,
            recordingScreenViewModelPassed: Boolean = false): Boolean

        @JvmStatic external fun delete()

        @JvmStatic external fun startRecording()

        @JvmStatic external fun stopRecording()

        @JvmStatic external fun pauseRecording()

        @JvmStatic external fun startLivePlayback()

        @JvmStatic external fun stopLivePlayback()

        @JvmStatic external fun pauseLivePlayback()

        @JvmStatic external fun startPlayback()

        @JvmStatic external fun stopPlayback()

        @JvmStatic external fun pausePlayback()

        @JvmStatic external fun flushWriteBuffer()

        @JvmStatic external fun restartPlayback()

        @JvmStatic external fun getCurrentMax(): Int

        @JvmStatic external fun resetCurrentMax()
    }
}