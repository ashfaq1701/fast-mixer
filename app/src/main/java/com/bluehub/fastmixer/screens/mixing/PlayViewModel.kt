package com.bluehub.fastmixer.screens.mixing

import android.content.Context
import androidx.lifecycle.*
import com.bluehub.fastmixer.common.utils.areEqual
import com.bluehub.fastmixer.common.utils.reInitList
import com.bluehub.fastmixer.common.viewmodel.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

class PlayViewModel @Inject constructor(
    val context: Context,
    private val mixingRepository: MixingRepository,
    private val audioFileStore: AudioFileStore,
    private val playFlagStore: PlayFlagStore) : BaseViewModel() {

    lateinit var selectedAudioFile: AudioFile

    val isPlaying: LiveData<Boolean>
        get() = playFlagStore.isPlaying

    val isGroupPlaying: LiveData<Boolean>
        get() = playFlagStore.isGroupPlaying

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    private val playList: MutableList<String> = mutableListOf()

    private var seekbarTimer: Timer? = null

    private val _seekbarProgress = MutableLiveData<Int>(0)
    val seekbarProgress: LiveData<Int>
        get() = _seekbarProgress

    private val _seekbarMaxValue = MutableLiveData<Int>(0)
    val seekbarMaxValue: LiveData<Int>
        get() = _seekbarMaxValue

    fun setAudioFile(audioFile: AudioFile) {
        selectedAudioFile = audioFile
        loadSource()
    }

    fun togglePlay() {
        if (isPlaying.value == null || isPlaying.value == false) {
            playAudio()
        } else {
            pauseAudio()
        }
    }

    fun toggleGroupPlay() {
        if (isGroupPlaying.value == null || isGroupPlaying.value == false) {
            groupPlay()
        } else {
            groupPause()
        }
    }

    private fun loadSource() {
        viewModelScope.launch(Dispatchers.IO) {

            _isLoading.postValue(true)

            val pathList = listOf(selectedAudioFile.path)
            mixingRepository.loadFiles(pathList)

            _seekbarMaxValue.postValue(mixingRepository.getTotalSampleFrames())

            _isLoading.postValue(false)
        }
    }

    private fun playAudio() {
        viewModelScope.launch(Dispatchers.IO) {

            _isLoading.postValue(true)

            val pathList = listOf(selectedAudioFile.path)

            if (!playList.areEqual(pathList)) {
                mixingRepository.loadFiles(pathList)
                playList.reInitList(pathList)
            }

            mixingRepository.startPlayback()
            playFlagStore.isPlaying.postValue(true)

            _isLoading.postValue(false)
        }
    }

    private fun pauseAudio() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.postValue(true)

            mixingRepository.pausePlayback()
            playFlagStore.isPlaying.postValue(false)

            _isLoading.postValue(false)
        }
    }

    private fun groupPlay() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.postValue(true)

            val pathList = audioFileStore.audioFiles.map { it.path }
            if (!playList.areEqual(pathList)) {
                mixingRepository.loadFiles(pathList)
                playList.reInitList(pathList)
            }
            mixingRepository.startPlayback()
            playFlagStore.isGroupPlaying.postValue(true)

            _isLoading.postValue(false)
        }
    }

    private fun groupPause() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.postValue(true)

            mixingRepository.pausePlayback()
            playFlagStore.isGroupPlaying.postValue(false)

            _isLoading.postValue(false)
        }
    }

    fun startPlayback() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.postValue(true)
            mixingRepository.startPlayback()
            _isLoading.postValue(false)
        }
    }

    fun pausePlayback() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.postValue(true)
            mixingRepository.pausePlayback()
            _isLoading.postValue(false)
        }
    }

    fun startTrackingSeekbarTimer() {
        _seekbarProgress.value = 0
        _seekbarMaxValue.value = mixingRepository.getTotalSampleFrames()
        stopTrackingSeekbarTimer()
        seekbarTimer = Timer()
        seekbarTimer?.schedule(object: TimerTask() {
            override fun run() {
                _seekbarProgress.postValue(mixingRepository.getCurrentPlaybackProgress())
            }
        }, 0, 10)
    }

    fun stopTrackingSeekbarTimer() {
        seekbarTimer?.cancel()
        seekbarTimer = null
    }

    fun setPlayerHead(playHead: Int) = mixingRepository.setPlayerHead(playHead)

    fun setTrackPlayHead(playHead: Int) = mixingRepository.setSourcePlayHead(selectedAudioFile.path, playHead)

    override fun onCleared() {
        super.onCleared()

        if (isPlaying.value == true || isGroupPlaying.value == true) {
            mixingRepository.pausePlayback()
        }
    }
}
