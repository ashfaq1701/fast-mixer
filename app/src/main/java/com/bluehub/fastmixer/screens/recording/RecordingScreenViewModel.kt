package com.bluehub.fastmixer.screens.recording

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bluehub.fastmixer.common.audio.AudioEngineProxy
import com.bluehub.fastmixer.common.permissions.PermissionViewModel
import com.bluehub.fastmixer.common.utils.PermissionManager
import com.bluehub.fastmixer.common.utils.ScreenConstants
import kotlinx.coroutines.*
import javax.inject.Inject

class RecordingScreenViewModel(override val context: Context?, val audioEngineProxy: AudioEngineProxy, override val tag: String) : PermissionViewModel(context, tag) {
    init {
        getViewModelComponent().inject(this)
    }

    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    @Inject
    override lateinit var permissionManager: PermissionManager

    private val _eventIsRecording = MutableLiveData<Boolean>(false)
    val eventIsRecording: LiveData<Boolean>
        get() = _eventIsRecording

    private val _eventIsPlaying = MutableLiveData<Boolean>(false)
    val eventIsPlaying: LiveData<Boolean>
        get() = _eventIsPlaying

    private val _eventGoBack = MutableLiveData<Boolean>(false)
    val eventGoBack: LiveData<Boolean>
        get() = _eventGoBack

    fun toggleRecording() {
        if (!checkRecordingPermission()) {
            setRequestRecordPermission(ScreenConstants.TOGGLE_RECORDING)
            return
        }

        _eventIsRecording.value = !_eventIsRecording.value!!

        if (_eventIsRecording.value == true) {
            uiScope.launch {
                startRecording()
            }
        } else {
            uiScope.launch {
                pauseRecording()
            }
        }
    }

    fun togglePlay() {
        _eventIsPlaying.value = !_eventIsPlaying.value!!
        if(_eventIsPlaying.value == true) {
            startPlaying()
        } else {
            pausePlaying()
        }
    }

    fun deleteAudioEngine() {
        audioEngineProxy.delete()
    }

    fun reset() {
        uiScope.launch {
            stopRecording()
        }
    }

    fun setGoBack() {
        uiScope.launch {
            stopRecording()
            _eventGoBack.value = true
        }
    }

    suspend fun stopRecording() {
        withContext(Dispatchers.IO) {
            audioEngineProxy.stopRecording()
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

    fun startPlaying() {

    }

    fun pausePlaying() {

    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}