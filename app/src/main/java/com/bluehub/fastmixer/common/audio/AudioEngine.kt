package com.bluehub.fastmixer.common.audio

class AudioEngine {
    companion object {
        init {
            System.loadLibrary("audioEngine")
        }

        @JvmStatic external fun create(appPathStr: String, recordingSessionIdStr: String): Boolean

        @JvmStatic external fun delete()

        @JvmStatic external fun startRecording()

        @JvmStatic external fun stopRecording()

        @JvmStatic external fun pauseRecording()
    }
}