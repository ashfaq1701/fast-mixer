package com.bluehub.fastmixer.common.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bluehub.fastmixer.fragments.recording.RecordingViewModel
import com.bluehub.fastmixer.screens.mixing.MixingViewModel

class ViewModelFactory(private val mMixingViewModel: MixingViewModel,
                       private val mRecordingViewModel: RecordingViewModel): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        lateinit var viewModel: ViewModel
        if (modelClass == MixingViewModel::class.java) {
            viewModel = mMixingViewModel;
        } else if (modelClass == RecordingViewModel::class.java) {
            viewModel = mRecordingViewModel;
        } else {
            throw IllegalArgumentException("invalid view model class: $modelClass")
        }
        return viewModel as T
    }
}