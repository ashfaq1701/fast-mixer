package com.bluehub.fastmixer.screens.recording

import android.content.Context
import android.os.Build
import android.os.Environment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.*

class RecordingRepository(val recordingEngineProxy: RecordingEngineProxy) {
    private val recordingSessionId = UUID.randomUUID().toString()
    private lateinit var cacheDir: String

    suspend fun stopRecording() {
        withContext(Dispatchers.IO) {
            recordingEngineProxy.stopRecording()
        }
    }

    fun startLivePlayback() {
        recordingEngineProxy.startLivePlayback()
    }

    fun pauseLivePlayback() {
        recordingEngineProxy.pauseLivePlayback()
    }

    fun stopLivePlayback() {
        recordingEngineProxy.stopLivePlayback()
    }

    fun startPlaying(): Boolean {
        return recordingEngineProxy.startPlayback()
    }

    fun pausePlaying() {
        recordingEngineProxy.pausePlayback()
    }

    suspend fun stopPlaying() {
        withContext(Dispatchers.IO) {
            recordingEngineProxy.stopPlayback()
        }
    }

    fun stopPlayingSync() {
        recordingEngineProxy.stopPlayback()
    }

    fun startRecording() {
        recordingEngineProxy.startRecording()
    }

    suspend fun pauseRecording() {
        withContext(Dispatchers.IO) {
            recordingEngineProxy.pauseRecording()
        }
    }

    suspend fun flushWriteBuffer() {
        withContext(Dispatchers.IO) {
            recordingEngineProxy.flushWriteBuffer()
        }
    }

    suspend fun restartPlayback() {
        withContext(Dispatchers.IO) {
            recordingEngineProxy.restartPlayback()
        }
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

    fun copyRecordedFile(context: Context) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            val externalPath = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
            val cacheFile = File("$cacheDir/recording.wav")
            if (cacheFile.exists()) {
                Files.copy(
                    Paths.get("$cacheDir/recording.wav"),
                    Paths.get(externalPath!!.path + "/$recordingSessionId.wav"),
                    StandardCopyOption.REPLACE_EXISTING
                )
            }
        }
    }

    fun getCurrentMax() = recordingEngineProxy.getCurrentMax()

    fun resetCurrentMax() = recordingEngineProxy.resetCurrentMax()

    fun getTotalRecordedFrames() = recordingEngineProxy.getTotalRecordedFrames()

    fun getCurrentPlaybackProgress() = recordingEngineProxy.getCurrentPlaybackProgress()

    fun setPlayHead(position: Int) = recordingEngineProxy.setPlayHead(position)

    fun getDurationInSeconds() = recordingEngineProxy.getDurationInSeconds()

    fun resetRecordingEngine() = recordingEngineProxy.resetRecordingEngine()

    fun getRecordedFilePath(): String = "$cacheDir/recording.wav"
}