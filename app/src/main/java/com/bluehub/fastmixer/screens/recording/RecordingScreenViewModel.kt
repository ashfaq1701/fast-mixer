package com.bluehub.fastmixer.screens.recording

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bluehub.fastmixer.common.permissions.PermissionViewModel
import com.bluehub.fastmixer.common.utils.PermissionManager
import com.bluehub.fastmixer.common.utils.ScreenConstants
import javax.inject.Inject

class RecordingScreenViewModel(override val context: Context?, override val tag: String) : PermissionViewModel(context, tag) {
    init {
        getViewModelComponent().inject(this)
    }

    @Inject
    override lateinit var permissionManager: PermissionManager

    private val _eventSetRecording = MutableLiveData<Boolean>(false)
    val eventSetRecording: LiveData<Boolean>
        get() = _eventSetRecording

    private val _eventSetPlaying = MutableLiveData<Boolean>(false)
    val eventSetPlaying: LiveData<Boolean>
        get() = _eventSetPlaying

    private val _eventGoBack = MutableLiveData<Boolean>(false)
    val eventGoBack: LiveData<Boolean>
        get() = _eventGoBack

    fun toggleRecording() {
        if (!checkRecordingPermission()) {
            setRequestRecordPermission(ScreenConstants.TOGGLE_RECORDING)
            return
        }

        _eventSetRecording.value = !_eventSetRecording.value!!

        if (_eventSetRecording.value == true) {
            startRecording()
        } else {
            pauseRecording()
        }
    }

    fun togglePlay() {
        _eventSetPlaying.value = !_eventSetPlaying.value!!
        if(_eventSetPlaying.value == true) {
            startPlaying()
        } else {
            pausePlaying()
        }
    }

    fun stopRecording() {
        if (!checkRecordingPermission()) {
            setRequestRecordPermission(ScreenConstants.STOP_RECORDING)
            return
        }
    }

    fun setGoBack() {
        _eventGoBack.value = true
    }

    fun resetGoBack() {
        _eventGoBack.value = false
    }

    fun startRecording() {

    }

    fun pauseRecording() {

    }

    fun startPlaying() {

    }

    fun pausePlaying() {

    }
}