package com.bluehub.fastmixer.common.dependencyinjection.presentation

import com.bluehub.fastmixer.broadcastReceivers.AudioDeviceChangeListener
import com.bluehub.fastmixer.common.repositories.AudioRepository
import com.bluehub.fastmixer.common.utils.DialogManager
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class PresentationModule {
    @Provides
    fun dialogManager(): DialogManager = DialogManager.create()
}