package com.bluehub.fastmixer

import android.app.Application
import com.bluehub.fastmixer.common.audio.AudioEngine
import com.bluehub.fastmixer.common.audio.AudioEngineProxy
import com.bluehub.fastmixer.common.dependencyinjection.application.ApplicationComponent
import com.bluehub.fastmixer.common.dependencyinjection.application.ApplicationModule
import com.bluehub.fastmixer.common.dependencyinjection.application.DaggerApplicationComponent
import javax.inject.Inject

class MixerApplication: Application() {
    private lateinit var mApplicationComponent: ApplicationComponent

    @Inject
    lateinit var mAudioEngine: AudioEngineProxy

    override fun onCreate() {
        super.onCreate()
        mApplicationComponent = DaggerApplicationComponent.builder()
            .applicationModule(
                ApplicationModule(
                    this
                )
            )
            .build()
        mApplicationComponent.inject(this)
    }

    fun getApplicationComponent(): ApplicationComponent = mApplicationComponent

    fun getAudioEngine(): AudioEngineProxy = mAudioEngine
}