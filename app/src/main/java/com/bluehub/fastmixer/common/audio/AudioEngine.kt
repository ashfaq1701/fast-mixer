package com.bluehub.fastmixer.common.audio

import android.content.res.AssetManager

class AudioEngine {
    companion object {
        init {
            System.loadLibrary("audioEngine")
        }

        @JvmStatic external fun create(assetManager: AssetManager, appPathStr: String, recordingSessionIdStr: String): Boolean

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
    }
}