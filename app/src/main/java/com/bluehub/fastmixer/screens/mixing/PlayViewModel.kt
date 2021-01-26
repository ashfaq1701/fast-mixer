package com.bluehub.fastmixer.screens.mixing

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bluehub.fastmixer.common.viewmodel.BaseViewModel
import javax.inject.Inject

class PlayViewModel @Inject constructor(
    val context: Context,
    private val mixingRepository: MixingRepository,
    private val audioFileStore: AudioFileStore,
    private val playFlagStore: PlayFlagStore) : BaseViewModel() {

    lateinit var selectedAudioFile: AudioFile

    val isPlaying: LiveData<Boolean>
        get() = playFlagStore.isPlaying

    val isGroupPlaying: LiveData<Boolean>
        get() = playFlagStore.isGroupPlaying

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean>
        get() = _isLoading

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

    private fun playAudio() {

    }

    private fun pauseAudio() {

    }

    private fun groupPlay() {

    }

    private fun groupPause() {

    }
}
