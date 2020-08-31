package com.bluehub.fastmixer.screens.recording

import android.content.Context
import android.content.IntentFilter
import android.media.AudioManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.databinding.Bindable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.bluehub.fastmixer.BR
import com.bluehub.fastmixer.R
import com.bluehub.fastmixer.broadcastReceivers.AudioDeviceChangeListener
import com.bluehub.fastmixer.common.permissions.PermissionViewModel
import com.bluehub.fastmixer.common.repositories.AudioRepository
import com.bluehub.fastmixer.common.utils.PermissionManager
import com.bluehub.fastmixer.common.utils.ScreenConstants
import kotlinx.coroutines.*
import timber.log.Timber
import java.nio.file.Files
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

    @Inject
    lateinit var audioDeviceChangeListener: AudioDeviceChangeListener

    var audioManager: AudioManager
    var audioSessionId: MutableLiveData<Int> = MutableLiveData(0)

    private val _eventIsRecording = MutableLiveData<Boolean>(false)
    val eventIsRecording: LiveData<Boolean>
        get() = _eventIsRecording

    private val _eventIsPlaying = MutableLiveData<Boolean>(false)
    val eventIsPlaying: LiveData<Boolean>
        get() = _eventIsPlaying

    private val _eventLivePlaybackSet = MutableLiveData<Boolean>(false)
    val eventLivePlayback: LiveData<Boolean>
        get() = _eventLivePlaybackSet

    private val _eventGoBack = MutableLiveData<Boolean>(false)
    val eventGoBack: LiveData<Boolean>
        get() = _eventGoBack

    val recordingLabel = Transformations.map(_eventIsRecording) {
        if (it)
            context!!.getString(R.string.stop_recording_label)
        else
            context!!.getString(R.string.start_recording_label)
    }

    private val _livePlaybackPermitted = MutableLiveData<Boolean>(false)

    val headphoneConnectedCallback: () -> Unit = {
        if (_eventLivePlaybackSet.value == true) {
            _eventLivePlaybackSet.value = audioRepository.isHeadphoneConnected()
            notifyPropertyChanged(BR.livePlaybackEnabled)
        }
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

    init {
        getViewModelComponent().inject(this)
        uiScope.launch {
            withContext(Dispatchers.IO) {
                context?.let {
                    repository.createCacheDirectory(context!!.cacheDir.absolutePath)
                    repository.createAudioEngine()
                }
            }
            context?.let {
                audioRepository.audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                _livePlaybackPermitted.value = audioRepository.isHeadphoneConnected()
            }
        }

        audioDeviceChangeListener.setHandleInputCallback(handleInputStreamDisconnection)
        audioDeviceChangeListener.setHandleOutputCallback(handleOutputStreamDisconnection)
        audioDeviceChangeListener.setHeadphoneConnectedCallback(headphoneConnectedCallback)

        val filter = IntentFilter().apply {
            addAction(AudioManager.ACTION_HEADSET_PLUG)
            addAction(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED)
        }
        context?.registerReceiver(audioDeviceChangeListener, filter)

        audioManager = context!!.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    @Bindable
    fun getLivePlaybackEnabled(): Boolean {
        return _eventLivePlaybackSet.value!!
    }

    fun setLivePlaybackEnabled(value: Boolean) {
        if (_eventLivePlaybackSet.value != value) {
            if (!value || (_eventIsRecording.value == true && _livePlaybackPermitted.value == true)) {
                _eventLivePlaybackSet.value = value
                toggleLivePlayback()
            }
            notifyPropertyChanged(BR.livePlaybackEnabled)
        }
    }

    fun toggleRecording() {
        if (!checkRecordingPermission()) {
            setRequestRecordPermission(ScreenConstants.TOGGLE_RECORDING)
            return
        }

        uiScope.launch {
            audioSessionId.value = audioManager.generateAudioSessionId()
            withContext(Dispatchers.IO) {
                if (_eventIsRecording.value == false) {
                    repository.startRecording(audioSessionId.value!!)
                    _eventLivePlaybackSet.value?.let {
                        if (it) {
                            repository.startLivePlayback()
                        }
                    }
                } else {
                    _eventLivePlaybackSet.value?.let {
                        if (it) {
                            repository.pauseLivePlayback()
                        }
                    }
                    repository.pauseRecording()
                }
            }
            _eventIsRecording.value = !_eventIsRecording.value!!
        }
    }

    fun toggleLivePlayback() {
        if (_eventLivePlaybackSet.value == true) {
            uiScope.launch {
                repository.startLivePlayback()
            }
        } else {
            uiScope.launch {
                repository.stopLivePlayback()
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
            repository.copyRecordedFile(context!!)
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