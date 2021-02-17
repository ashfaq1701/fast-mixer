package com.bluehub.fastmixer.screens.mixing

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bluehub.fastmixer.common.models.AudioFileUiState
import com.bluehub.fastmixer.common.viewmodel.BaseViewModel
import javax.inject.Inject

class SegmentAdjustmentViewModel @Inject constructor() : BaseViewModel() {

    private lateinit var _audioFileUiState: AudioFileUiState

    private val _closeDialog: MutableLiveData<Boolean> = MutableLiveData()
    val closeDialog: LiveData<Boolean>
        get() = _closeDialog

    private val _error: MutableLiveData<String> = MutableLiveData()
    val error: LiveData<String>
        get() = _error

    private val _segmentStart: MutableLiveData<Int> = MutableLiveData()
    val segmentStart: LiveData<Int>
        get() = _segmentStart

    private val _segmentEnd: MutableLiveData<Int> = MutableLiveData()
    val segmentEnd: LiveData<Int>
        get() = _segmentEnd

    fun setAudioFileUiState(audioFileUiState: AudioFileUiState) {
        _audioFileUiState = audioFileUiState

        _audioFileUiState.segmentStartSampleMs?.let {
            _segmentStart.value = it
        }

        _audioFileUiState.segmentEndSampleMs?.let {
            _segmentEnd.value = it
        }
    }

    fun cancelSegmentAdjustment() {
        _closeDialog.value = true
    }

    fun saveSegmentAdjustment() {

        if (_segmentStart.value == null || _segmentEnd.value == null) {
            _error.value = "Segment start and end bounds has to be set"
            return
        }

        val segmentStartValue = _segmentStart.value ?: 0
        val segmentEndValue = _segmentEnd.value ?: 0

        if (segmentEndValue > _audioFileUiState.numSamplesMs) {
            _error.value = "Segment end should be less than file duration"
            return
        }

        if (segmentStartValue == segmentEndValue) {
            _error.value = "Segment start and end value has to be different"
            return
        }

        if (segmentStartValue > segmentEndValue) {
            _error.value = "Segment start value should be less than end value"
            return
        }

        _audioFileUiState.setSegmentStartInMs(segmentStartValue)
        _audioFileUiState.setSegmentEndInMs(segmentEndValue)

        _closeDialog.value = true
    }

    fun setSegmentStart(value: Int) {
        if (_segmentStart.value != value) {
            _segmentStart.value = value
        }
    }

    fun setSegmentEnd(value: Int) {
        if (_segmentEnd.value != value) {
            _segmentEnd.value = value
        }
    }

    fun setError(str: String) {
        _error.value = str
    }
}
