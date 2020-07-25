package com.bluehub.fastmixer.screens.recording

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bluehub.fastmixer.common.audio.AudioDeviceChangeListener
import com.bluehub.fastmixer.common.audio.AudioEngineProxy
import com.bluehub.fastmixer.common.permissions.PermissionViewModel
import com.bluehub.fastmixer.common.utils.PermissionManager
import com.bluehub.fastmixer.common.utils.ScreenConstants
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.util.*
import javax.inject.Inject

class RecordingScreenViewModel(override val context: Context?, val audioEngineProxy: AudioEngineProxy, override val tag: String) : PermissionViewModel(context, tag) {
    override var TAG: String = javaClass.simpleName

    private val recordingSessionId = UUID.randomUUID().toString()
    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    @Inject
    override lateinit var permissionManager: PermissionManager

    @Inject
    lateinit var audioDeviceChangeListener: AudioDeviceChangeListener

    private val _eventIsRecording = MutableLiveData<Boolean>(false)
    val eventIsRecording: LiveData<Boolean>
        get() = _eventIsRecording

    private val _eventIsPlaying = MutableLiveData<Boolean>(false)
    val eventIsPlaying: LiveData<Boolean>
        get() = _eventIsPlaying

    private val _eventLivePlaybackSet = MutableLiveData<Boolean>(true)
    val eventLivePlayback: LiveData<Boolean>
        get() = _eventLivePlaybackSet

    private val _eventGoBack = MutableLiveData<Boolean>(false)
    val eventGoBack: LiveData<Boolean>
        get() = _eventGoBack

    init {
        getViewModelComponent().inject(this)
        uiScope.launch {
            withContext(Dispatchers.IO) {
                context?.let {
                    val cacheDir = createCacheDirectory()
                    audioEngineProxy.create(cacheDir, recordingSessionId)
                }
            }
        }
        audioDeviceChangeListener.setRestartInputCallback {
            restartInputStreams()
        }
        audioDeviceChangeListener.setRestartOutputCallback {
            restartOutputStreams()
        }

        val cacheDir = createCacheDirectory()

        val inStr = context!!.assets.open("example.wav")
        val outFile = File(cacheDir, "example.wav")
        val out = FileOutputStream(outFile)

        var read: Int = 0
        val bytes = ByteArray(1024)

        while (inStr.read(bytes).also({ read = it }) != -1) {
            out.write(bytes, 0, read)
        }

        inStr.close()
        out.flush()
        out.close()
    }

    private fun restartInputStreams() {

    }

    private fun restartOutputStreams() {

    }

    fun toggleRecording() {
        if (!checkRecordingPermission()) {
            setRequestRecordPermission(ScreenConstants.TOGGLE_RECORDING)
            return
        }

        _eventIsRecording.value = !_eventIsRecording.value!!

        if (_eventIsRecording.value == true) {
            uiScope.launch {
                startRecording()
                _eventLivePlaybackSet.value?.let {
                    if (it) {
                        startLivePlayback()
                    }
                }
            }
        } else {
            uiScope.launch {
                pauseRecording()
                _eventLivePlaybackSet.value?.let {
                    if (it) {
                        pauseLivePlayback()
                    }
                }
            }
        }
    }

    private fun createCacheDirectory(): String {
        val cacheDir = context!!.cacheDir.absolutePath + "/" + recordingSessionId
        val cacheDirFile = File(cacheDir)
        if (!cacheDirFile.exists()) {
            cacheDirFile.mkdir()
        }
        return cacheDir
    }

    fun togglePlay() {
        _eventIsPlaying.value = !_eventIsPlaying.value!!
        if(_eventIsPlaying.value == true) {
            uiScope.launch {
                startPlaying()
            }
        } else {
            uiScope.launch {
                pausePlaying()
            }
        }
    }

    fun deleteAudioEngine() {
        audioEngineProxy.delete()
    }

    fun reset() {
        uiScope.launch {
            stopRecording()
            _eventLivePlaybackSet.value?.let {
                if (it) {
                    stopLivePlayback()
                }
            }
        }
    }

    fun setGoBack() {
        uiScope.launch {
            stopRecording()
            _eventLivePlaybackSet.value?.let {
                if (it) {
                    stopLivePlayback()
                }
            }
            _eventIsPlaying.value?.let {
                if (it) {
                    stopPlaying()
                }
            }
            _eventGoBack.value = true
        }
    }

    fun resetGoBack() {
        _eventGoBack.value = false
    }

    private suspend fun startRecording() {
        withContext(Dispatchers.IO) {
            audioEngineProxy.startRecording()
        }
    }

    private suspend fun pauseRecording() {
        withContext(Dispatchers.IO) {
            audioEngineProxy.pauseRecording()
        }
    }

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

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
        deleteAudioEngine()
    }
}