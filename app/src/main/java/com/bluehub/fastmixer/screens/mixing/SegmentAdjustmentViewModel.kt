package com.bluehub.fastmixer.screens.mixing

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bluehub.fastmixer.R
import com.bluehub.fastmixer.common.models.AudioFileUiState
import com.bluehub.fastmixer.common.viewmodel.BaseViewModel
import javax.inject.Inject

class SegmentAdjustmentViewModel @Inject constructor(
    val context: Context
) : BaseViewModel() {

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

        if (!validateSegmentBoundaries()) return

        val segmentStartValue = _segmentStart.value ?: 0
        val segmentEndValue = _segmentEnd.value ?: 0

        _audioFileUiState.setSegmentStartInMs(segmentStartValue)
        _audioFileUiState.setSegmentEndInMs(segmentEndValue)

        _closeDialog.value = true
    }

    private fun validateSegmentBoundaries(): Boolean {
        if (_segmentStart.value == null || _segmentEnd.value == null) {
            _error.value = context.getString(R.string.error_segment_bounds_required)
            return false
        }

        val segmentStartValue = _segmentStart.value ?: 0
        val segmentEndValue = _segmentEnd.value ?: 0

        if (segmentEndValue > _audioFileUiState.numSamplesMs) {
            _error.value = context.getString(R.string.error_segment_end_should_be_less_than_duration)
            return false
        }

        if (segmentStartValue == segmentEndValue) {
            _error.value = context.getString(R.string.error_segment_start_end_should_be_different)
            return false
        }

        if (segmentStartValue > segmentEndValue) {
            _error.value = context.getString(R.string.error_segment_start_should_be_less_than_end)
            return false
        }

        return true
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
