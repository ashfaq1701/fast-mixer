package com.bluehub.fastmixer.audio

class MixingEngine {
    companion object {
        init {
            System.loadLibrary("audioEngine")
        }

        @JvmStatic external fun create(): Boolean

        @JvmStatic external fun addFile(filePath: String, fd: Int)

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

        @JvmStatic external fun shiftBySamples(filePath: String, position: Int, numSamples: Int): Int

        @JvmStatic external fun cutToClipboard(filePath: String, startPosition: Int, endPosition: Int): Int

        @JvmStatic external fun copyToClipboard(filePath: String, startPosition: Int, endPosition: Int): Boolean

        @JvmStatic external fun muteAndCopyToClipboard(filePath: String, startPosition: Int, endPosition: Int): Boolean

        @JvmStatic external fun pasteFromClipboard(filePath: String, position: Int)

        @JvmStatic external fun pasteNewFromClipboard(filePath: String)

        @JvmStatic external fun setPlayerBoundStart(playerBoundStart: Int)

        @JvmStatic external fun setPlayerBoundEnd(playerBoundEnd: Int)

        @JvmStatic external fun resetPlayerBoundStart()

        @JvmStatic external fun resetPlayerBoundEnd()

        @JvmStatic external fun writeToFile(pathList: Array<String>, fd: Int): Boolean

        @JvmStatic external fun delete()
    }
}
