package com.bluehub.fastmixer.screens.mixing

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bluehub.fastmixer.common.viewmodel.BaseScreenViewModel

class MixingScreenViewModel(val context: Context?, val tag: String): BaseScreenViewModel(context, tag) {
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