package com.bluehub.fastmixer.screens.mixing

import javax.inject.Inject

class MixingEngineProxy @Inject constructor() {
    fun create() = MixingEngine.create()

    fun addFile(filePath: String) = MixingEngine.addFile(filePath)

    fun readSamples(filePath: String, countPoints: Int) = MixingEngine.readSamples(filePath, countPoints)

    fun getTotalSamples(filePath: String): Int = MixingEngine.getTotalSamples(filePath)

    fun addSources(filePaths: Array<String>) = MixingEngine.addSources(filePaths)

    fun clearPlayerSources() = MixingEngine.clearPlayerSources()

    fun startPlayback() = MixingEngine.startPlayback()

    fun pausePlayback() = MixingEngine.pausePlayback()

    fun deleteFile(filePath: String) = MixingEngine.deleteFile(filePath)

    fun delete() = MixingEngine.delete()
}
