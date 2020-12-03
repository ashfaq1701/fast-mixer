package com.bluehub.fastmixer.common.dependencyinjection.modules.application

import android.app.Application
import android.content.Context
import com.bluehub.fastmixer.common.dependencyinjection.ApplicationScope
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module
interface ApplicationModule {
    @Binds
    @ApplicationScope
    fun provideContext(application: Application): Context

    @Module
    companion object {
        @JvmStatic
        @Provides
        @ApplicationScope
        fun getAudioManager(context: Context): Any? = context.getSystemService(Context.AUDIO_SERVICE)
    }
}