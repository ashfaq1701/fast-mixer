package com.bluehub.fastmixer.common.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bluehub.fastmixer.screens.mixing.MixingViewModel

class ViewModelFactory(private val mMixingViewModel: MixingViewModel): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        lateinit var viewModel: ViewModel
        if (modelClass == MixingViewModel::class.java) {
            viewModel = mMixingViewModel;
        } else {
            throw IllegalArgumentException("invalid view model class: $modelClass")
        }
        return viewModel as T
    }

}