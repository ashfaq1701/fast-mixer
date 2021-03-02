package com.bluehub.fastmixer.screens.recording

import com.bluehub.fastmixer.audio.RecordingEngineProxy
import com.bluehub.fastmixer.common.utils.FileManager
import java.io.File
import javax.inject.Inject

class RecordingRepository @Inject constructor(
    private val recordingEngineProxy: RecordingEngineProxy,
    private val fileManager: FileManager) {

    fun setupAudioSource(filePath: String): Int? {
        return fileManager.getFdForPath(filePath)?.also {
            recordingEngineProxy.setupAudioSource(it)
        }
    }

    fun stopRecording() {
        recordingEngineProxy.stopRecording()
    }

    fun startLivePlayback() {
        recordingEngineProxy.startLivePlayback()
    }

    fun stopLivePlayback() {
        recordingEngineProxy.stopLivePlayback()
    }

    fun startPlaying(fd: Int): Boolean {
        try {
            return recordingEngineProxy.startPlayback()
        } finally {
            recordingEngineProxy.closeFd(fd)
        }
    }

    fun startPlayingWithMixingTracks(fd: Int): Boolean {
        try {
            return recordingEngineProxy.startPlaybackWithMixingTracks()
        } finally {
            recordingEngineProxy.closeFd(fd)
        }
    }

    fun startPlayingWithMixingTracksWithoutSetup() {
        recordingEngineProxy.startPlayingWithMixingTracksWithoutSetup()
    }

    fun startMixingTracksPlaying(fd: Int): Boolean {
        try {
            return recordingEngineProxy.startMixingTracksPlayback()
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

    fun restartPlayback(fd: Int) {
        try {
            recordingEngineProxy.restartPlayback()
        } finally {
            recordingEngineProxy.closeFd(fd)
        }
    }

    fun restartPlaybackWithMixingTracks(fd: Int) {
        try {
            recordingEngineProxy.restartPlaybackWithMixingTracks()
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
