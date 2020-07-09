package com.bluehub.fastmixer.screens.mixing

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bluehub.fastmixer.common.audio.AudioEngine
import com.bluehub.fastmixer.common.audio.AudioEngineProxy

class MixingScreenViewModelFactory (private val context: Context?, private val audioEngine: AudioEngineProxy, private val tag: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MixingScreenViewModel::class.java)) {
            return MixingScreenViewModel(context, audioEngine, tag) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}