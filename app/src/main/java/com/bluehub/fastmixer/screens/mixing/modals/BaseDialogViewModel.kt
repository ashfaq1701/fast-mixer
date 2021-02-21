package com.bluehub.fastmixer.screens.mixing.modals

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bluehub.fastmixer.common.viewmodel.BaseViewModel

open class BaseDialogViewModel : BaseViewModel() {

    protected val closeDialogLiveData: MutableLiveData<Boolean> = MutableLiveData()
    val closeDialog: LiveData<Boolean>
        get() = closeDialogLiveData

    protected val errorLiveData: MutableLiveData<String> = MutableLiveData()
    val error: LiveData<String>
        get() = errorLiveData

    fun setError(str: String) {
        errorLiveData.value = str
    }
}
