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

    private lateinit var _audioFilePath: String

    private val _closeDialog: MutableLiveData<Boolean> = MutableLiveData()
    val closeDialog: LiveData<Boolean>
        get() = _closeDialog

    private val _gainValue: MutableLiveData<Int> = MutableLiveData(0)
    val gainValue: LiveData<Int>
        get() = _gainValue

    fun setAudioFilePath(audioFilePath: String) {
        _audioFilePath = audioFilePath
    }

    fun setGainValue(gainVal: Int) {
        _gainValue.value = gainVal
    }

    fun applyGain() {
        viewModelScope.launch(Dispatchers.IO) {
            _gainValue.value?.let { gainValue ->
                isLoading.postValue(true)
                mixingRepository.gainSourceByDb(_audioFilePath, gainValue.toFloat())
                isLoading.postValue(false)
            }
        }
    }

    fun saveGainApplication() {
        viewModelScope.launch(Dispatchers.IO) {
            isLoading.postValue(true)
            mixingRepository.applySourceTransformation(_audioFilePath)
            fileWaveViewStore.reRenderFile(_audioFilePath)
            isLoading.postValue(false)
            _closeDialog.postValue(true)
        }
    }

    fun cancelGainApplication() {
        isLoading.postValue(true)
        mixingRepository.clearSourceTransformation(_audioFilePath)
        isLoading.postValue(false)
        _closeDialog.postValue(true)
    }
}
