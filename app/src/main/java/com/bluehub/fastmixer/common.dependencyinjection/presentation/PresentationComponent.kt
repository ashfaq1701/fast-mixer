package com.bluehub.fastmixer.common.dependencyinjection.presentation

import com.bluehub.fastmixer.screens.mixing.MixingFragment
import dagger.Subcomponent

@Subcomponent(modules = [PresentationModule::class])
interface PresentationComponent {
    fun inject(mixingFragment: MixingFragment)
}