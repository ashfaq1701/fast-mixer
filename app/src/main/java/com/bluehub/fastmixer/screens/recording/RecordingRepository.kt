package com.bluehub.fastmixer.screens.recording

import android.content.Context
import android.os.Build
import android.os.Environment
import com.bluehub.fastmixer.common.audio.AudioEngineProxy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.*

class RecordingRepository(val audioEngineProxy: AudioEngineProxy) {
    private val recordingSessionId = UUID.randomUUID().toString()
    private lateinit var cacheDir: String

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
            audioEngineProxy.stopLivePlayback()
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
        cacheDir = "$cacheDirPath/$recordingSessionId"
        val cacheDirFile = File(cacheDir)
        if (!cacheDirFile.exists()) {
            cacheDirFile.mkdir()
        }
        return cacheDir
    }

    fun createAudioEngine() {
        audioEngineProxy.create(cacheDir, recordingSessionId)
    }

    fun copyRecordedFile(context: Context) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            val externalPath = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
            Files.copy(Paths.get("$cacheDir/recording.wav"), Paths.get(externalPath!!.path + "/$recordingSessionId.wav"), StandardCopyOption.REPLACE_EXISTING)
        }
    }
}