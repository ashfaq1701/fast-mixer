package com.bluehub.fastmixer.common.di.modules.screens

import androidx.lifecycle.ViewModel
import com.bluehub.fastmixer.common.di.FragmentScope
import com.bluehub.fastmixer.common.di.ViewModelKey
import com.bluehub.fastmixer.screens.mixing.PlayViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
interface PlayFragmentModule {
    @Binds
    @IntoMap
    @FragmentScope
    @ViewModelKey(PlayViewModel::class)
    fun playViewModel(viewModel: PlayViewModel): ViewModel
}
