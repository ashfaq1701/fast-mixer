package com.bluehub.fastmixer.screens.mixing

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MixingRepository(val mixingEngineProxy: MixingEngineProxy) {
    fun createMixingEngine() {
        mixingEngineProxy.create()
    }

    suspend fun addFile(filePath: String) = withContext(Dispatchers.IO){
        mixingEngineProxy.addFile(filePath)
    }

    fun readSamples(filePath: String, numSamples: Int): Array<Float> = mixingEngineProxy.readSamples(filePath, numSamples)

    fun getTotalSamples(filePath: String): Int = mixingEngineProxy.getTotalSamples(filePath)

    suspend fun deleteFile(filePath: String) = withContext(Dispatchers.IO) {
        mixingEngineProxy.deleteFile(filePath)
    }

    fun deleteMixingEngine() = mixingEngineProxy.delete()
}