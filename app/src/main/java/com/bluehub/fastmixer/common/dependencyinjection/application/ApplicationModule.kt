package com.bluehub.fastmixer.common.dependencyinjection.application

import android.app.Application
import android.content.Context
import android.media.AudioManager
import com.bluehub.fastmixer.common.audio.AudioEngineProxy
import dagger.Module
import dagger.Provides

@Module
class ApplicationModule(var mApplication: Application) {
    @Provides
    fun getAudioEngine(): AudioEngineProxy = AudioEngineProxy.getInstance()

    @Provides
    fun getAudioManager(): Any? = mApplication.applicationContext.getSystemService(Context.AUDIO_SERVICE)
}