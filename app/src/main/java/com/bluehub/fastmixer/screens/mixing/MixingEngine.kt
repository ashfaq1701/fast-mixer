package com.bluehub.fastmixer.screens.mixing

class MixingEngine {
    companion object {
        init {
            System.loadLibrary("mixingEngine")
        }

        @JvmStatic external fun create(): Boolean

        @JvmStatic external fun readSamples(fileName: String): Array<Float>

        @JvmStatic external fun getTotalSamples(filePath: String): Int

        @JvmStatic external fun delete()
    }
}