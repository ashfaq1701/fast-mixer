package com.bluehub.fastmixer.screens.mixing

import com.bluehub.fastmixer.audio.MixingEngineProxy
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

    fun getTotalSampleFrames() = mixingEngineProxy.getTotalSampleFrames()

    fun getCurrentPlaybackProgress() = mixingEngineProxy.getCurrentPlaybackProgress()

    fun setPlayerHead(playHead: Int) = mixingEngineProxy.setPlayerHead(playHead)

    fun setSourcePlayHead(filePath: String, playHead: Int) = mixingEngineProxy.setSourcePlayHead(filePath, playHead)

    fun gainSourceByDb(filePath: String, db: Float) = mixingEngineProxy.gainSourceByDb(filePath, db)

    fun applySourceTransformation(filePath: String) = mixingEngineProxy.applySourceTransformation(filePath)

    fun clearSourceTransformation(filePath: String) = mixingEngineProxy.clearSourceTransformation(filePath)

    fun setSourceBounds(filePath: String, start: Int, end: Int) = mixingEngineProxy.setSourceBounds(filePath, start, end)

    fun resetSourceBounds(filePath: String) = mixingEngineProxy.resetSourceBounds(filePath)

    fun clearSources() {
        mixingEngineProxy.clearPlayerSources()
    }
}
