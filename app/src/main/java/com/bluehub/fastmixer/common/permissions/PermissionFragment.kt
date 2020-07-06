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
        viewModel.eventRecordPermission.observe(viewLifecycleOwner, Observer {recordPermission ->
            if (recordPermission.permissionChecked && !recordPermission.hasPermission) {
                viewModel.enableRecordControls()
            } else {
                viewModel.disableRecordControls()
            }
        })

        viewModel.eventReadFilePermission.observe(viewLifecycleOwner, Observer {readFilePermission ->
            if (readFilePermission.permissionChecked && !readFilePermission.hasPermission) {
                viewModel.enableReadFileControls()
            } else {
                viewModel.disableReadFileControls()
            }
        })

        viewModel.eventWriteFilePermission.observe(viewLifecycleOwner, Observer {writeFilePermission ->
            if (writeFilePermission.permissionChecked && !writeFilePermission.hasPermission) {
                viewModel.enableWriteFileControls()
            } else {
                viewModel.disableWriteFileControls()
            }
        })

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

        viewModel.eventEnableRecordControls.observe(viewLifecycleOwner, Observer { recordControlsEnable ->
            if (recordControlsEnable) {
                enableRecordingControls()
            } else {
                disableRecordingControls()
            }
        })

        viewModel.eventEnableReadFileControls.observe(viewLifecycleOwner, Observer { readFileControlsEnable ->
            if (readFileControlsEnable) {
                enableReadFileControls()
            } else {
                disableReadFileControls()
            }
        })

        viewModel.eventEnableWriteFileControls.observe(viewLifecycleOwner, Observer { writeFileControlsEnable ->
            if (writeFileControlsEnable) {
                enableWriteFileControls()
            } else {
                disableWriteFileControls()
            }
        })
    }

    open fun enableRecordingControls() {}

    open fun disableRecordingControls() {}

    open fun enableReadFileControls() {}

    open fun disableReadFileControls() {}

    open fun enableWriteFileControls() {}

    open fun disableWriteFileControls() {}

    fun requestPermissions(permissions: Array<String>) {
        requestPermissions(permissions, PermissionManager.REQUEST_GROUP_ID)
    }
}