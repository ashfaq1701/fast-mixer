package com.bluehub.fastmixer.common.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bluehub.fastmixer.fragments.recording.RecordingViewModel
import com.bluehub.fastmixer.screens.mixing.MixingScreenViewModel
import com.bluehub.fastmixer.screens.recording.RecordingScreenViewModel

class ViewModelFactory(private val mMixingScreenViewModel: MixingScreenViewModel,
                       private val mRecordingScreenViewModel: RecordingScreenViewModel,
                       private val mRecordingViewModel: RecordingViewModel): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        lateinit var viewModel: ViewModel
        if (modelClass == MixingScreenViewModel::class.java) {
            viewModel = mMixingScreenViewModel;
        } else if (modelClass == RecordingScreenViewModel::class.java) {
            viewModel = mRecordingScreenViewModel;
        } else if (modelClass == RecordingViewModel::class.java) {
            viewModel = mRecordingViewModel;
        } else {
            throw IllegalArgumentException("invalid view model class: $modelClass")
        }
        return viewModel as T
    }
}