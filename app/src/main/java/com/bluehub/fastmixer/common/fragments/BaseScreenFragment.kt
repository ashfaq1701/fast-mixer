package com.bluehub.fastmixer.common.fragments

import android.os.Build
import android.util.Log
import com.bluehub.fastmixer.common.utils.DialogManager
import com.bluehub.fastmixer.common.utils.PermissionManager

abstract class BaseScreenFragment: BaseFragment() {
    abstract fun disableControls()

    abstract fun enableControls()

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        Log.d(TAG, "onRequestPermissionsResult: requestCode = $requestCode, ")
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (PermissionManager.AUDIO_RECORD_REQUEST != requestCode) {
            return
        }

        // handle the case when user clicks on "Don't ask again"
        // inside permission dialog
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (!shouldShowRequestPermissionRationale(PermissionManager.PERMISSIONS[0]) ||
                !shouldShowRequestPermissionRationale(PermissionManager.PERMISSIONS[1]) ||
                !shouldShowRequestPermissionRationale(PermissionManager.PERMISSIONS[2])) {

                Log.w(TAG, "onRequestPermissionsResult: Some Permission(s) not granted, show error dialog.")
                context?.let {
                    DialogManager.showPermissionsErrorDialog(context!!)
                }
                return
            }
        }

        if (!PermissionManager.isRequiredPermissionsGranted(context, TAG)) {
            Log.w(TAG, "onRequestPermissionsResult: Some Permission(s) not granted, disable controls")
            disableControls()

        } else {
            Log.i(TAG, "onRequestPermissionsResult: ALL Permissions granted, continue with enableControls")
            enableControls()
        }
    }
}