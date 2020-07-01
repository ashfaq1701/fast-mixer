package com.bluehub.fastmixer

import android.app.Application
import com.bluehub.fastmixer.common.dependencyinjection.application.ApplicationComponent
import com.bluehub.fastmixer.common.dependencyinjection.application.ApplicationModule
import com.bluehub.fastmixer.common.dependencyinjection.application.DaggerApplicationComponent

class MixerApplication: Application() {
    private lateinit var mApplicationComponent: ApplicationComponent

    override fun onCreate() {
        super.onCreate()
        mApplicationComponent = DaggerApplicationComponent.builder()
            .applicationModule(
                ApplicationModule(
                    this
                )
            )
            .build()
    }

    fun getApplicationComponent(): ApplicationComponent = mApplicationComponent
}