package com.bluehub.fastmixer.screens.recording

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RecordingScreenViewModel : ViewModel() {
    private val _eventSetRecording = MutableLiveData<Boolean>(false)
    val eventSetRecording: LiveData<Boolean>
        get() = _eventSetRecording

    private val _eventDoneRecording = MutableLiveData<Boolean>(false)
    val eventDoneRecording: LiveData<Boolean>
        get() = _eventDoneRecording

    fun toggleRecording() {
        _eventSetRecording.value = !_eventSetRecording.value!!

        if (_eventSetRecording.value == true) {
            startRecording()
        } else {
            stopRecording()
        }
    }

    fun setDoneRecording() {
        _eventDoneRecording.value = true
    }

    fun resetDoneRecording() {
        _eventDoneRecording.value = false
    }

    fun startRecording() {

    }

    fun stopRecording() {

    }
}