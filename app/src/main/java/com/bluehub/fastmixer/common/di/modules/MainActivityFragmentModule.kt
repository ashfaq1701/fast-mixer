package com.bluehub.fastmixer.common.di.modules

import com.bluehub.fastmixer.common.di.FragmentScope
import com.bluehub.fastmixer.common.di.modules.screens.*
import com.bluehub.fastmixer.screens.mixing.*
import com.bluehub.fastmixer.screens.recording.RecordingScreen
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
interface MainActivityFragmentModule {
    @FragmentScope
    @ContributesAndroidInjector(modules = [MixingScreenModule::class])
    fun mixingScreen(): MixingScreen

    @FragmentScope
    @ContributesAndroidInjector(modules = [RecordingScreenModule::class])
    fun recordingScreen(): RecordingScreen

    @FragmentScope
    @ContributesAndroidInjector(modules = [GainAdjustmentDialogModule::class])
    fun gainAdjustmentDialog(): GainAdjustmentDialog

    @FragmentScope
    @ContributesAndroidInjector(modules = [SegmentAdjustmentDialogModule::class])
    fun segmentAdjustmentDialog(): SegmentAdjustmentDialog

    @FragmentScope
    @ContributesAndroidInjector(modules = [PlayFragmentModule::class])
    fun playFragment(): PlayFragment
}
