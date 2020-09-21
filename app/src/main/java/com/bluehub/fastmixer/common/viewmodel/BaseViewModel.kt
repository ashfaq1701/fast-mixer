package com.bluehub.fastmixer.common.viewmodel

import androidx.databinding.Observable
import androidx.databinding.PropertyChangeRegistry
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import com.bluehub.fastmixer.MixerApplication
import com.bluehub.fastmixer.common.dependencyinjection.viewmodel.DaggerViewModelComponent
import com.bluehub.fastmixer.common.dependencyinjection.viewmodel.ViewModelComponent
import com.bluehub.fastmixer.common.dependencyinjection.viewmodel.ViewModelModule

open class BaseViewModel(mixerApplication: MixerApplication): AndroidViewModel(mixerApplication), Observable {
    open var TAG: String = ""

    private lateinit var mViewModelComponent: ViewModelComponent

    @delegate:Transient
    private val mCallBacks: PropertyChangeRegistry by lazy { PropertyChangeRegistry() }

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

    override fun addOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback) {
        mCallBacks.add(callback)
    }

    override fun removeOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback) {
        mCallBacks.remove(callback)
    }

    fun notifyChange() {
        mCallBacks.notifyChange(this, 0)
    }

    fun notifyChange(viewId:Int){
        mCallBacks.notifyChange(this, viewId)
    }

    open fun notifyPropertyChanged(fieldId: Int) {
        mCallBacks.notifyCallbacks(this, fieldId, null)
    }
}