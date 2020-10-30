package com.bluehub.fastmixer.screens.mixing

class MixingRepository(val mixingEngineProxy: MixingEngineProxy) {
    fun createMixingEngine() {
        mixingEngineProxy.create()
    }

    fun readAllSamples(filePath: String): Array<Float> = mixingEngineProxy.readAllSamples(filePath)

    fun getTotalSamples(uuid: String): Int = mixingEngineProxy.getTotalSamples(uuid)

    fun deleteMixingEngine() = mixingEngineProxy.delete()
}