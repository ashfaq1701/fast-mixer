package com.bluehub.fastmixer.screens.recording

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bluehub.fastmixer.common.audio.AudioEngineProxy
import com.bluehub.fastmixer.common.repositories.AudioRepository
import com.visualizer.amplitude.AudioRecordView

class RecordingScreenViewModelFactory (private val context: Context?, private val audioRecordView: AudioRecordView, private val tag: String) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecordingScreenViewModel::class.java)) {
            return RecordingScreenViewModel(context, audioRecordView, tag) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}