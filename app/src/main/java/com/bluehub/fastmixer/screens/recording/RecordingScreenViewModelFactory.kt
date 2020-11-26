package com.bluehub.fastmixer.screens.recording

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bluehub.fastmixer.broadcastReceivers.AudioDeviceChangeListener
import com.bluehub.fastmixer.common.repositories.AudioRepository
import com.bluehub.fastmixer.common.utils.PermissionManager

class RecordingScreenViewModelFactory (private val context: Context,
                                       private val permissionManager: PermissionManager,
                                       private val repository: RecordingRepository,
                                       private val audioRepository: AudioRepository,
                                       private val audioDeviceChangeListener: AudioDeviceChangeListener
) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecordingScreenViewModel::class.java)) {
            return RecordingScreenViewModel(context, permissionManager, repository, audioRepository, audioDeviceChangeListener) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}