package com.bluehub.fastmixer.screens.mixing

class MixingRepository(val mixingEngineProxy: MixingEngineProxy) {
    fun createMixingEngine() {
        mixingEngineProxy.create()
    }

    fun readSamples(filePath: String): Array<Float> = mixingEngineProxy.readSamples(filePath)

    fun getTotalSamples(filePath: String): Int = mixingEngineProxy.getTotalSamples(filePath)

    fun deleteMixingEngine() = mixingEngineProxy.delete()
}