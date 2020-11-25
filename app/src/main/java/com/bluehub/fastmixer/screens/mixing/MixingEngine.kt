package com.bluehub.fastmixer.screens.mixing

import java.util.*

class MixingEngine {
    companion object {
        init {
            System.loadLibrary("mixingEngine")
        }

        @JvmStatic external fun create(): Boolean

        @JvmStatic external fun addFile(filePath: String)

        @JvmStatic external fun readSamples(filePath: String, numSamples: Int): Array<Float>

        @JvmStatic external fun deleteFile(filePath: String)

        @JvmStatic external fun getTotalSamples(filePath: String): Int

        @JvmStatic external fun delete()
    }
}