package com.bluehub.mixi.screens.mixing.modals

import android.content.Context
import androidx.lifecycle.*
import com.bluehub.mixi.screens.mixing.FileWaveViewStore
import com.bluehub.mixi.screens.mixing.MixingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GainAdjustmentViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    private val mixingRepository: MixingRepository,
    val fileWaveViewStore: FileWaveViewStore
) : BaseDialogViewModel() {

    private lateinit var _audioFilePath: String

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
            closeDialogLiveData.postValue(true)
        }
    }

    fun cancelGainApplication() {
        isLoading.postValue(true)
        mixingRepository.clearSourceTransformation(_audioFilePath)
        isLoading.postValue(false)
        closeDialogLiveData.postValue(true)
    }
}
