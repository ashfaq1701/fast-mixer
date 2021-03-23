package com.bluehub.mixi.screens.recording

import android.content.Context
import android.content.IntentFilter
import android.media.AudioManager
import androidx.databinding.Bindable
import androidx.lifecycle.*
import com.bluehub.mixi.BR
import com.bluehub.mixi.R
import com.bluehub.mixi.broadcastReceivers.AudioDeviceChangeListener
import com.bluehub.mixi.common.repositories.AudioRepository
import com.bluehub.mixi.common.utils.BooleanCombinedLiveData
import com.bluehub.mixi.common.viewmodel.BaseViewModel
import com.bluehub.mixi.screens.mixing.AudioFileStore
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import java.io.File
import java.util.*
import javax.inject.Inject

@HiltViewModel
class RecordingScreenViewModel @Inject constructor (@ApplicationContext val context: Context,
                                                        val repository: RecordingRepository,
                                                        private val audioRepository: AudioRepository,
                                                        private val audioFileStore: AudioFileStore,
                                                        private val audioDeviceChangeListener: AudioDeviceChangeListener)
    : BaseViewModel() {

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

    private val cacheDir by lazy {
        val recordingDirectory = "${context.cacheDir.absolutePath}/recording"
        val recordingFile = File(recordingDirectory)
        if (!recordingFile.exists()) {
            recordingFile.mkdir()
        }
        recordingDirectory
    }

    private val recordingSessionId = UUID.randomUUID().toString()
    private val recordingFileDirectory: String
        get() {
            return "$cacheDir/$recordingSessionId"
        }

    val recordingFilePath: String
        get() {
            return "$recordingFileDirectory/recording.wav"
        }

    private var visualizerTimer: Timer? = null
    private var seekbarTimer: Timer? = null
    private var recordingTimer: Timer?  = null

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    private val _eventIsRecording = MutableLiveData(false)
    val eventIsRecording: LiveData<Boolean>
        get() = _eventIsRecording

    private val _eventIsPlaying = MutableLiveData(false)
    val eventIsPlaying: LiveData<Boolean>
        get() = _eventIsPlaying

    private val _eventIsPlayingWithMixingTracks = MutableLiveData(false)
    val eventIsPlayingWithMixingTracks: LiveData<Boolean>
        get() = _eventIsPlayingWithMixingTracks

    private val _eventLivePlaybackSet = MutableLiveData(false)

    private val _eventGoBack = MutableLiveData(false)
    val eventGoBack: LiveData<Boolean>
        get() = _eventGoBack

    private val _recordingPermissionGranted = MutableLiveData(false)

    private val _requestRecordingPermission = MutableLiveData(false)
    val requestRecordingPermission: LiveData<Boolean>
        get() = _requestRecordingPermission

    val recordingLabel = Transformations.map(_eventIsRecording) {
        if (it)
            context.getString(R.string.stop_recording_label)
        else
            context.getString(R.string.start_recording_label)
    }

    private val _seekbarProgress = MutableLiveData(0)
    val seekbarProgress: LiveData<Int>
        get() = _seekbarProgress

    private val _seekbarMaxValue = MutableLiveData(0)
    val seekbarMaxValue: LiveData<Int>
        get() = _seekbarMaxValue

    private val _audioVisualizerMaxAmplitude = MutableLiveData<Int>()
    val audioVisualizerMaxAmplitude: LiveData<Int>
        get() = _audioVisualizerMaxAmplitude

    private val _audioVisualizerRunning = MutableLiveData(false)
    val audioVisualizerRunning: LiveData<Boolean>
        get() = _audioVisualizerRunning

    private val _recordingTimerText = MutableLiveData("00:00")
    val recordingTimerText: LiveData<String>
        get() = _recordingTimerText

    private val _livePlaybackEnabled = MutableLiveData(false)
    val livePlaybackEnabled: LiveData<Boolean>
        get() = _livePlaybackEnabled


    val mixingPlayActive = MutableLiveData(false)

    val isRecordButtonEnabled = BooleanCombinedLiveData(
        true,
        _eventIsPlaying, _eventIsPlayingWithMixingTracks, _isLoading
    ) { acc, curr ->
        acc && !curr
    }

    val isPlayButtonEnabled = BooleanCombinedLiveData(
        true,
        _eventIsRecording, _eventIsPlayingWithMixingTracks, _isLoading
    ) { acc, curr ->
        acc && !curr
    }

    val isPlayWithMixingTracksButtonEnabled = BooleanCombinedLiveData(
        true,
        _eventIsRecording, _eventIsPlaying, _isLoading
    ) { acc, curr ->
        acc && !curr
    }

    val isResetButtonEnabled = BooleanCombinedLiveData(
        true,
        _eventIsRecording, _eventIsPlaying, _eventIsPlayingWithMixingTracks, _isLoading
    ) { acc, curr ->
        acc && !curr
    }

    val isPlaySeekbarEnabled = BooleanCombinedLiveData(
        false,
        _eventIsPlaying, _eventIsPlayingWithMixingTracks
    ) { acc, curr ->
        acc || curr
    }

    val isGoBackButtonEnabled = BooleanCombinedLiveData(
        true,
        _isLoading
    ) { acc, curr ->
        acc && !curr
    }

    val headphoneConnectedCallback: () -> Unit = {
        if (_eventLivePlaybackSet.value == true) {
            _eventLivePlaybackSet.value = audioRepository.isHeadphoneConnected()
            notifyPropertyChanged(BR.livePlaybackActive)
        }
        _livePlaybackEnabled.value = audioRepository.isHeadphoneConnected()
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
                    repository.run {
                        setupAudioSource(recordingFilePath)
                        restartPlayback()
                    }
                }
            }
        }

        if (_eventIsPlayingWithMixingTracks.value == true) {
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    repository.run {
                        setupAudioSource(recordingFilePath)
                        restartPlaybackWithMixingTracks()
                    }
                }
            }
        }
    }

    init {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.createAudioEngine(recordingFileDirectory)
                loadMixerFiles()
            }

            audioRepository.audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            _livePlaybackEnabled.value = audioRepository.isHeadphoneConnected()
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
            _eventLivePlaybackSet.value = value
            if (eventIsRecording.value == true) {
                toggleLivePlayback()
            }
            notifyPropertyChanged(BR.livePlaybackActive)
        }
    }

    fun toggleRecording() {
        if (_recordingPermissionGranted.value == null || _recordingPermissionGranted.value == false) {
            _requestRecordingPermission.value = true
            return
        }

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _isLoading.postValue(true)

                if (_eventIsRecording.value == false) {
                    repository.resetCurrentMax()

                    if (mixingPlayActive.value == true) {
                        repository.run {
                            setupAudioSource(recordingFilePath)
                            startMixingTracksPlaying()
                        }
                    }

                    repository.startRecording()

                    _eventLivePlaybackSet.value?.let {
                        if (it) {
                            repository.startLivePlayback()
                        }
                    }

                } else {

                    if (mixingPlayActive.value == true) {
                        repository.stopMixingTracksPlay()
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
                _isLoading.postValue(true)

                if(_eventIsPlaying.value == false) {
                    repository.run {
                        if (setupAudioSource(recordingFilePath) && startPlaying()) {
                            _eventIsPlaying.postValue(true)
                        }
                    }
                } else {
                    repository.pausePlaying()
                    _eventIsPlaying.postValue(false)
                }

                _isLoading.postValue(false)
            }
        }
    }

    fun togglePlayWithMixingTracks() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _isLoading.postValue(true)

                if(_eventIsPlayingWithMixingTracks.value == false) {

                    repository.run {
                        setupAudioSource(recordingFilePath)
                        if (startPlayingWithMixingTracks()) {
                            _eventIsPlayingWithMixingTracks.postValue(true)
                        }
                    }
                } else {
                    repository.pausePlaying()
                    _eventIsPlayingWithMixingTracks.postValue(false)
                }

                _isLoading.postValue(false)
            }
        }
    }

    fun startPlayback() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _isLoading.postValue(true)
                repository.run {
                    setupAudioSource(recordingFilePath) && startPlaying()
                }
                _isLoading.postValue(false)
            }
        }
    }

    fun startPlaybackWithMixingTracks() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _isLoading.postValue(true)
                repository.startPlayingWithMixingTracksWithoutSetup()
                _isLoading.postValue(false)
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
        if (_eventIsPlayingWithMixingTracks.value == true) {
            _eventIsPlayingWithMixingTracks.postValue(false)
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

        if (_isLoading.value == true) return

        viewModelScope.launch {
            withContext(Dispatchers.IO) {

                // Stop Recording
                repository.stopRecording()
                _eventIsRecording.postValue(false)

                // Stop Live playback if active
                _eventLivePlaybackSet.value?.let {
                    if (it) {
                        repository.stopLivePlayback()
                        _eventLivePlaybackSet.postValue(false)
                    }
                }

                // Stop play if active
                _eventIsPlaying.value?.let {
                    if (it) {
                        repository.stopPlaying()
                        _eventIsPlaying.postValue(false)
                    }
                }

                // Stop play with mixing tacks if active
                _eventIsPlayingWithMixingTracks.value?.let {
                    if (it) {
                        repository.stopPlaying()
                        _eventIsPlayingWithMixingTracks.postValue(false)
                    }
                }

                // Stop playing mixing tracks if active
                mixingPlayActive.value?.let {
                    if (it) {
                        repository.stopMixingTracksPlay()
                        mixingPlayActive.postValue(false)
                    }
                }
            }

            _eventGoBack.value = true
        }
        stopAllTimers()
    }

    fun resetGoBack() {
        _eventGoBack.value = false
    }

    fun setRecordingPermissionGranted() {
        _recordingPermissionGranted.value = true
    }

    fun resetRequestRecordingPermission() {
        _requestRecordingPermission.value = false
    }

    fun startDrawingVisualizer() {
        _audioVisualizerRunning.value = true
        stopTrackingVisualizerTimer()
        visualizerTimer = Timer()
        visualizerTimer?.schedule(object : TimerTask() {
            override fun run() {
                viewModelScope.launch {
                    _audioVisualizerMaxAmplitude.value = repository.getCurrentMax()
                }
            }
        }, 0, 50)
    }

    fun stopDrawingVisualizer() {
        stopTrackingVisualizerTimer()
        _audioVisualizerRunning.value = false
    }

    fun startTrackingSeekbar() {
        _seekbarProgress.value = 0
        _seekbarMaxValue.value = repository.getTotalSampleFrames()
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
        }, 0, 500)
    }

    fun stopTrackingSeekbarTimer() {
        seekbarTimer?.cancel()
        seekbarTimer = null
    }

    fun stopTrackingRecordingTimer() {
        recordingTimer?.cancel()
        recordingTimer = null
    }

    private fun stopTrackingVisualizerTimer() {
        visualizerTimer?.cancel()
        visualizerTimer = null
    }

    private fun stopAllTimers() {
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
