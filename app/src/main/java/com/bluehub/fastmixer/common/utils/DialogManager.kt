package com.bluehub.fastmixer.common.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.provider.Settings.Global.getString
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bluehub.fastmixer.R
import javax.inject.Inject

class DialogManager {
    private val tag = DialogManager::class.java.simpleName

    fun showPermissionsErrorDialog(context: Context, permission: String, neverAskAgain: Boolean) {
        if (!((context as AppCompatActivity).isFinishing)) {

            var dialogTheme = android.R.style.Theme_Holo_Light_Dialog

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Log.i(tag, "showPermissionsErrorDialog: All permissions are not allowed, Show error dialog, Build.VERSION.SDK_INT >= 23")
                dialogTheme = android.R.style.Theme_Material_Light_Dialog
            }

            val builder = AlertDialog.Builder(context, dialogTheme)

            val message = when(permission) {
                Manifest.permission.RECORD_AUDIO -> context.getString(R.string.recording_permission_needed)
                Manifest.permission.READ_EXTERNAL_STORAGE -> context.getString(R.string.read_external_storage_permission_needed)
                Manifest.permission.WRITE_EXTERNAL_STORAGE -> context.getString(R.string.write_external_storage_permission_needed)
                else -> ""
            }

            builder.setTitle("Permission Required")
            builder.setMessage(message)
            builder.setCancelable(false)

            if (neverAskAgain) {
                builder.setPositiveButton("Go to Settings") { dialog, _ ->
                    dialog.dismiss()
                    openAppSettingsPage(context)
                }
            }

            builder.setNegativeButton("Cancel") { dialog, _ ->
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