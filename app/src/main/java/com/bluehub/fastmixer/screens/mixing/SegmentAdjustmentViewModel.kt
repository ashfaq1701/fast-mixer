package com.bluehub.fastmixer.screens.mixing

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bluehub.fastmixer.R
import com.bluehub.fastmixer.common.models.AudioFileUiState
import com.bluehub.fastmixer.common.utils.Optional
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

    private val _segmentStart: MutableLiveData<Int?> = MutableLiveData()
    val segmentStart: LiveData<Int?>
        get() = _segmentStart

    private val _segmentDuration: MutableLiveData<Int?> = MutableLiveData()
    val segmentDuration: LiveData<Int?>
        get() = _segmentDuration

    fun setAudioFileUiState(audioFileUiState: AudioFileUiState) {
        _audioFileUiState = audioFileUiState

        _audioFileUiState.segmentStartSampleMs?.let {
            _segmentStart.value = it
        }

        _audioFileUiState.segmentDurationMs?.let {
            _segmentDuration.value = it
        }
    }

    fun cancelSegmentAdjustment() {
        _closeDialog.value = true
    }

    fun saveSegmentAdjustment() {

        if (!validateSegmentBoundaries()) return

        val segmentStartValue = _segmentStart.value ?: 0
        val segmentDuration = _segmentDuration.value ?: 0

        _audioFileUiState.setSegmentStartInMs(segmentStartValue)

        val segmentEndValue = segmentStartValue + segmentDuration - 1
        _audioFileUiState.setSegmentEndInMs(segmentEndValue)

        _closeDialog.value = true
    }

    private fun validateSegmentBoundaries(): Boolean {
        if (_segmentStart.value == null || _segmentDuration.value == null) {
            _error.value = context.getString(R.string.error_segment_bounds_required)
            return false
        }

        val segmentStartValue = _segmentStart.value ?: 0
        val segmentDurationValue = _segmentDuration.value ?: 0

        if (segmentStartValue + segmentDurationValue > _audioFileUiState.numSamplesMs) {
            _error.value = context.getString(R.string.error_segment_duration_should_be_less_than_duration)
            return false
        }

        return true
    }

    fun clearSegment() {
        _audioFileUiState.showSegmentSelector.onNext(false)
        _audioFileUiState.segmentStartSample.onNext(Optional.empty())
        _audioFileUiState.segmentEndSample.onNext(Optional.empty())

        _closeDialog.value = true
    }

    fun setSegmentStart(value: Int?) {
        if (_segmentStart.value != value) {
            _segmentStart.value = value
        }
    }

    fun setSegmentDuration(value: Int?) {
        if (_segmentDuration.value != value) {
            _segmentDuration.value = value
        }
    }

    fun setError(str: String) {
        _error.value = str
    }
}
