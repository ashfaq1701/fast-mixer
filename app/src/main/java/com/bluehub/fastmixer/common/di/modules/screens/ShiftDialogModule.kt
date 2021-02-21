package com.bluehub.fastmixer.common.di.modules.screens

import androidx.lifecycle.ViewModel
import com.bluehub.fastmixer.common.di.FragmentScope
import com.bluehub.fastmixer.common.di.ViewModelKey
import com.bluehub.fastmixer.screens.mixing.modals.ShiftViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
interface ShiftDialogModule {

    @Binds
    @IntoMap
    @FragmentScope
    @ViewModelKey(ShiftViewModel::class)
    fun shiftViewModel(viewModel: ShiftViewModel): ViewModel
}
