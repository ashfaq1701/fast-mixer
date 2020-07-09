package com.bluehub.fastmixer.common.dependencyinjection.application

import android.app.Application
import com.bluehub.fastmixer.common.audio.AudioEngineProxy
import dagger.Module
import dagger.Provides

@Module
class ApplicationModule(var mApplication: Application) {
    @Provides
    fun getAudioEngine(): AudioEngineProxy = AudioEngineProxy()
}