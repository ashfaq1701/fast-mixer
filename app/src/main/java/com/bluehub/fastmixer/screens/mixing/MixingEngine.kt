package com.bluehub.fastmixer.screens.mixing

class MixingEngine {
    companion object {
        init {
            System.loadLibrary("mixingEngine")
        }

        @JvmStatic external fun create(): Boolean

        @JvmStatic external fun addFile(filePath: String, uuid: String)

        @JvmStatic external fun readSamples(uuid: String, numSamples: Int): Array<Float>

        @JvmStatic external fun deleteFile(uuid: String)

        @JvmStatic external fun getTotalSamples(uuid: String): Int

        @JvmStatic external fun delete()
    }
}