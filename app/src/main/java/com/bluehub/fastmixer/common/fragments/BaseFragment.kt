package com.bluehub.fastmixer.common.fragments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bluehub.fastmixer.R
import com.bluehub.fastmixer.common.dependencyinjection.DaggerFragment
import com.bluehub.fastmixer.common.dependencyinjection.FragmentViewModelFactory
import com.bluehub.fastmixer.common.utils.ViewModelType
import javax.inject.Inject
import kotlin.reflect.KClass


abstract class BaseFragment<T: ViewModel>(viewModelType: ViewModelType = ViewModelType.FRAGMENT_SCOPED): DaggerFragment() {
    abstract var TAG: String

    @Inject
    lateinit var viewModelFactory: FragmentViewModelFactory

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
}