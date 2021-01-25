package com.bluehub.fastmixer.common.permissions

import android.Manifest
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bluehub.fastmixer.MixerApplication
import com.bluehub.fastmixer.common.utils.PermissionManager
import com.bluehub.fastmixer.common.viewmodel.BaseViewModel
import javax.inject.Inject

abstract class PermissionControlViewModel(open val context: Context?): BaseViewModel() {
    abstract val permissionManager: PermissionManager

    private val _eventRecordPermission = MutableLiveData<PermissionHolder>(PermissionHolder(hasPermission = false))
    val eventRecordPermission: LiveData<PermissionHolder>
        get() = _eventRecordPermission

    private val _eventRequestRecordPermission = MutableLiveData<PermissionRequest>(PermissionRequest(toRequest = false))
    val eventRequestRecordPermission: LiveData<PermissionRequest>
        get() = _eventRequestRecordPermission

    private val _eventShowRecordingPermissionDialog = MutableLiveData<Boolean>(false)
    val eventShowRecordingPermissionDialog: LiveData<Boolean>
        get() = _eventShowRecordingPermissionDialog

    fun setRequestRecordPermission(permissionCode: Int) {
        _eventRequestRecordPermission.value = PermissionRequest(toRequest = true, requestCode = permissionCode)
    }

    fun resetRequestRecordPermission() {
        _eventRequestRecordPermission.value = PermissionRequest(toRequest = false)
    }

    fun showRecordingPermissionDialog() {
        _eventShowRecordingPermissionDialog.value = true
    }

    fun hideRecordingPermissionDialog() {
        _eventShowRecordingPermissionDialog.value = false
    }

    fun checkRecordingPermission(): Boolean {
        return if (permissionManager.isSpecifiedPermissionsGranted(arrayOf(Manifest.permission.RECORD_AUDIO), context)) {
            _eventRecordPermission.value = PermissionHolder(hasPermission = true)
            true
        } else {
            _eventRecordPermission.value = PermissionHolder(hasPermission = false)
            false
        }
    }

    fun setEventRecordPermission(permissionHolder: PermissionHolder) {
        _eventRecordPermission.value = permissionHolder
    }
}
