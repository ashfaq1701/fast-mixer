package com.bluehub.fastmixer.screens.recording

import android.content.Context
import android.os.Build
import android.os.Environment
import com.bluehub.fastmixer.audio.RecordingEngineProxy
import java.io.File
import java.nio.file.*
import java.util.*
import javax.inject.Inject

class RecordingRepository @Inject
    constructor(private val recordingEngineProxy: RecordingEngineProxy) {

    private val recordingSessionId = UUID.randomUUID().toString()
    private lateinit var cacheDir: String

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

    fun createCacheDirectory(cacheDirPath: String): String {
        cacheDir = "$cacheDirPath/$recordingSessionId"
        val cacheDirFile = File(cacheDir)
        if (!cacheDirFile.exists()) {
            cacheDirFile.mkdir()
        }
        return cacheDir
    }

    fun createAudioEngine() {
        recordingEngineProxy.create(cacheDir, recordingSessionId, true)
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

    fun getRecordedFilePath(): String = "$cacheDir/recording.wav"
}
