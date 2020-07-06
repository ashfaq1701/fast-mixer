package com.bluehub.fastmixer.common.permissions

import androidx.lifecycle.Observer
import com.bluehub.fastmixer.common.fragments.BaseFragment
import com.bluehub.fastmixer.common.utils.DialogManager
import com.bluehub.fastmixer.common.utils.PermissionManager
import android.Manifest

abstract class PermissionFragment: BaseFragment() {
    abstract var dialogManager: DialogManager

    abstract var viewModel: PermissionViewModel

    fun setPermissionEvents() {
        viewModel.eventRequestRecordPermission.observe(viewLifecycleOwner, Observer { requestRecordPermission ->
            if (requestRecordPermission) {
                requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO))
                viewModel.resetRequestRecordPermission()
            }
        })

        viewModel.eventRequestReadFilePermission.observe(viewLifecycleOwner, Observer { requestReadFilePermission ->
            if (requestReadFilePermission) {
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
                viewModel.resetRequestReadFilePermission()
            }
        })

        viewModel.eventRequestWriteFilePermission.observe(viewLifecycleOwner, Observer { requestWriteFilePermission ->
            if (requestWriteFilePermission) {
                requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE))
                viewModel.resetRequestWriteFilePermission()
            }
        })

        viewModel.eventShowRecordingPermissionDialog.observe(viewLifecycleOwner, Observer { showRecordingPermissionDialog ->
            if (showRecordingPermissionDialog) {
                context?.let {
                    dialogManager.showPermissionsErrorDialog(context!!, Manifest.permission.RECORD_AUDIO)
                }
                viewModel.hideRecordingPermissionDialog()
            }
        })

        viewModel.eventShowReadFilePermissionDialog.observe(viewLifecycleOwner, Observer { showReadFilePermissionDialog ->
            if (showReadFilePermissionDialog) {
                context?.let {
                    dialogManager.showPermissionsErrorDialog(context!!, Manifest.permission.READ_EXTERNAL_STORAGE)
                }
                viewModel.hideReadFilePermissionDialog()
            }
        })

        viewModel.eventShowWriteFilePermissionDialog.observe(viewLifecycleOwner, Observer { showWriteFilePermissionDialog ->
            if (showWriteFilePermissionDialog) {
                context?.let {
                    dialogManager.showPermissionsErrorDialog(context!!, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
                viewModel.hideWriteFilePermissionDialog()
            }
        })
    }

    fun requestPermissions(permissions: Array<String>) {
        requestPermissions(permissions, PermissionManager.REQUEST_GROUP_ID)
    }
}