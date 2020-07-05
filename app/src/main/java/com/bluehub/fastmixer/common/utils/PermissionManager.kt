package com.bluehub.fastmixer.common.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat

object PermissionManager {
    const val AUDIO_RECORD_REQUEST = 12446

    val PERMISSIONS = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    fun requestRequiredPermissions(activity: Activity, tag: String) {
        Log.d(tag, "requestRecordPermission: ")
        ActivityCompat.requestPermissions(activity, PERMISSIONS, AUDIO_RECORD_REQUEST)
    }

    fun isRequiredPermissionsGranted(context: Context?, tag: String): Boolean {
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