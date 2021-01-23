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
import com.bluehub.fastmixer.R
import com.bluehub.fastmixer.broadcastReceivers.AudioDeviceChangeListener
import com.bluehub.fastmixer.common.permissions.PermissionViewModel
import com.bluehub.fastmixer.common.repositories.AudioRepository
import com.bluehub.fastmixer.common.utils.PermissionManager
import com.bluehub.fastmixer.common.utils.ScreenConstants
import com.bluehub.fastmixer.screens.mixing.AudioFileStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class RecordingScreenViewModel @Inject constructor (override val context: Context,
                                                    override val permissionManager: PermissionManager,
                                                    val repository: RecordingRepository,
                                                    private val audioRepository: AudioRepository,
                                                    private val audioFileStore: AudioFileStore,
                                                    private val audioDeviceChangeListener: AudioDeviceChangeListener)
    : PermissionViewModel(context) {

    companion object {
        private lateinit var instance: RecordingScreenViewModel

        fun setInstance(vmInstance: RecordingScreenViewModel) {
            instance = vmInstance
        }

        @JvmStatic
        public fun setStopPlay() {
            if (::instance.isInitialized) {
                instance.stopPlay()
                instance.stopTrackingSeekbarTimer()
            }
        }
    }

    private var visualizerTimer: Timer? = null
    private var seekbarTimer: Timer? = null
    private var recordingTimer: Timer?  = null

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean>
        get() = _isLoading

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

    val mixingPlayActive = MutableLiveData<Boolean>(false)

    val headphoneConnectedCallback: () -> Unit = {
        if (_eventLivePlaybackSet.value == true) {
            _eventLivePlaybackSet.value = audioRepository.isHeadphoneConnected()
            notifyPropertyChanged(BR.livePlaybackActive)
        }
        _livePlaybackPermitted.value = audioRepository.isHeadphoneConnected()
    }

    val handleInputStreamDisconnection: () -> Unit = {
        if (_eventIsRecording.value == true) {
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    repository.stopRecording()
                }
                _eventIsRecording.value = false
            }
        }
    }

    val handleOutputStreamDisconnection: () -> Unit = {
        if (_eventIsPlaying.value == true) {
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    repository.restartPlayback()
                }
            }
        }
    }

    init {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.createCacheDirectory(context.cacheDir.absolutePath)
                repository.createAudioEngine()
                loadMixerFiles()
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
    fun getLivePlaybackActive(): Boolean {
        return _eventLivePlaybackSet.value!!
    }

    fun setLivePlaybackActive(value: Boolean) {
        if (_eventLivePlaybackSet.value != value) {
            if (!value || (_eventIsRecording.value == true && _livePlaybackPermitted.value == true)) {
                _eventLivePlaybackSet.value = value
                toggleLivePlayback()
            }
            notifyPropertyChanged(BR.livePlaybackActive)
        }
    }

    fun toggleRecording() {
        if (!checkRecordingPermission()) {
            setRequestRecordPermission(ScreenConstants.TOGGLE_RECORDING)
            return
        }

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _isLoading.postValue(true)

                if (_eventIsRecording.value == false) {
                    repository.resetCurrentMax()

                    if (mixingPlayActive.value == true) {
                        repository.startMixingScreenPlaying()
                    }

                    repository.startRecording()

                    _eventLivePlaybackSet.value?.let {
                        if (it) {
                            repository.startLivePlayback()
                        }
                    }

                } else {

                    if (mixingPlayActive.value == true) {
                        repository.stopMixingScreenPlaying()
                    }

                    _eventLivePlaybackSet.value?.let {
                        if (it) {
                            repository.stopLivePlayback()
                        }
                    }

                    repository.stopRecording()
                }

                _isLoading.postValue(false)
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
        if (_eventIsPlaying.value == true) {
            _eventIsPlaying.postValue(false)
        }
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.stopPlaying()
            }
        }
    }

    private fun loadMixerFiles() {
        val audioFilePaths = audioFileStore.audioFiles.map { it.path }

        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.postValue(true)

            repository.loadFiles(audioFilePaths)

            _isLoading.postValue(false)
        }
    }

    fun reset() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.stopRecording()
            }
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
            withContext(Dispatchers.IO) {
                repository.stopRecording()
                _eventIsRecording.postValue(false)
                _eventLivePlaybackSet.value?.let {
                    if (it) {
                        repository.stopLivePlayback()
                        _eventLivePlaybackSet.postValue(false)
                    }
                }
                _eventIsPlaying.value?.let {
                    if (it) {
                        repository.stopPlaying()
                        _eventIsPlaying.postValue(false)
                    }
                }
                repository.copyRecordedFile(context)
            }
            _eventGoBack.value = true
        }
        stopAllTimers()
    }

    fun resetGoBack() {
        _eventGoBack.value = false
    }

    fun startDrawingVisualizer() {
        _audioVisualizerRunning.value = true
        stopTrackingVisualizerTimer()
        visualizerTimer = Timer()
        visualizerTimer?.schedule(object : TimerTask() {
            override fun run() {
                _audioVisualizerMaxAmplitude.postValue(repository.getCurrentMax())
            }
        }, 0, 50)
    }

    fun stopDrawingVisualizer() {
        stopTrackingVisualizerTimer()
        _audioVisualizerRunning.value = false
    }

    fun startTrackingSeekbar() {
        _seekbarProgress.value = 0
        _seekbarMaxValue.value = repository.getTotalRecordedFrames()
        stopTrackingSeekbarTimer()
        seekbarTimer = Timer()
        seekbarTimer?.schedule(object: TimerTask() {
            override fun run() {
                _seekbarProgress.postValue(repository.getCurrentPlaybackProgress())
            }
        }, 0, 10)
    }

    fun setPlayHead(position: Int) {
        repository.setPlayHead(position)
    }

    fun startUpdatingTimer() {
        stopTrackingRecordingTimer()
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

    fun stopTrackingSeekbarTimer() {
        seekbarTimer?.cancel()
        seekbarTimer = null
    }

    fun stopTrackingRecordingTimer() {
        recordingTimer?.cancel()
        recordingTimer = null
    }

    fun stopTrackingVisualizerTimer() {
        visualizerTimer?.cancel()
        visualizerTimer = null
    }

    fun stopAllTimers() {
        stopTrackingSeekbarTimer()
        stopTrackingRecordingTimer()
        stopTrackingVisualizerTimer()
    }

    override fun onCleared() {
        super.onCleared()
        repository.deleteAudioEngine()
        context.unregisterReceiver(audioDeviceChangeListener)
        stopAllTimers()
    }
}
