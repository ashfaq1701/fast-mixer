package com.bluehub.fastmixer.common.fragments

import androidx.annotation.UiThread
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bluehub.fastmixer.MixerApplication
import com.bluehub.fastmixer.R
import com.bluehub.fastmixer.common.dependencyinjection.application.ApplicationComponent
import com.bluehub.fastmixer.common.dependencyinjection.presentation.PresentationComponent
import com.bluehub.fastmixer.common.dependencyinjection.presentation.PresentationModule


abstract class BaseFragment: Fragment() {
    abstract var TAG: String

    private var mIsInjectorUsed = false
    @UiThread
    fun getPresentationComponent(): PresentationComponent {
        if (mIsInjectorUsed) {
            throw RuntimeException("there is no need to use injector more than once")
        }

        val navStoreOwner = findNavController().getViewModelStoreOwner(R.id.nav_graph)

        mIsInjectorUsed = true
        return getApplicationComponent()
            .newPresentationComponent(PresentationModule(requireContext(), this, navStoreOwner))
    }

    private fun getApplicationComponent(): ApplicationComponent {
        return (requireActivity().getApplication() as MixerApplication).getApplicationComponent()
    }
}