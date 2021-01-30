package com.bluehub.fastmixer.screens.mixing

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.bluehub.fastmixer.common.viewmodel.BaseViewModel
import javax.inject.Inject

class GainAdjustmentViewModel @Inject constructor(val context: Context) : BaseViewModel() {

    val isLoading: MutableLiveData<Boolean> = MutableLiveData()
}
