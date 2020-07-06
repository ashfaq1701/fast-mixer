package com.bluehub.fastmixer.common.permissions

import androidx.lifecycle.Observer
import com.bluehub.fastmixer.common.fragments.BaseFragment
import com.bluehub.fastmixer.common.utils.DialogManager
import com.bluehub.fastmixer.common.utils.PermissionManager
import android.Manifest
import android.os.Build
import android.util.Log

abstract class PermissionFragment: BaseFragment() {
    abstract var dialogManager: DialogManager

    abstract var viewModel: PermissionViewModel

    fun setPermissionEvents() {
        viewModel.eventRequestRecordPermission.observe(viewLifecycleOwner, Observer { requestRecordPermission ->
            if (requestRecordPermission.toRequest) {
                requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), requestRecordPermission.requestCode)
                viewModel.resetRequestRecordPermission()
            }
        })

        viewModel.eventRequestReadFilePermission.observe(viewLifecycleOwner, Observer { requestReadFilePermission ->
            if (requestReadFilePermission.toRequest) {
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), requestReadFilePermission.requestCode)
                viewModel.resetRequestReadFilePermission()
            }
        })

        viewModel.eventRequestWriteFilePermission.observe(viewLifecycleOwner, Observer { requestWriteFilePermission ->
            if (requestWriteFilePermission.toRequest) {
                requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), requestWriteFilePermission.requestCode)
                viewModel.resetRequestWriteFilePermission()
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
                    dialogManager.showPermissionsErrorDialog(context!!, Manifest.permission.RECORD_AUDIO, neverAskAgain)
                }
                viewModel.hideRecordingPermissionDialog()
            }
        })

        viewModel.eventShowReadFilePermissionDialog.observe(viewLifecycleOwner, Observer { showReadFilePermissionDialog ->
            if (showReadFilePermissionDialog) {
                val neverAskAgain = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    !shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)
                } else {
                    false
                }

                context?.let {
                    dialogManager.showPermissionsErrorDialog(context!!, Manifest.permission.READ_EXTERNAL_STORAGE, neverAskAgain)
                }
                viewModel.hideReadFilePermissionDialog()
            }
        })

        viewModel.eventShowWriteFilePermissionDialog.observe(viewLifecycleOwner, Observer { showWriteFilePermissionDialog ->
            if (showWriteFilePermissionDialog) {
                val neverAskAgain = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    !shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                } else {
                    false
                }

                context?.let {
                    dialogManager.showPermissionsErrorDialog(
                        context!!,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        neverAskAgain)
                }
                viewModel.hideWriteFilePermissionDialog()
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
                Manifest.permission.READ_EXTERNAL_STORAGE -> viewModel.setEventRequestReadFilePermission(
                    PermissionHolder(hasPermission = true, permissionCode = requestCode, fromCallback = true))
                Manifest.permission.WRITE_EXTERNAL_STORAGE -> viewModel.setEventRequestWriteFilePermission(
                    PermissionHolder(hasPermission = true, permissionCode = requestCode, fromCallback = true)
                )
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
                    Manifest.permission.READ_EXTERNAL_STORAGE -> viewModel.setEventRequestReadFilePermission(
                        PermissionHolder(hasPermission = false, permissionCode = requestCode, fromCallback = true)
                    )
                    Manifest.permission.WRITE_EXTERNAL_STORAGE -> viewModel.setEventRequestWriteFilePermission(
                        PermissionHolder(hasPermission = false, permissionCode = requestCode, fromCallback = true)
                    )
                }
            }

            when(notGrantedPermissions.first()) {
                Manifest.permission.RECORD_AUDIO -> viewModel.showRecordingPermissionDialog()
                Manifest.permission.WRITE_EXTERNAL_STORAGE -> viewModel.showWriteFilePermissionDialog()
                Manifest.permission.READ_EXTERNAL_STORAGE -> viewModel.showReadFilePermissionDialog()
            }
        }
    }
}