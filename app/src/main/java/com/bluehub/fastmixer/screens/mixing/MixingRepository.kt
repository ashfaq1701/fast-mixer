package com.bluehub.fastmixer.screens.mixing

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MixingRepository(val mixingEngineProxy: MixingEngineProxy) {
    fun createMixingEngine() {
        mixingEngineProxy.create()
    }

    suspend fun addFile(filePath: String, uuid: String) = withContext(Dispatchers.IO){
        mixingEngineProxy.addFile(filePath, uuid)
    }

    fun readSamples(uuid: String, numSamples: Int): Array<Float> = mixingEngineProxy.readSamples(uuid, numSamples)

    suspend fun deleteFile(uuid: String) = withContext(Dispatchers.IO) {
        mixingEngineProxy.deleteFile(uuid)
    }

    fun getTotalSamples(uuid: String): Int = mixingEngineProxy.getTotalSamples(uuid)
}