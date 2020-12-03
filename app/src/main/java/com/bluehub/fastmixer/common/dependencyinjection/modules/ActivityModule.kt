package com.bluehub.fastmixer.common.dependencyinjection.modules

import android.content.Context
import com.bluehub.fastmixer.activities.MainActivity
import com.bluehub.fastmixer.common.dependencyinjection.ActivityScope
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector

@Module
interface ActivityModule {
    @ActivityScope
    @ContributesAndroidInjector(
        modules = [
            MainActivityFragmentModule::class
        ]
    )
    fun mainActivity(): MainActivity

    @Module
    companion object {
        @JvmStatic
        @Provides
        @ActivityScope
        fun getAudioManager(context: Context): Any? = context.getSystemService(Context.AUDIO_SERVICE)
    }
}