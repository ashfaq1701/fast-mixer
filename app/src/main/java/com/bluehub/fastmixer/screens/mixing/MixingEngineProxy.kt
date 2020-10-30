package com.bluehub.fastmixer.screens.mixing

class MixingEngineProxy {
    companion object {
        private val INSTANCE: MixingEngineProxy =
            MixingEngineProxy()

        public fun getInstance(): MixingEngineProxy {
            return INSTANCE
        }
    }

    fun create() = MixingEngine.create()

    fun readAllSamples(uuid: String) = MixingEngine.readAllSamples(uuid)

    fun getTotalSamples(filePath: String): Int = MixingEngine.getTotalSamples(filePath)

    fun delete() = MixingEngine.delete()
}