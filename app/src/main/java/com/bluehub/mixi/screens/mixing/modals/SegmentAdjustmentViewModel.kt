package com.bluehub.mixi.screens.mixing.modals

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bluehub.mixi.R
import com.bluehub.mixi.common.models.AudioFileUiState
import com.bluehub.mixi.common.utils.Optional
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class SegmentAdjustmentViewModel @Inject constructor(
    @ApplicationContext val context: Context
) : BaseDialogViewModel() {

    private lateinit var _audioFileUiState: AudioFileUiState

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
        closeDialogLiveData.value = true
    }

    fun saveSegmentAdjustment() {

        if (!validateSegmentBoundaries()) return

        val segmentStartValue = _segmentStart.value ?: 0
        val segmentDuration = _segmentDuration.value ?: 0

        _audioFileUiState.setSegmentStartInMs(segmentStartValue)

        val segmentEndValue = segmentStartValue + segmentDuration - 1
        _audioFileUiState.setSegmentEndInMs(segmentEndValue)

        closeDialogLiveData.value = true
    }

    private fun validateSegmentBoundaries(): Boolean {
        if (_segmentStart.value == null || _segmentDuration.value == null) {
            errorLiveData.value = context.getString(R.string.error_segment_bounds_required)
            return false
        }

        val segmentStartValue = _segmentStart.value ?: 0
        val segmentDurationValue = _segmentDuration.value ?: 0

        if (segmentStartValue + segmentDurationValue > _audioFileUiState.numSamplesMs) {
            errorLiveData.value = context.getString(R.string.error_segment_duration_should_be_less_than_duration)
            return false
        }

        return true
    }

    fun clearSegment() {
        _audioFileUiState.showSegmentSelector.onNext(false)
        _audioFileUiState.segmentStartSample.onNext(Optional.empty())
        _audioFileUiState.segmentEndSample.onNext(Optional.empty())

        closeDialogLiveData.value = true
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
}
