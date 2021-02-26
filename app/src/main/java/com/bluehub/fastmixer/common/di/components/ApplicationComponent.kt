package com.bluehub.fastmixer.common.di.components

import android.app.Application
import com.bluehub.fastmixer.MixerApplication
import com.bluehub.fastmixer.common.di.ApplicationScope
import com.bluehub.fastmixer.common.di.modules.*
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector

@ApplicationScope
@Component(modules = [
    AndroidInjectionModule::class,
    AppModule::class,
    ActivityModule::class,
    FileStoresModule::class
])
interface ApplicationComponent: AndroidInjector<MixerApplication> {
    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance application: Application
        ): ApplicationComponent
    }
}
