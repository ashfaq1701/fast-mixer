package com.bluehub.fastmixer.common.audio


class AudioEngine {
    companion object {
        init {
            System.loadLibrary("audioEngine")
        }

        @JvmStatic external fun create(appPathStr: String, recordingSessionIdStr: String): Boolean

        @JvmStatic external fun delete()

        @JvmStatic external fun startRecording(): Int

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
    }
}