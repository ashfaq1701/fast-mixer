package com.bluehub.fastmixer.screens.mixing

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bluehub.fastmixer.common.utils.PermissionManager

class MixingScreenViewModelFactory (private val context: Context,
                                    private val permissionManager: PermissionManager,
                                    private val mixingRepository: MixingRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MixingScreenViewModel::class.java)) {
            return MixingScreenViewModel(context, permissionManager, mixingRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}