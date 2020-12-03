package com.bluehub.fastmixer.screens.mixing

class MixingEngineProxy {
    fun create() = MixingEngine.create()

    fun addFile(filePath: String) = MixingEngine.addFile(filePath)

    fun readSamples(filePath: String, numSamples: Int) = MixingEngine.readSamples(filePath, numSamples)

    fun getTotalSamples(filePath: String): Int = MixingEngine.getTotalSamples(filePath)

    fun deleteFile(filePath: String) = MixingEngine.deleteFile(filePath)

    fun delete() = MixingEngine.delete()
}