package com.bluehub.fastmixer.common.fragments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bluehub.fastmixer.common.dependencyinjection.DaggerFragment
import com.bluehub.fastmixer.common.dependencyinjection.FragmentViewModelFactory
import javax.inject.Inject
import kotlin.reflect.KClass


abstract class BaseFragment<T: ViewModel>: DaggerFragment() {
    abstract var TAG: String

    @Inject
    lateinit var viewModelFactory: FragmentViewModelFactory

    protected val viewModel: T by lazy { viewModel() }

    protected abstract val viewModelClass: KClass<T>

    fun viewModel(): T =
            ViewModelProvider(this, viewModelFactory)[viewModelClass.java]
}