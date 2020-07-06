package com.bluehub.fastmixer.common.viewmodel

import androidx.lifecycle.ViewModel
import com.bluehub.fastmixer.common.dependencyinjection.viewmodel.DaggerViewModelComponent
import com.bluehub.fastmixer.common.dependencyinjection.viewmodel.ViewModelComponent
import com.bluehub.fastmixer.common.dependencyinjection.viewmodel.ViewModelModule

open class BaseViewModel: ViewModel() {

    private lateinit var mViewModelComponent: ViewModelComponent

    fun getViewModelComponent(): ViewModelComponent {
        if (::mViewModelComponent.isInitialized) {
            return mViewModelComponent
        }
        mViewModelComponent = DaggerViewModelComponent.builder()
            .viewModelModule(
                ViewModelModule()
            )
            .build()
        return mViewModelComponent
    }
}