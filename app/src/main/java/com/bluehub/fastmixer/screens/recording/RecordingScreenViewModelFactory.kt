package com.bluehub.fastmixer.screens.recording

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bluehub.fastmixer.MixerApplication

class RecordingScreenViewModelFactory (private val context: Context, private val mixerApplication: MixerApplication, private val tag: String) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecordingScreenViewModel::class.java)) {
            return RecordingScreenViewModel(context, mixerApplication, tag) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}