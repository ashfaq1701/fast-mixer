package com.bluehub.mixi.common.fragments

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.addTo


abstract class BaseFragment<T: ViewModel> : Fragment() {

    private val disposables = CompositeDisposable()

    protected abstract val viewModel: T

    protected fun Disposable.addToDisposables() = addTo(disposables)

    fun openAppSettingsPage() {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri = Uri.fromParts("package", requireContext().packageName, null)
        intent.data = uri
        requireContext().startActivity(intent)
    }
}
