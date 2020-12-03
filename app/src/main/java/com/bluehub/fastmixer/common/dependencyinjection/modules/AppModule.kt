package com.bluehub.fastmixer.common.dependencyinjection.modules

import android.app.Application
import android.content.Context
import com.bluehub.fastmixer.common.dependencyinjection.ApplicationScope
import dagger.Binds
import dagger.Module

@Module
interface AppModule {
    @Binds
    @ApplicationScope
    fun provideContext(application: Application): Context
}