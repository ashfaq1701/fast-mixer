package com.bluehub.fastmixer.common.permissions

import androidx.lifecycle.Observer
import com.bluehub.fastmixer.common.fragments.BaseFragment
import com.bluehub.fastmixer.common.utils.DialogManager
import android.Manifest
import android.os.Build
import com.bluehub.fastmixer.common.models.ViewModelType

abstract class PermissionControlFragment<T: PermissionControlViewModel>(viewModelType: ViewModelType): BaseFragment<T>(viewModelType) {
    abstract var dialogManager: DialogManager

    fun setPermissionEvents() {
        viewModel.eventRequestRecordPermission.observe(viewLifecycleOwner, Observer { requestRecordPermission ->
            if (requestRecordPermission.toRequest) {
                requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), requestRecordPermission.requestCode)
                viewModel.resetRequestRecordPermission()
            }
        })

        viewModel.eventShowRecordingPermissionDialog.observe(viewLifecycleOwner, Observer { showRecordingPermissionDialog ->
            if (showRecordingPermissionDialog) {
                val neverAskAgain = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    !shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)
                } else {
                    false
                }

                context?.let {
                    dialogManager.showPermissionsErrorDialog(requireContext(), Manifest.permission.RECORD_AUDIO, neverAskAgain)
                }
                viewModel.hideRecordingPermissionDialog()
            }
        })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        val notGranted = grantResults.foldIndexed(emptyArray<Int>()) { idx, arr, i ->
            if (i == -1) {
                return@foldIndexed arr.plus(idx)
            }
            return@foldIndexed arr
        }

        val granted = grantResults.foldIndexed(emptyArray<Int>()) { idx, arr, i ->
            if (i == 0) {
                return@foldIndexed arr.plus(idx)
            }
            return@foldIndexed arr
        }

        val grantedPermissions = granted.map {
            permissions[it]
        }

        grantedPermissions.forEach { permission ->
            when(permission) {
                Manifest.permission.RECORD_AUDIO -> viewModel.setEventRecordPermission(
                    PermissionHolder(hasPermission = true, permissionCode = requestCode, fromCallback = true))
            }
        }

        if (notGranted.isNotEmpty()) {
            val notGrantedPermissions = notGranted.map {
                permissions[it]
            }

            notGrantedPermissions.forEach { permission ->
                when (permission) {
                    Manifest.permission.RECORD_AUDIO -> viewModel.setEventRecordPermission(
                        PermissionHolder(hasPermission = false, permissionCode = requestCode, fromCallback = true)
                    )
                }
            }

            when(notGrantedPermissions.first()) {
                Manifest.permission.RECORD_AUDIO -> viewModel.showRecordingPermissionDialog()
            }
        }
    }
}
