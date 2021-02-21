package com.bluehub.fastmixer.screens.mixing.modals

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bluehub.fastmixer.common.models.AudioFileUiState
import com.bluehub.fastmixer.screens.mixing.FileWaveViewStore
import com.bluehub.fastmixer.screens.mixing.MixingRepository
import javax.inject.Inject

class ShiftViewModel@Inject constructor(
    val fileWaveViewStore: FileWaveViewStore,
    val mixingRepository: MixingRepository
) : BaseDialogViewModel() {

    private lateinit var _audioFileUiState: AudioFileUiState

    private val _shiftDuration: MutableLiveData<Int?> = MutableLiveData()
    val shiftDuration: LiveData<Int?>
        get() = _shiftDuration

    fun setAudioFileUiState(audioFileUiState: AudioFileUiState) {
        _audioFileUiState = audioFileUiState
    }

    fun setShiftDuration(value: Int?) {
        if (_shiftDuration.value != value) {
            _shiftDuration.value = value
        }
    }

    fun saveShift() {
        closeDialogLiveData.value = true
    }

    fun cancelShift() {
        closeDialogLiveData.value = true
    }
}
