package com.bluehub.fastmixer.screens.recording

import android.content.Context
import android.media.AudioManager
import androidx.databinding.Bindable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bluehub.fastmixer.BR
import com.bluehub.fastmixer.common.permissions.PermissionViewModel
import com.bluehub.fastmixer.common.repositories.AudioRepository
import com.bluehub.fastmixer.common.utils.PermissionManager
import com.bluehub.fastmixer.common.utils.ScreenConstants
import kotlinx.coroutines.*
import timber.log.Timber
import javax.inject.Inject

class RecordingScreenViewModel(override val context: Context?, override val tag: String) : PermissionViewModel(context, tag) {
    override var TAG: String = javaClass.simpleName

    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    @Inject
    override lateinit var permissionManager: PermissionManager

    @Inject
    lateinit var repository: RecordingRepository

    @Inject
    lateinit var audioRepository: AudioRepository

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

    private val _livePlaybackPermitted = MutableLiveData<Boolean>(false)

    init {
        getViewModelComponent().inject(this)
        uiScope.launch {
            withContext(Dispatchers.IO) {
                context?.let {
                    val cacheDir = repository.createCacheDirectory(context!!.cacheDir.absolutePath)
                    repository.createAudioEngine(cacheDir)
                    audioRepository.audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                    _livePlaybackPermitted.value = audioRepository.isHeadphoneConnected()
                }
            }
        }
    }

    @Bindable
    fun getLivePlaybackEnabled(): Boolean {
        return _eventLivePlaybackSet.value!!
    }

    fun setLivePlaybackEnabled(value: Boolean) {
        if (_eventLivePlaybackSet.value != value) {
            if (!value || (_eventIsRecording.value == true && _livePlaybackPermitted.value == true)) {
                _eventLivePlaybackSet.value = value
            }
            notifyPropertyChanged(BR.livePlaybackEnabled)
        }
    }

    val headphoneConnectedCallback: () -> Unit = {
        _livePlaybackPermitted.value = audioRepository.isHeadphoneConnected()
    }

    val handleInputStreamDisconnection: () -> Unit = {
        if (_eventIsRecording.value == true) {
            uiScope.launch {
                repository.flushWriteBuffer()
                _eventIsRecording.value = false
            }
        }
    }

    val handleOutputStreamDisconnection: () -> Unit = {
        if (_eventIsPlaying.value == true) {
            uiScope.launch {
                repository.restartPlayback()
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

    fun toggleLivePlayback() {
        Timber.d("Toggling Live Playback")
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