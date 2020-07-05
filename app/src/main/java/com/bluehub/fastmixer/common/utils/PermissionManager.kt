package com.bluehub.fastmixer.common.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat

object PermissionManager {
    const val REQUEST_GROUP_ID = 12446

    fun isRecordPermissionGranted(context: Context?, tag: String): Boolean {
        context?.let {
           return ActivityCompat
               .checkSelfPermission(
                   context,
                   Manifest.permission.RECORD_AUDIO
               ) == PackageManager.PERMISSION_GRANTED
        }
        return false
    }

    fun isReadStoragePermissionGranted(context: Context?, tag: String): Boolean {
        context?.let {
            return ActivityCompat
                .checkSelfPermission(
                    context,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
        }
        return false
    }

    fun isWriteStoragePermissionGranted(context: Context?, tag: String): Boolean {
        context?.let {
            return ActivityCompat
                .checkSelfPermission(
                    context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
        }
        return false
    }

    fun isAllPermissionsGranted(context: Context?, tag: String): Boolean {
        context?.let {
            val permissionStatus = (ActivityCompat
                .checkSelfPermission(
                    context,
                    Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED) &&
                    (ActivityCompat
                        .checkSelfPermission(
                            context,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        ) == PackageManager.PERMISSION_GRANTED) &&
                    (ActivityCompat
                        .checkSelfPermission(
                            context,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ) == PackageManager.PERMISSION_GRANTED)

            Log.d(tag, "isRequiredPermissionsGranted: $permissionStatus")

            return permissionStatus
        }

        return false
    }
}