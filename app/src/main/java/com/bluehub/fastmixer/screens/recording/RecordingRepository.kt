package com.bluehub.fastmixer.screens.recording

import com.bluehub.fastmixer.common.audio.AudioEngineProxy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*

class RecordingRepository(val audioEngineProxy: AudioEngineProxy) {
    private val recordingSessionId = UUID.randomUUID().toString()

    suspend fun stopRecording() {
        withContext(Dispatchers.IO) {
            audioEngineProxy.stopRecording()
        }
    }

    suspend fun startLivePlayback() {
        withContext(Dispatchers.IO) {
            audioEngineProxy.startLivePlayback()
        }
    }

    suspend fun pauseLivePlayback() {
        withContext(Dispatchers.IO) {
            audioEngineProxy.pauseLivePlayback()
        }
    }

    suspend fun stopLivePlayback() {
        withContext(Dispatchers.IO) {
            audioEngineProxy.stopRecording()
        }
    }

    suspend fun startPlaying() {
        withContext(Dispatchers.IO) {
            audioEngineProxy.startPlayback()
        }
    }

    suspend fun pausePlaying() {
        withContext(Dispatchers.IO) {
            audioEngineProxy.pausePlayback()
        }
    }

    suspend fun stopPlaying() {
        withContext(Dispatchers.IO) {
            audioEngineProxy.stopPlayback()
        }
    }

    suspend fun startRecording() {
        withContext(Dispatchers.IO) {
            audioEngineProxy.startRecording()
        }
    }

    suspend fun pauseRecording() {
        withContext(Dispatchers.IO) {
            audioEngineProxy.pauseRecording()
        }
    }

    suspend fun flushWriteBuffer() {
        withContext(Dispatchers.IO) {
            audioEngineProxy.flushWriteBuffer()
        }
    }

    suspend fun restartPlayback() {
        withContext(Dispatchers.IO) {
            audioEngineProxy.restartPlayback()
        }
    }

    fun deleteAudioEngine() {
        audioEngineProxy.delete()
    }

    fun createCacheDirectory(cacheDirPath: String): String {
        val cacheDir = "$cacheDirPath/$recordingSessionId"
        val cacheDirFile = File(cacheDir)
        if (!cacheDirFile.exists()) {
            cacheDirFile.mkdir()
        }
        return cacheDir
    }

    fun createAudioEngine(cacheDir: String) {
        audioEngineProxy.create(cacheDir, recordingSessionId)
    }
}