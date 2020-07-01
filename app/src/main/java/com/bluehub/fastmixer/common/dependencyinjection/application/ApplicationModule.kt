package com.bluehub.fastmixer.common.dependencyinjection.application

import android.app.Application
import dagger.Module

@Module
class ApplicationModule(var mApplication: Application) {
}