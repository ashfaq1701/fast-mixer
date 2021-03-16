package com.bluehub.fastmixer.screens.mixing.modals

import com.bluehub.fastmixer.screens.mixing.AudioFileStore
import com.bluehub.fastmixer.screens.mixing.MixingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class WriteViewModel @Inject constructor(
    private val audioFileStore: AudioFileStore,
    val mixingRepository: MixingRepository
) : BaseDialogViewModel() {

    init {

    }

    fun performWrite() {
        closeDialogLiveData.postValue(true)
    }

    fun cancelWrite() {
        closeDialogLiveData.value = true
    }
}
