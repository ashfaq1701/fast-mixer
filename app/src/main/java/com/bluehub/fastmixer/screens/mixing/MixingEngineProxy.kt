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

    fun addFile(filePath: String, uuid: String) = MixingEngine.addFile(filePath, uuid)

    fun readSamples(uuid: String, numSamples: Int) = MixingEngine.readSamples(uuid, numSamples)

    fun deleteFile(uuid: String) = MixingEngine.deleteFile(uuid)

    fun getTotalSamples(uuid: String): Int = MixingEngine.getTotalSamples(uuid)

    fun delete() = MixingEngine.delete()
}