package com.bluehub.fastmixer.common.audio

class AudioEngineProxy {
    fun create(): Boolean = AudioEngine.create()
}