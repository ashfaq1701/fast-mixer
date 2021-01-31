package com.bluehub.fastmixer.common.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.view.View
import android.view.Window
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bluehub.fastmixer.R
import com.bluehub.fastmixer.common.di.FragmentViewModelFactory
import dagger.android.support.DaggerDialogFragment
import javax.inject.Inject
import kotlin.reflect.KClass

abstract class BaseDialogFragment <T: ViewModel>() : DaggerDialogFragment() {

    protected abstract val viewModelClass: KClass<T>

    @Inject
    lateinit var viewModelFactory: FragmentViewModelFactory

    protected val viewModel: T by lazy {
        ViewModelProvider(this, viewModelFactory)[viewModelClass.java]
    }

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
