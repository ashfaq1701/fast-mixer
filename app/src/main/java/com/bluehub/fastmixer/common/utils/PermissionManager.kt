package com.bluehub.fastmixer.common.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat

class PermissionManager private constructor() {
    companion object {
        const val REQUEST_GROUP_ID = 12446

        val allPermissions = arrayOf(Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE)

        fun create(): PermissionManager {
            return PermissionManager()
        }
    }

    fun isSpecifiedPermissionsGranted(permissions: Array<String>, context: Context?): Boolean {
        context?.let {
            return permissions.fold(true) {result, permission ->
                result && ActivityCompat
                    .checkSelfPermission(
                        context,
                        permission
                    ) == PackageManager.PERMISSION_GRANTED
            }
        }
        return false
    }

    fun isAllPermissionsGranted(context: Context?): Boolean {
        context?.let {
            return PermissionManager.allPermissions.fold(true) { result, permission ->
                result && ActivityCompat
                    .checkSelfPermission(
                        context,
                        permission
                    ) == PackageManager.PERMISSION_GRANTED
            }
        }
        return false
    }
}