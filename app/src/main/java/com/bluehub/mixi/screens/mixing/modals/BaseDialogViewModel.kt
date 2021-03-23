package com.bluehub.mixi.screens.mixing.modals

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bluehub.mixi.common.viewmodel.BaseViewModel

open class BaseDialogViewModel : BaseViewModel() {

    val isLoading: MutableLiveData<Boolean> = MutableLiveData()

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
