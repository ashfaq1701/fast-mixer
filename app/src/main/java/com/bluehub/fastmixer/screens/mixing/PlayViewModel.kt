package com.bluehub.fastmixer.screens.mixing

import android.content.Context
import com.bluehub.fastmixer.common.viewmodel.BaseViewModel
import javax.inject.Inject

class PlayViewModel @Inject constructor(
    val context: Context,
    private val mixingRepository: MixingRepository,
    private val audioFileStore: AudioFileStore) : BaseViewModel() {

    lateinit var selectedAudioFile: AudioFile
}
