package com.bluehub.fastmixer.common.permissions

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bluehub.fastmixer.common.utils.PermissionManager
import com.bluehub.fastmixer.common.viewmodel.BaseViewModel
import javax.inject.Inject

abstract class PermissionViewModel(open val context: Context?, open val tag: String): BaseViewModel() {
    abstract var permissionManager: PermissionManager

    private val _eventRecordPermission = MutableLiveData<PermissionHolder>(PermissionHolder(permissionChecked = false, hasPermission = false))
    val eventRecordPermission: LiveData<PermissionHolder>
        get() = _eventRecordPermission

    private val _eventReadFilePermission = MutableLiveData<PermissionHolder>(PermissionHolder(permissionChecked = false, hasPermission = false))
    val eventReadFilePermission: LiveData<PermissionHolder>
        get() = _eventReadFilePermission

    private val _eventWriteFilePermission = MutableLiveData<PermissionHolder>(PermissionHolder(permissionChecked = false, hasPermission = false))
    val eventWriteFilePermission: LiveData<PermissionHolder>
        get() = _eventWriteFilePermission
}