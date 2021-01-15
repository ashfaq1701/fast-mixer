package com.bluehub.fastmixer.screens.mixing

class MixingEngine {
    companion object {
        init {
            System.loadLibrary("mixingEngine")
        }

        @JvmStatic external fun create(): Boolean

        @JvmStatic external fun addFile(filePath: String)

        @JvmStatic external fun readSamples(filePath: String, countPoints: Int): Array<Float>

        @JvmStatic external fun deleteFile(filePath: String)

        @JvmStatic external fun addSources(filePaths: Array<String>)

        @JvmStatic external fun clearPlayerSources()

        @JvmStatic external fun startPlayback()

        @JvmStatic external fun pausePlayback()

        @JvmStatic external fun getTotalSamples(filePath: String): Int

        @JvmStatic external fun getCurrentPlaybackProgress(): Int

        @JvmStatic external fun delete()
    }
}
