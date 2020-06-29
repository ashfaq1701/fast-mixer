package com.bluehub.fastmixer.common.dependencyinjection.presentation

import com.bluehub.fastmixer.screens.common.ViewModelFactory
import com.bluehub.fastmixer.screens.mixing.MixingViewModel
import dagger.Module
import dagger.Provides

@Module
class PresentationModule {
    @Provides
    fun viewModelFactory(mixingViewModel: MixingViewModel): ViewModelFactory = ViewModelFactory(mixingViewModel)

    @Provides
    fun mixingViewModel(): MixingViewModel = MixingViewModel()
}