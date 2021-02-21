package com.bluehub.fastmixer.audio

class MixingEngine {
    companion object {
        init {
            System.loadLibrary("audioEngine")
        }

        @JvmStatic external fun create(): Boolean

        @JvmStatic external fun addFile(filePath: String)

        @JvmStatic external fun readSamples(filePath: String, countPoints: Int): Array<Float>

        @JvmStatic external fun deleteFile(filePath: String)

        @JvmStatic external fun addSources(filePaths: Array<String>)

        @JvmStatic external fun clearPlayerSources()

        @JvmStatic external fun setPlayerHead(playHead: Int)

        @JvmStatic external fun setSourcePlayHead(filePath: String, playHead: Int)

        @JvmStatic external fun startPlayback()

        @JvmStatic external fun pausePlayback()

        @JvmStatic external fun getTotalSamples(filePath: String): Int

        @JvmStatic external fun getTotalSampleFrames(): Int

        @JvmStatic external fun getCurrentPlaybackProgress(): Int

        @JvmStatic external fun gainSourceByDb(filePath: String, db: Float)

        @JvmStatic external fun applySourceTransformation(filePath: String)

        @JvmStatic external fun clearSourceTransformation(filePath: String)

        @JvmStatic external fun setSourceBounds(filePath: String, start: Int, end: Int)

        @JvmStatic external fun resetSourceBounds(filePath: String)

        @JvmStatic external fun shiftBySamples(filePath: String, position: Int, numSamples: Int)

        @JvmStatic external fun delete()
    }
}
