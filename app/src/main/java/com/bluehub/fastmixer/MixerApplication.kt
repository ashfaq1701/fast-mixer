package com.bluehub.fastmixer

import android.app.Application
import com.bluehub.fastmixer.common.dependencyinjection.application.ApplicationComponent
import com.bluehub.fastmixer.common.dependencyinjection.application.ApplicationModule
import com.bluehub.fastmixer.common.dependencyinjection.application.DaggerApplicationComponent
import timber.log.Timber;

class MixerApplication: Application() {
    private lateinit var mApplicationComponent: ApplicationComponent

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
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
}