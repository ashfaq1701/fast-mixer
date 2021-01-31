package com.bluehub.fastmixer.common.di.modules

import android.app.Application
import android.content.Context
import com.bluehub.fastmixer.common.di.ApplicationScope
import com.bluehub.fastmixer.screens.mixing.*
import dagger.*

@Module
interface AppModule {
    @Binds
    @ApplicationScope
    fun provideContext(application: Application): Context

    @Module
    companion object {
        @JvmStatic
        @Provides
        @ApplicationScope
        fun audioFileStore(): AudioFileStore = AudioFileStore()

        @JvmStatic
        @Provides
        @ApplicationScope
        fun playFlagStore(): PlayFlagStore = PlayFlagStore()

        @JvmStatic
        @Provides
        @ApplicationScope
        fun fileWaveViewStore(): FileWaveViewStore = FileWaveViewStore()
    }
}
