package com.bluehub.fastmixer.screens.mixing

import com.bluehub.fastmixer.screens.recording.RecordingEngineProxy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MixingRepository(val mixingEngineProxy: MixingEngineProxy) {
    fun createMixingEngine() {
        mixingEngineProxy.create()
    }

    suspend fun addFile(filePath: String) = withContext(Dispatchers.IO){
        mixingEngineProxy.addFile(filePath)
    }

    fun readSamples(index: Int, numSamples: Int): Array<Float> = mixingEngineProxy.readSamples(index, numSamples)

    suspend fun deleteFile(index: Int) = withContext(Dispatchers.IO) {
        mixingEngineProxy.deleteFile(index)
    }
}