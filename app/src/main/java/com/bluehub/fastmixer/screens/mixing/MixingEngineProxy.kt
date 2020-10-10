package com.bluehub.fastmixer.screens.mixing

import com.bluehub.fastmixer.screens.recording.RecordingEngineProxy

class MixingEngineProxy {
    companion object {
        private val INSTANCE: MixingEngineProxy =
            MixingEngineProxy()

        public fun getInstance(): MixingEngineProxy {
            return INSTANCE
        }
    }

    fun create() = MixingEngine.create()

    fun addFile(filePath: String) = MixingEngine.addFile(filePath)

    fun readSamples(index: Int, numSamples: Int) = MixingEngine.readSamples(index, numSamples)

    fun deleteFile(index: Int) = MixingEngine.deleteFile(index)
}