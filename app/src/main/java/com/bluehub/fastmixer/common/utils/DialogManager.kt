package com.bluehub.fastmixer.common.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

object DialogManager {
    private val TAG = DialogManager::class.java.simpleName

    fun showPermissionsErrorDialog(context: Context) {

        Log.d(TAG, "showPermissionsErrorDialog(): ")

        if (!((context as AppCompatActivity).isFinishing)) {

            var dialogTheme = android.R.style.Theme_Holo_Light_Dialog

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Log.i(TAG, "showPermissionsErrorDialog: All permissions are not allowed, Show error dialog, Build.VERSION.SDK_INT >= 23")
                dialogTheme = android.R.style.Theme_Material_Light_Dialog
            }

            val builder = AlertDialog.Builder(context, dialogTheme)

            builder.setTitle("Permissions")
            builder.setMessage("Need some permissions for app to work correctly")
            builder.setCancelable(false)

            builder.setPositiveButton("Go to Settings") { dialog, _ ->
                Log.d(TAG, "positiveButton::onClick: ")
                dialog.dismiss()
                openAppSettingsPage(context)
            }

            builder.setNegativeButton("Cancel") { dialog, _ ->
                Log.d(TAG, "negativeButton::onClick: ")
                dialog.dismiss()
            }

            builder.show()
        }

    }

    fun openAppSettingsPage(context: Context) {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri = Uri.fromParts("package", context.packageName, null)
        intent.data = uri
        context.startActivity(intent)
    }
}