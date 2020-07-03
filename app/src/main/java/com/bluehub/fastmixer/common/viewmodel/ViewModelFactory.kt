package com.bluehub.fastmixer.common.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bluehub.fastmixer.fragments.recording.VisualizerViewModel
import com.bluehub.fastmixer.screens.mixing.MixingScreenViewModel
import com.bluehub.fastmixer.screens.recording.RecordingScreenViewModel

class ViewModelFactory(private val mMixingScreenViewModel: MixingScreenViewModel,
                       private val mRecordingScreenViewModel: RecordingScreenViewModel,
                       private val mVisualizerViewModel: VisualizerViewModel): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        lateinit var viewModel: ViewModel
        if (modelClass == MixingScreenViewModel::class.java) {
            viewModel = mMixingScreenViewModel;
        } else if (modelClass == RecordingScreenViewModel::class.java) {
            viewModel = mRecordingScreenViewModel;
        } else if (modelClass == VisualizerViewModel::class.java) {
            viewModel = mVisualizerViewModel;
        } else {
            throw IllegalArgumentException("invalid view model class: $modelClass")
        }
        return viewModel as T
    }
}