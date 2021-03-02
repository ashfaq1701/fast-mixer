package com.bluehub.fastmixer.screens.recording

import com.bluehub.fastmixer.audio.RecordingEngineProxy
import com.bluehub.fastmixer.common.utils.FileManager
import java.io.File
import javax.inject.Inject

class RecordingRepository @Inject constructor(
    private val recordingEngineProxy: RecordingEngineProxy,
    private val fileManager: FileManager) {

    fun stopRecording() {
        recordingEngineProxy.stopRecording()
    }

    fun startLivePlayback() {
        recordingEngineProxy.startLivePlayback()
    }

    fun stopLivePlayback() {
        recordingEngineProxy.stopLivePlayback()
    }

    fun startPlaying(filePath: String): Boolean {
        val fd = fileManager.getFdForPath(filePath) ?: return false
        try {
            return recordingEngineProxy.startPlayback(fd)
        } finally {
            recordingEngineProxy.closeFd(fd)
        }
    }

    fun startPlayingWithMixingTracks(filePath: String): Boolean {
        val fd = fileManager.getFdForPath(filePath) ?: return false

        try {
            return recordingEngineProxy.startPlaybackWithMixingTracks(fd)
        } finally {
            recordingEngineProxy.closeFd(fd)
        }
    }

    fun startPlayingWithMixingTracksWithoutSetup() {
        recordingEngineProxy.startPlayingWithMixingTracksWithoutSetup()
    }

    fun startMixingTracksPlaying(filePath: String): Boolean {
        val fd = fileManager.getFdForPath(filePath) ?: return false

        try {
            return recordingEngineProxy.startMixingTracksPlayback(fd)
        } finally {
            recordingEngineProxy.closeFd(fd)
        }
    }

    fun stopMixingTracksPlay() {
        return recordingEngineProxy.stopMixingTracksPlayback()
    }

    fun pausePlaying() {
        recordingEngineProxy.pausePlayback()
    }

    fun stopPlaying() {
        recordingEngineProxy.stopPlayback()
    }

    fun startRecording() {
        recordingEngineProxy.startRecording()
    }

    fun flushWriteBuffer() {
        recordingEngineProxy.flushWriteBuffer()
    }

    fun restartPlayback(filePath: String) {
        val fd = fileManager.getFdForPath(filePath) ?: return

        try {
            recordingEngineProxy.restartPlayback(fd)
        } finally {
            recordingEngineProxy.closeFd(fd)
        }
    }

    fun restartPlaybackWithMixingTracks(filePath: String) {
        val fd = fileManager.getFdForPath(filePath) ?: return
        
        try {
            recordingEngineProxy.restartPlaybackWithMixingTracks(fd)
        } finally {
            recordingEngineProxy.closeFd(fd)
        }
    }

    fun deleteAudioEngine() {
        recordingEngineProxy.delete()
    }

    fun createAudioEngine(recordingFileDir: String) {
        val cacheDirFile = File(recordingFileDir)
        if (!cacheDirFile.exists()) {
            cacheDirFile.mkdir()
        }
        recordingEngineProxy.create(recordingFileDir, true)
    }

    fun loadFiles(fileArr: List<String>) {
        recordingEngineProxy.addSources(fileArr.toTypedArray())
    }

    fun getCurrentMax() = recordingEngineProxy.getCurrentMax()

    fun resetCurrentMax() = recordingEngineProxy.resetCurrentMax()

    fun getTotalSampleFrames() = recordingEngineProxy.getTotalSampleFrames()

    fun getCurrentPlaybackProgress() = recordingEngineProxy.getCurrentPlaybackProgress()

    fun setPlayHead(position: Int) = recordingEngineProxy.setPlayHead(position)

    fun getDurationInSeconds() = recordingEngineProxy.getDurationInSeconds()

    fun resetRecordingEngine() = recordingEngineProxy.resetRecordingEngine()

}
