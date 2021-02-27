package com.bluehub.fastmixer.common.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.view.View
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModel
import com.bluehub.fastmixer.R

abstract class BaseDialogFragment <T: ViewModel> : DialogFragment() {

    protected abstract val viewModel: T

    protected fun createDialog(view: View) : Dialog {
        return activity?.let { parentActivity ->
            val builder = AlertDialog.Builder(parentActivity, R.style.ThemeOverlay_AppCompat_Dialog)
            builder.setView(view)
            val dialog = builder.create()

            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCanceledOnTouchOutside(false)

            dialog
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    protected fun closeDialog() {
        dialog?.dismiss()
    }
}
