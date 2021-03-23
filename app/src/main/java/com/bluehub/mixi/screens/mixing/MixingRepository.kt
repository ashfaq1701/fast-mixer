package com.bluehub.mixi.screens.mixing

import com.bluehub.mixi.audio.MixingEngineProxy
import javax.inject.Inject

class MixingRepository @Inject constructor(val mixingEngineProxy: MixingEngineProxy) {
    fun createMixingEngine() {
        mixingEngineProxy.create()
    }

    fun addFile(filePath: String, fd: Int) {
        mixingEngineProxy.addFile(filePath, fd)
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

    fun shiftBySamples(filePath: String, position: Int, numSamples: Int): Int = mixingEngineProxy.shiftBySamples(filePath, position, numSamples)

    fun cutToClipboard(filePath: String, startPosition: Int, endPosition: Int): Int = mixingEngineProxy.cutToClipboard(filePath, startPosition, endPosition)

    fun copyToClipboard(filePath: String, startPosition: Int, endPosition: Int): Boolean = mixingEngineProxy.copyToClipboard(filePath, startPosition, endPosition)

    fun muteAndCopyToClipboard(filePath: String, startPosition: Int, endPosition: Int): Boolean = mixingEngineProxy.muteAndCopyToClipboard(filePath, startPosition, endPosition)

    fun pasteFromClipboard(filePath: String, position: Int) = mixingEngineProxy.pasteFromClipboard(filePath, position)

    fun pasteNewFromClipboard(filePath: String) = mixingEngineProxy.pasteNewFromClipboard(filePath)

    fun setPlayerBoundStart(playerBoundStart: Int) = mixingEngineProxy.setPlayerBoundStart(playerBoundStart)

    fun setPlayerBoundEnd(playerBoundEnd: Int) = mixingEngineProxy.setPlayerBoundEnd(playerBoundEnd)

    fun resetPlayerBoundStart() = mixingEngineProxy.resetPlayerBoundStart()

    fun resetPlayerBoundEnd() = mixingEngineProxy.resetPlayerBoundEnd()

    fun writeToFile(pathList: List<String>, fd: Int) = mixingEngineProxy.writeToFile(pathList.toTypedArray(), fd)

    fun clearSources() {
        mixingEngineProxy.clearPlayerSources()
    }
}
