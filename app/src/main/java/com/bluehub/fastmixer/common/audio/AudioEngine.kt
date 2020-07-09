package com.bluehub.fastmixer.common.audio

class AudioEngine {
    companion object {
        init {
            System.loadLibrary("audioEngine")
        }

        @JvmStatic external fun create(): Boolean
    }
}