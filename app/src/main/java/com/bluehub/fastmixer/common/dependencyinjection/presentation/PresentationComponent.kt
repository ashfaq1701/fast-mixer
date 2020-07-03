package com.bluehub.fastmixer.common.dependencyinjection.presentation

import com.bluehub.fastmixer.fragments.recording.RecordingFragment
import com.bluehub.fastmixer.screens.mixing.MixingScreen
import com.bluehub.fastmixer.screens.recording.RecordingScreen
import dagger.Subcomponent

@Subcomponent(modules = [PresentationModule::class])
interface PresentationComponent {
    fun inject(mixingScreen: MixingScreen)

    fun inject(recordingScreen: RecordingScreen)

    fun inject(recordingFragment: RecordingFragment)
}