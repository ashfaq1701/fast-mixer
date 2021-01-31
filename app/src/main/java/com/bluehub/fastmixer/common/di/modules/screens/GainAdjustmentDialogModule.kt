package com.bluehub.fastmixer.common.di.modules.screens

import androidx.lifecycle.ViewModel
import com.bluehub.fastmixer.common.di.FragmentScope
import com.bluehub.fastmixer.common.di.ViewModelKey
import com.bluehub.fastmixer.screens.mixing.GainAdjustmentViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
interface GainAdjustmentDialogModule {
    @Binds
    @IntoMap
    @FragmentScope
    @ViewModelKey(GainAdjustmentViewModel::class)
    fun gainAdjustmentViewModel(viewModel: GainAdjustmentViewModel): ViewModel
}
