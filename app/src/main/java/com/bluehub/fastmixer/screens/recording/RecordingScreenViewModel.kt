package com.bluehub.fastmixer.screens.recording

import android.content.Context
import android.content.IntentFilter
import android.media.AudioManager
import androidx.databinding.Bindable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.bluehub.fastmixer.BR
import com.bluehub.fastmixer.MixerApplication
import com.bluehub.fastmixer.R
import com.bluehub.fastmixer.broadcastReceivers.AudioDeviceChangeListener
import com.bluehub.fastmixer.common.permissions.PermissionViewModel
import com.bluehub.fastmixer.common.repositories.AudioRepository
import com.bluehub.fastmixer.common.utils.PermissionManager
import com.bluehub.fastmixer.common.utils.ScreenConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject

class RecordingScreenViewModel(override val context: Context, mixerApplication: MixerApplication, override val tag: String) : PermissionViewModel(context, mixerApplication, tag) {
    companion object {
        private lateinit var instance: RecordingScreenViewModel

        public fun setInstance(vmInstance: RecordingScreenViewModel) {
            instance = vmInstance
        }

        @JvmStatic
        public fun setStopPlay() {
            if (::instance.isInitialized) {
                instance.stopPlay()
                instance.stopTrackingSeekbar()
            }
        }
    }

    override var TAG: String = javaClass.simpleName

    @Inject
    override lateinit var permissionManager: PermissionManager

    @Inject
    lateinit var repository: RecordingRepository

    @Inject
    lateinit var audioRepository: AudioRepository

    @Inject
    lateinit var audioDeviceChangeListener: AudioDeviceChangeListener

    private var visualizerTimer: Timer? = null
    private var seekbarTimer: Timer? = null
    private var recordingTimer: Timer?  = null

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
            context.getString(R.string.stop_recording_label)
        else
            context.getString(R.string.start_recording_label)
    }

    private val _seekbarProgress = MutableLiveData<Int>(0)
    val seekbarProgress: LiveData<Int>
        get() = _seekbarProgress

    private val _seekbarMaxValue = MutableLiveData<Int>(0)
    val seekbarMaxValue: LiveData<Int>
        get() = _seekbarMaxValue

    private val _audioVisualizerMaxAmplitude = MutableLiveData<Int>(0)
    val audioVisualizerMaxAmplitude: LiveData<Int>
        get() = _audioVisualizerMaxAmplitude

    private val _audioVisualizerRunning = MutableLiveData<Boolean>(false)
    val audioVisualizerRunning: LiveData<Boolean>
        get() = _audioVisualizerRunning

    private val _recordingTimerText = MutableLiveData<String>("00:00")
    val recordingTimerText: LiveData<String>
        get() = _recordingTimerText

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
            viewModelScope.launch {
                repository.flushWriteBuffer()
                _eventIsRecording.value = false
            }
        }
    }

    val handleOutputStreamDisconnection: () -> Unit = {
        if (_eventIsPlaying.value == true) {
            viewModelScope.launch {
                repository.restartPlayback()
            }
        }
    }

    init {
        getViewModelComponent().inject(this)
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.createCacheDirectory(context.cacheDir.absolutePath)
                repository.createAudioEngine()
            }

            audioRepository.audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            _livePlaybackPermitted.value = audioRepository.isHeadphoneConnected()
        }

        audioDeviceChangeListener.setHandleInputCallback(handleInputStreamDisconnection)
        audioDeviceChangeListener.setHandleOutputCallback(handleOutputStreamDisconnection)
        audioDeviceChangeListener.setHeadphoneConnectedCallback(headphoneConnectedCallback)

        val filter = IntentFilter().apply {
            addAction(AudioManager.ACTION_HEADSET_PLUG)
            addAction(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED)
        }
        context.registerReceiver(audioDeviceChangeListener, filter)
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

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                if (_eventIsRecording.value == false) {
                    repository.resetCurrentMax()
                    repository.startRecording()
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
            viewModelScope.launch {
                repository.startLivePlayback()
            }
        } else {
            viewModelScope.launch {
                repository.stopLivePlayback()
            }
        }
    }

    fun togglePlay() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                if(_eventIsPlaying.value == false) {
                    if (repository.startPlaying()) {
                        _eventIsPlaying.postValue(true)
                    }
                } else {
                    repository.pausePlaying()
                    _eventIsPlaying.postValue(false)
                }
            }
        }
    }

    fun startPlayback() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.startPlaying()
            }
        }
    }

    fun pausePlayback() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.pausePlaying()
            }
        }
    }

    fun stopPlay() {
        _eventIsPlaying.postValue(false)
        viewModelScope.launch {
            repository.stopPlaying()
        }
    }

    fun reset() {
        viewModelScope.launch {
            repository.stopRecording()
            _eventLivePlaybackSet.value?.let {
                if (it) {
                    repository.stopLivePlayback()
                }
            }
            repository.resetRecordingEngine()
            _seekbarProgress.value = 0
            _seekbarMaxValue.value = 0
            _audioVisualizerMaxAmplitude.value = 0
            _recordingTimerText.value = "00:00"
        }
    }

    fun setGoBack() {
        viewModelScope.launch {
            repository.stopRecording()
            _eventIsRecording.value = false
            _eventLivePlaybackSet.value?.let {
                if (it) {
                    repository.stopLivePlayback()
                    _eventLivePlaybackSet.value = false
                }
            }
            _eventIsPlaying.value?.let {
                if (it) {
                    repository.stopPlaying()
                    _eventIsPlaying.value = false
                }
            }
            repository.copyRecordedFile(context)
            stopUpdatingTimer()
            _eventGoBack.value = true
        }
    }

    fun resetGoBack() {
        _eventGoBack.value = false
    }

    fun startDrawingVisualizer() {
        _audioVisualizerRunning.value = true
        visualizerTimer = Timer()
        visualizerTimer?.schedule(object : TimerTask() {
            override fun run() {
                _audioVisualizerMaxAmplitude.postValue(repository.getCurrentMax())
            }
        }, 0, 50)
    }

    fun stopDrawingVisualizer() {
        visualizerTimer?.let {
            it.cancel()
            _audioVisualizerRunning.value = false
        }
    }

    fun startTrackingSeekbar() {
        _seekbarProgress.value = 0
        _seekbarMaxValue.value = repository.getTotalRecordedFrames()
        seekbarTimer = Timer()
        seekbarTimer?.schedule(object: TimerTask() {
            override fun run() {
                _seekbarProgress.postValue(repository.getCurrentPlaybackProgress())
            }
        }, 0, 10)
    }

    fun stopTrackingSeekbar() {
        seekbarTimer?.cancel()
    }

    fun setPlayHead(position: Int) {
        repository.setPlayHead(position)
    }

    fun startUpdatingTimer() {
        recordingTimer = Timer()
        recordingTimer?.schedule(object: TimerTask() {
            override fun run() {
                val durationInSeconds = repository.getDurationInSeconds()
                val minutes = durationInSeconds / 60
                val seconds = durationInSeconds % 60

                val minutesStr = if (minutes < 10) {
                    "0$minutes"
                } else {
                    minutes.toString()
                }

                val secondsStr = if (seconds < 10) {
                    "0$seconds"
                } else {
                    seconds.toString()
                }

                val timeStr = "$minutesStr:$secondsStr"

                _recordingTimerText.postValue(timeStr)
            }
        }, 0, 1000)
    }

    fun stopUpdatingTimer() {
        recordingTimer?.cancel()
        recordingTimer = null

        seekbarTimer?.cancel()
        seekbarTimer = null;

        visualizerTimer?.cancel()
        visualizerTimer = null
    }

    override fun onCleared() {
        super.onCleared()
        repository.deleteAudioEngine()
        context.unregisterReceiver(audioDeviceChangeListener)
        stopUpdatingTimer()
    }
}