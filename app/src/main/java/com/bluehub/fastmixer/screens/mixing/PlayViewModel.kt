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

    lateinit var mAudioFilePath: String

    val isPlaying: LiveData<Boolean>
        get() = playFlagStore.isPlaying

    val isGroupPlaying: LiveData<Boolean>
        get() = playFlagStore.isGroupPlaying

    private lateinit var _isLoading: MutableLiveData<Boolean>

    private val playList: MutableList<String> = mutableListOf()

    private var seekbarTimer: Timer? = null

    private val _seekbarProgress = MutableLiveData<Int>(0)
    val seekbarProgress: LiveData<Int>
        get() = _seekbarProgress

    private val _seekbarMaxValue = MutableLiveData<Int>(0)
    val seekbarMaxValue: LiveData<Int>
        get() = _seekbarMaxValue

    fun setIsLoadingLiveData(isLoading: MutableLiveData<Boolean>) {
        _isLoading = isLoading
    }

    fun setAudioFilePath(audioFilePath: String) {
        mAudioFilePath = audioFilePath
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

            setIsLoading()

            val pathList = listOf(mAudioFilePath)
            mixingRepository.loadFiles(pathList)

            _seekbarMaxValue.postValue(mixingRepository.getTotalSampleFrames())

            cancelIsLoading()
        }
    }

    private fun playAudio() {
        viewModelScope.launch(Dispatchers.IO) {

            setIsLoading()

            val pathList = listOf(mAudioFilePath)

            if (!playList.areEqual(pathList)) {
                mixingRepository.loadFiles(pathList)
                playList.reInitList(pathList)
            }

            mixingRepository.startPlayback()
            playFlagStore.isPlaying.postValue(true)

            cancelIsLoading()
        }
    }

    private fun pauseAudio() {
        viewModelScope.launch(Dispatchers.IO) {
            setIsLoading()

            mixingRepository.pausePlayback()
            playFlagStore.isPlaying.postValue(false)

            cancelIsLoading()
        }
    }

    private fun groupPlay() {
        viewModelScope.launch(Dispatchers.IO) {
            setIsLoading()

            val pathList = audioFileStore.audioFiles.map { it.path }
            if (!playList.areEqual(pathList)) {
                mixingRepository.loadFiles(pathList)
                playList.reInitList(pathList)
            }
            mixingRepository.startPlayback()
            playFlagStore.isGroupPlaying.postValue(true)

            cancelIsLoading()
        }
    }

    private fun groupPause() {
        viewModelScope.launch(Dispatchers.IO) {
            setIsLoading()

            mixingRepository.pausePlayback()
            playFlagStore.isGroupPlaying.postValue(false)

            cancelIsLoading()
        }
    }

    fun startPlayback() {
        viewModelScope.launch(Dispatchers.IO) {
            setIsLoading()
            mixingRepository.startPlayback()
            cancelIsLoading()
        }
    }

    fun pausePlayback() {
        viewModelScope.launch(Dispatchers.IO) {
            setIsLoading()
            mixingRepository.pausePlayback()
            cancelIsLoading()
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

    fun setTrackPlayHead(playHead: Int) = mixingRepository.setSourcePlayHead(mAudioFilePath, playHead)

    fun setIsLoading() {
        if (::_isLoading.isInitialized) {
            _isLoading.postValue(true)
        }
    }

    fun cancelIsLoading() {
        if (::_isLoading.isInitialized) {
            _isLoading.postValue(false)
        }
    }

    override fun onCleared() {
        super.onCleared()

        if (isPlaying.value == true || isGroupPlaying.value == true) {
            mixingRepository.pausePlayback()
        }

        if (isPlaying.value == true) {
            playFlagStore.isPlaying.value = false
        }

        if (isGroupPlaying.value == true) {
            playFlagStore.isGroupPlaying.value = false
        }
    }
}
