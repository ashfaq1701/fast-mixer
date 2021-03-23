package com.bluehub.mixi.screens.recording

import com.bluehub.mixi.audio.RecordingEngineProxy
import com.bluehub.mixi.common.utils.FileManager
import java.io.File
import javax.inject.Inject

class RecordingRepository @Inject constructor(
    private val recordingEngineProxy: RecordingEngineProxy,
    private val fileManager: FileManager) {

    fun setupAudioSource(filePath: String): Boolean {
        return fileManager.getReadOnlyFdForPath(filePath)?.also {
            recordingEngineProxy.setupAudioSource(it.fd)
        } != null
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

    fun startPlaying(): Boolean {
        return recordingEngineProxy.startPlayback()
    }

    fun startPlayingWithMixingTracks(): Boolean {
        return recordingEngineProxy.startPlaybackWithMixingTracks()
    }

    fun startPlayingWithMixingTracksWithoutSetup() {
        recordingEngineProxy.startPlayingWithMixingTracksWithoutSetup()
    }

    fun startMixingTracksPlaying(): Boolean {
        return recordingEngineProxy.startMixingTracksPlayback()
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

    fun restartPlayback() {
        recordingEngineProxy.restartPlayback()
    }

    fun restartPlaybackWithMixingTracks() {
        recordingEngineProxy.restartPlaybackWithMixingTracks()
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
