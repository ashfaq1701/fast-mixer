package com.bluehub.fastmixer.screens.mixing

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MixingRepository @Inject constructor(val mixingEngineProxy: MixingEngineProxy) {
    fun createMixingEngine() {
        mixingEngineProxy.create()
    }

    suspend fun addFile(filePath: String) = withContext(Dispatchers.IO){
        mixingEngineProxy.addFile(filePath)
    }

    fun readSamples(filePath: String, countPoints: Int): Array<Float> = mixingEngineProxy.readSamples(filePath, countPoints)

    fun getTotalSamples(filePath: String): Int = mixingEngineProxy.getTotalSamples(filePath)

    fun deleteFile(filePath: String) {
        mixingEngineProxy.deleteFile(filePath)
    }

    fun startPlayback() {
        mixingEngineProxy.startPlayback()
    }

    fun pausePlayback() {
        mixingEngineProxy.pausePlayback()
    }

    fun deleteMixingEngine() = mixingEngineProxy.delete()

    fun loadFiles(fileArr: List<String>) {
        mixingEngineProxy.addSources(fileArr.toTypedArray())
    }

    fun clearSources() {
        mixingEngineProxy.clearPlayerSources()
    }
}
