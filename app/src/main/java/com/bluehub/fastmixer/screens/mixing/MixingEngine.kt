package com.bluehub.fastmixer.screens.mixing

class MixingEngine {
    companion object {
        init {
            System.loadLibrary("mixingEngine")
        }

        @JvmStatic external fun create(): Boolean

        @JvmStatic external fun addFile(filePath: String)

        @JvmStatic external fun readSamples(index: Int, numSamples: Int): Array<Float>

        @JvmStatic external fun deleteFile(index: Int)
    }
}