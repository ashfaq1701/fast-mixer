package com.bluehub.fastmixer.common.permissions

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bluehub.fastmixer.common.utils.PermissionManager
import kotlin.random.Random

interface ViewModelPermissionInterface {
    val context: Context?

    val tag: String
}