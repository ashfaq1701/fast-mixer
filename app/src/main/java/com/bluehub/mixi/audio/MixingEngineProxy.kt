package com.bluehub.mixi.audio

import javax.inject.Inject

class MixingEngineProxy @Inject constructor() {
    fun create() = MixingEngine.create()

    fun addFile(filePath: String, fd: Int): Boolean = MixingEngine.addFile(filePath, fd)

    fun readSamples(filePath: String, countPoints: Int) = MixingEngine.readSamples(filePath, countPoints)

    fun getTotalSampleFrames() = MixingEngine.getTotalSampleFrames()

    fun getTotalSamples(filePath: String): Int = MixingEngine.getTotalSamples(filePath)

    fun getCurrentPlaybackProgress(): Int = MixingEngine.getCurrentPlaybackProgress()

    fun addSources(filePaths: Array<String>) = MixingEngine.addSources(filePaths)

    fun setPlayerHead(playHead: Int) = MixingEngine.setPlayerHead(playHead)

    fun setSourcePlayHead(filePath: String, playHead: Int) = MixingEngine.setSourcePlayHead(filePath, playHead)

    fun clearPlayerSources() = MixingEngine.clearPlayerSources()

    fun startPlayback() = MixingEngine.startPlayback()

    fun pausePlayback() = MixingEngine.pausePlayback()

    fun deleteFile(filePath: String) = MixingEngine.deleteFile(filePath)

    fun gainSourceByDb(filePath: String, db: Float) = MixingEngine.gainSourceByDb(filePath, db)

    fun applySourceTransformation(filePath: String) = MixingEngine.applySourceTransformation(filePath)

    fun clearSourceTransformation(filePath: String) = MixingEngine.clearSourceTransformation(filePath)

    fun setSourceBounds(filePath: String, start: Int, end: Int) = MixingEngine.setSourceBounds(filePath, start, end)

    fun resetSourceBounds(filePath: String) = MixingEngine.resetSourceBounds(filePath)

    fun shiftBySamples(filePath: String, position: Int, numSamples: Int): Int = MixingEngine.shiftBySamples(filePath, position, numSamples)

    fun cutToClipboard(filePath: String, startPosition: Int, endPosition: Int): Int = MixingEngine.cutToClipboard(filePath, startPosition, endPosition)

    fun copyToClipboard(filePath: String, startPosition: Int, endPosition: Int): Boolean = MixingEngine.copyToClipboard(filePath, startPosition, endPosition)

    fun muteAndCopyToClipboard(filePath: String, startPosition: Int, endPosition: Int): Boolean = MixingEngine.muteAndCopyToClipboard(filePath, startPosition, endPosition)

    fun pasteFromClipboard(filePath: String, position: Int) = MixingEngine.pasteFromClipboard(filePath, position)

    fun pasteNewFromClipboard(filePath: String) = MixingEngine.pasteNewFromClipboard(filePath)

    fun setPlayerBoundStart(playerBoundStart: Int) = MixingEngine.setPlayerBoundStart(playerBoundStart)

    fun setPlayerBoundEnd(playerBoundEnd: Int) = MixingEngine.setPlayerBoundEnd(playerBoundEnd)

    fun resetPlayerBoundStart() = MixingEngine.resetPlayerBoundStart()

    fun resetPlayerBoundEnd() = MixingEngine.resetPlayerBoundEnd()

    fun writeToFile(pathList: Array<String>, fd: Int) = MixingEngine.writeToFile(pathList, fd)

    fun delete() = MixingEngine.delete()
}
