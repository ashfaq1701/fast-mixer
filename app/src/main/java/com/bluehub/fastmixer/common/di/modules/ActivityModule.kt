package com.bluehub.fastmixer.common.di.modules

import android.content.Context
import com.bluehub.fastmixer.activities.MainActivity
import com.bluehub.fastmixer.common.di.ActivityScope
import com.bluehub.fastmixer.common.di.ApplicationScope
import com.bluehub.fastmixer.screens.mixing.AudioFileStore
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import javax.inject.Singleton

@Module
interface ActivityModule {
    @ActivityScope
    @ContributesAndroidInjector(
        modules = [
            MainActivityFragmentModule::class,
            FileStoresModule::class
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
