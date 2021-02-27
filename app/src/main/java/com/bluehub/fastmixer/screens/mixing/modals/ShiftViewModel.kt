package com.bluehub.fastmixer.screens.mixing.modals

import android.content.Context
import androidx.lifecycle.*
import com.bluehub.fastmixer.R
import com.bluehub.fastmixer.common.config.Config
import com.bluehub.fastmixer.common.models.AudioFileUiState
import com.bluehub.fastmixer.screens.mixing.FileWaveViewStore
import com.bluehub.fastmixer.screens.mixing.MixingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class ShiftViewModel@Inject constructor(
    val context: Context,
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
        _audioFileUiState.run {
            if (!playSliderPosition.hasValue()) {
                errorLiveData.value = context.getString(R.string.error_play_head_required)
                return
            }

            if (playSliderPositionSample >= numSamples) {
                errorLiveData.value = context.getString(R.string.error_play_head_should_be_less_than_numsamples)
                return
            }

            _shiftDuration.value?.let {

                val shiftDurationSamples = (it * (Config.SAMPLE_RATE.toFloat() / 1000.0)).toInt()

                applyShift(path, playSliderPositionSample, shiftDurationSamples)
            } ?: run {
                errorLiveData.value = context.getString(R.string.error_shift_duration_required)
                return
            }
        }
    }

    private fun applyShift(filePath: String, position: Int, numSamples: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            isLoading.postValue(true)

            val newPlayHeadPosition = mixingRepository.shiftBySamples(filePath, position, numSamples)

            if (newPlayHeadPosition >= 0) {
                fileWaveViewStore.recalculateAudioSegment(filePath, newPlayHeadPosition)
            }

            isLoading.postValue(false)
            closeDialogLiveData.postValue(true)
        }
    }

    fun cancelShift() {
        closeDialogLiveData.value = true
    }
}
