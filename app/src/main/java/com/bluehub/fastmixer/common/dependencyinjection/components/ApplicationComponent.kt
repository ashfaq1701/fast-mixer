package com.bluehub.fastmixer.common.dependencyinjection.components

import android.app.Application
import com.bluehub.fastmixer.MixerApplication
import com.bluehub.fastmixer.common.dependencyinjection.ApplicationScope
import com.bluehub.fastmixer.common.dependencyinjection.modules.application.ApplicationModule
import com.bluehub.fastmixer.common.dependencyinjection.modules.presentation.PresentationModule
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector

@ApplicationScope
@Component(modules = [
    AndroidInjectionModule::class,
    ApplicationModule::class,
    PresentationModule::class
])
interface ApplicationComponent: AndroidInjector<MixerApplication> {
    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance application: Application
        ): ApplicationComponent
    }
}