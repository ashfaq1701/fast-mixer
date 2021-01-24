package com.bluehub.fastmixer.common.di.modules

import com.bluehub.fastmixer.common.di.FragmentScope
import com.bluehub.fastmixer.common.di.modules.screens.MixingScreenModule
import com.bluehub.fastmixer.common.di.modules.screens.RecordingScreenModule
import com.bluehub.fastmixer.screens.mixing.MixingScreen
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
}
