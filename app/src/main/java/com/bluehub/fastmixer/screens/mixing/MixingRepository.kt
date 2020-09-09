package com.bluehub.fastmixer.screens.mixing

import com.bluehub.fastmixer.screens.recording.RecordingEngineProxy

class MixingRepository(val mixingEngineProxy: MixingEngineProxy) {
    fun createMixingEngine() {
        mixingEngineProxy.create()
    }
}