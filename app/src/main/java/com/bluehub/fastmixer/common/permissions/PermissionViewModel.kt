package com.bluehub.fastmixer.common.permissions

import android.Manifest
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

    private val _eventRequestRecordPermission = MutableLiveData<Boolean>(false)
    val eventRequestRecordPermission: LiveData<Boolean>
        get() = _eventRequestRecordPermission

    private val _eventRequestReadFilePermission = MutableLiveData<Boolean>(false)
    val eventRequestReadFilePermission: LiveData<Boolean>
        get() = _eventRequestReadFilePermission

    private val _eventRequestWriteFilePermission = MutableLiveData<Boolean>(false)
    val eventRequestWriteFilePermission: LiveData<Boolean>
        get() = _eventRequestWriteFilePermission

    private val _eventShowRecordingPermissionDialog = MutableLiveData<Boolean>(false)
    val eventShowRecordingPermissionDialog: LiveData<Boolean>
        get() = _eventShowRecordingPermissionDialog

    private val _eventShowReadFilePermissionDialog = MutableLiveData<Boolean>(false)
    val eventShowReadFilePermissionDialog: LiveData<Boolean>
        get() = _eventShowReadFilePermissionDialog

    private val _eventShowWriteFilePermissionDialog = MutableLiveData<Boolean>(false)
    val eventShowWriteFilePermissionDialog: LiveData<Boolean>
        get() = _eventShowWriteFilePermissionDialog

    fun setRequestRecordPermission() {
        _eventRequestRecordPermission.value = true
    }

    fun resetRequestRecordPermission() {
        _eventRequestRecordPermission.value = false
    }

    fun setRequestReadFilePermission() {
        _eventRequestReadFilePermission.value = true
    }

    fun resetRequestReadFilePermission() {
        _eventRequestReadFilePermission.value = false
    }

    fun setRequestWriteFilePermission() {
        _eventRequestWriteFilePermission.value = true
    }

    fun resetRequestWriteFilePermission() {
        _eventRequestWriteFilePermission.value = false
    }

    fun showRecordingPermissionDialog() {
        _eventShowRecordingPermissionDialog.value = true
    }

    fun hideRecordingPermissionDialog() {
        _eventShowRecordingPermissionDialog.value = false
    }

    fun showReadFilePermissionDialog() {
        _eventShowReadFilePermissionDialog.value = true
    }

    fun hideReadFilePermissionDialog() {
        _eventShowReadFilePermissionDialog.value = false
    }

    fun showWriteFilePermissionDialog() {
        _eventShowWriteFilePermissionDialog.value = true
    }

    fun hideWriteFilePermissionDialog() {
        _eventShowWriteFilePermissionDialog.value = false
    }

    fun checkRecordingPermission(): Boolean {
        return if (permissionManager.isSpecifiedPermissionsGranted(arrayOf(Manifest.permission.RECORD_AUDIO), context)) {
            _eventRecordPermission.value = PermissionHolder(permissionChecked = true, hasPermission = true)
            true
        } else {
            _eventRecordPermission.value = PermissionHolder(permissionChecked = true, hasPermission = false)
            false
        }
    }

    fun checkReadFilePermission(): Boolean {
        return if (permissionManager.isSpecifiedPermissionsGranted(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), context)) {
            _eventReadFilePermission.value = PermissionHolder(permissionChecked = true, hasPermission = true)
            true
        } else {
            _eventReadFilePermission.value =
                PermissionHolder(permissionChecked = true, hasPermission = false)
            false
        }
    }

    fun checkWriteFilePermission(): Boolean {
        return if (permissionManager.isSpecifiedPermissionsGranted(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), context)) {
            _eventWriteFilePermission.value =
                PermissionHolder(permissionChecked = true, hasPermission = true)
            true
        } else {
            _eventWriteFilePermission.value =
                PermissionHolder(permissionChecked = true, hasPermission = false)
            false
        }
    }
}