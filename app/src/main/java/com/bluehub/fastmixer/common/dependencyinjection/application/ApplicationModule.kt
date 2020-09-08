package com.bluehub.fastmixer.common.dependencyinjection.application

import android.app.Application
import android.content.Context
import com.bluehub.fastmixer.screens.recording.RecordingEngineProxy
import dagger.Module
import dagger.Provides

@Module
class ApplicationModule(var mApplication: Application) {
    @Provides
    fun getAudioEngine(): RecordingEngineProxy = RecordingEngineProxy.getInstance()

    @Provides
    fun getAudioManager(): Any? = mApplication.applicationContext.getSystemService(Context.AUDIO_SERVICE)
}