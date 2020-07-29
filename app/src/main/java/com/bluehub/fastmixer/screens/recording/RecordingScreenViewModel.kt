package com.bluehub.fastmixer.screens.recording

import android.content.Context
import android.util.Log
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

class RecordingScreenViewModel(override val context: Context?, override val tag: String) : PermissionViewModel(context, tag) {
    override var TAG: String = javaClass.simpleName

    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    @Inject
    override lateinit var permissionManager: PermissionManager

    @Inject
    lateinit var repository: RecordingRepository;

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
                    val cacheDir = repository.createCacheDirectory(context!!.cacheDir.absolutePath)
                    repository.createAudioEngine(cacheDir)
                }
            }
        }
    }

    fun restartInputStreams() {
        if (_eventIsRecording.value == true) {
            uiScope.launch {

            }
        }
    }

    fun restartOutputStreams() {
        uiScope.launch {
            if (_eventIsRecording.value == true && _eventLivePlaybackSet.value == true) {

            } else if (_eventIsPlaying.value == true) {

            }
        }
    }

    fun toggleRecording() {
        if (!checkRecordingPermission()) {
            setRequestRecordPermission(ScreenConstants.TOGGLE_RECORDING)
            return
        }

        _eventIsRecording.value = !_eventIsRecording.value!!

        if (_eventIsRecording.value == true) {
            uiScope.launch {
                repository.startRecording()
                _eventLivePlaybackSet.value?.let {
                    if (it) {
                        repository.startLivePlayback()
                    }
                }
            }
        } else {
            uiScope.launch {
                _eventLivePlaybackSet.value?.let {
                    if (it) {
                        repository.pauseLivePlayback()
                    }
                }
                repository.pauseRecording()
            }
        }
    }

    fun togglePlay() {
        _eventIsPlaying.value = !_eventIsPlaying.value!!
        if(_eventIsPlaying.value == true) {
            uiScope.launch {
                repository.startPlaying()
            }
        } else {
            uiScope.launch {
                repository.pausePlaying()
            }
        }
    }

    fun reset() {
        uiScope.launch {
            repository.stopRecording()
            _eventLivePlaybackSet.value?.let {
                if (it) {
                    repository.stopLivePlayback()
                }
            }
        }
    }

    fun setGoBack() {
        uiScope.launch {
            repository.stopRecording()
            _eventLivePlaybackSet.value?.let {
                if (it) {
                    repository.stopLivePlayback()
                }
            }
            _eventIsPlaying.value?.let {
                if (it) {
                    repository.stopPlaying()
                }
            }
            _eventGoBack.value = true
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
        repository.deleteAudioEngine()
    }

    fun resetGoBack() {
        _eventGoBack.value = false
        _eventGoBack.value = false
    }
}