package com.bluehub.fastmixer.common.di.modules.screens

import androidx.lifecycle.ViewModel
import com.bluehub.fastmixer.common.di.FragmentScope
import com.bluehub.fastmixer.common.di.ViewModelKey
import com.bluehub.fastmixer.screens.mixing.MixingScreenViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
interface MixingScreenModule {
    @Binds
    @IntoMap
    @FragmentScope
    @ViewModelKey(MixingScreenViewModel::class)
    fun mixingScreenViewModel(viewModel: MixingScreenViewModel): ViewModel
}
