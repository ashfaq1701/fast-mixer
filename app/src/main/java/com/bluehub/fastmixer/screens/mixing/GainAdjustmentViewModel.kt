package com.bluehub.fastmixer.screens.mixing

import android.content.Context
import androidx.lifecycle.*
import com.bluehub.fastmixer.common.viewmodel.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class GainAdjustmentViewModel @Inject constructor(
    val context: Context,
    private val mixingRepository: MixingRepository,
    val fileWaveViewStore: FileWaveViewStore
) : BaseViewModel() {

    val isLoading: MutableLiveData<Boolean> = MutableLiveData()

    private lateinit var _audioFile: AudioFile

    private val _closeDialog: MutableLiveData<Boolean> = MutableLiveData()
    val closeDialog: LiveData<Boolean>
        get() = _closeDialog

    private val _gainValue: MutableLiveData<Int> = MutableLiveData(0)
    val gainValue: LiveData<Int>
        get() = _gainValue

    fun setAudioFile(audioFile: AudioFile) {
        _audioFile = audioFile
    }

    fun setGainValue(gainVal: Int) {
        _gainValue.value = gainVal
    }

    fun applyGain() {
        viewModelScope.launch(Dispatchers.IO) {
            _gainValue.value?.let { gainValue ->
                isLoading.postValue(true)
                mixingRepository.gainSourceByDb(_audioFile.path, gainValue.toFloat())
                isLoading.postValue(false)
            }
        }
    }

    fun saveGainApplication() {
        viewModelScope.launch(Dispatchers.IO) {
            isLoading.postValue(true)
            mixingRepository.applySourceTransformation(_audioFile.path)
            fileWaveViewStore.reRenderFile(_audioFile.path)
            isLoading.postValue(false)
            _closeDialog.postValue(true)
        }
    }

    fun cancelGainApplication() {
        isLoading.postValue(true)
        mixingRepository.clearSourceTransformation(_audioFile.path)
        isLoading.postValue(false)
        _closeDialog.postValue(true)
    }
}
