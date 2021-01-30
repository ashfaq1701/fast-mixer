package com.bluehub.fastmixer.audio

import javax.inject.Inject

class MixingEngineProxy @Inject constructor() {
    fun create() = MixingEngine.create()

    fun addFile(filePath: String) = MixingEngine.addFile(filePath)

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

    fun gainSourceByDb(filePath: String) = MixingEngine.applySourceTransformation(filePath)

    fun clearSourceByDb(filePath: String) = MixingEngine.clearSourceTransformation(filePath)

    fun delete() = MixingEngine.delete()
}
