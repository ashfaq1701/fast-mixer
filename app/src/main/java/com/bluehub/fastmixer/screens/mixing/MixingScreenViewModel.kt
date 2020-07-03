package com.bluehub.fastmixer.screens.mixing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class MixingScreenViewModel : ViewModel() {
    private val _eventRecord = MutableLiveData<Boolean>()
    val eventRecord: LiveData<Boolean>
        get() = _eventRecord

    fun onRecord() {
        _eventRecord.value = true
    }

    fun onRecordNavigated() {
        _eventRecord.value = false
    }
}