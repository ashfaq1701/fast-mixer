package com.bluehub.fastmixer.common.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import javax.inject.Inject
import javax.inject.Provider

@Suppress("UNCHECKED_CAST")
open class ViewModelFactory(
        private val creators: Map<Class<out ViewModel>, @JvmSuppressWildcards Provider<ViewModel>>
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        var creator: Provider<out ViewModel>? = creators[modelClass]
        if (creator == null) {
            for (entry in creators.entries) {
                if (modelClass.isAssignableFrom(entry.key)) {
                    creator = entry.value
                    break
                }
            }
        }
        requireNotNull(creator) { "unknown model class $modelClass" }

        try {
            return creator.get() as T
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }
}

@FragmentScope
class FragmentViewModelFactory @Inject constructor(
        creators: Map<Class<out ViewModel>, @JvmSuppressWildcards Provider<ViewModel>>
) : ViewModelFactory(creators)
