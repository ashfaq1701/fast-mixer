package com.bluehub.fastmixer.common.dependencyinjection.application

import com.bluehub.fastmixer.common.dependencyinjection.presentation.PresentationComponent
import com.bluehub.fastmixer.common.dependencyinjection.presentation.PresentationModule
import dagger.Component

@Component(modules = [ApplicationModule::class])
interface ApplicationComponent {
    fun newPresentationComponent(presentationModule: PresentationModule): PresentationComponent
}