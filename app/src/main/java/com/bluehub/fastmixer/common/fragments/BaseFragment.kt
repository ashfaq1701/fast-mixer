package com.bluehub.fastmixer.common.fragments

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bluehub.fastmixer.R
import com.bluehub.fastmixer.common.di.DaggerFragment
import com.bluehub.fastmixer.common.di.FragmentViewModelFactory
import com.bluehub.fastmixer.common.models.ViewModelType
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.addTo
import javax.inject.Inject
import kotlin.reflect.KClass


abstract class BaseFragment<T: ViewModel>(viewModelType: ViewModelType = ViewModelType.FRAGMENT_SCOPED): DaggerFragment() {

    @Inject
    lateinit var viewModelFactory: FragmentViewModelFactory

    private val disposables = CompositeDisposable()

    protected val viewModel: T by lazy {
        when (viewModelType) {
            ViewModelType.FRAGMENT_SCOPED -> fragmentScopedViewModel()
            ViewModelType.NAV_SCOPED -> navScopedViewModel()
        }
    }

    protected abstract val viewModelClass: KClass<T>

    private fun fragmentScopedViewModel(): T =
            ViewModelProvider(this, viewModelFactory)[viewModelClass.java]

    private fun navScopedViewModel(): T {
        val viewModelStoreOwner = findNavController().getViewModelStoreOwner(R.id.nav_graph)
        return ViewModelProvider(viewModelStoreOwner, viewModelFactory)[viewModelClass.java]
    }

    protected fun Disposable.addToDisposables() = addTo(disposables)

    fun openAppSettingsPage() {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri = Uri.fromParts("package", requireContext().packageName, null)
        intent.data = uri
        requireContext().startActivity(intent)
    }
}
