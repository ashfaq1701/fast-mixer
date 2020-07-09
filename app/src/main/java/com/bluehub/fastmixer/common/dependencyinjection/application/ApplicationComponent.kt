package com.bluehub.fastmixer.common.dependencyinjection.application

import com.bluehub.fastmixer.MixerApplication
import com.bluehub.fastmixer.common.dependencyinjection.presentation.PresentationComponent
import com.bluehub.fastmixer.common.dependencyinjection.presentation.PresentationModule
import com.bluehub.fastmixer.screens.mixing.MixingScreen
import com.bluehub.fastmixer.screens.recording.RecordingScreen
import dagger.Component

@Component(modules = [ApplicationModule::class])
interface ApplicationComponent {
    fun inject(mApplication: MixerApplication)

    fun newPresentationComponent(presentationModule: PresentationModule): PresentationComponent
}