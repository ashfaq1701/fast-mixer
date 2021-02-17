package com.bluehub.fastmixer.common.di.modules.screens

import androidx.lifecycle.ViewModel
import com.bluehub.fastmixer.common.di.FragmentScope
import com.bluehub.fastmixer.common.di.ViewModelKey
import com.bluehub.fastmixer.screens.mixing.GainAdjustmentViewModel
import com.bluehub.fastmixer.screens.mixing.SegmentAdjustmentViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
interface SegmentAdjustmentDialogModule {
    @Binds
    @IntoMap
    @FragmentScope
    @ViewModelKey(SegmentAdjustmentViewModel::class)
    fun segmentAdjustmentViewModel(viewModel: SegmentAdjustmentViewModel): ViewModel
}
