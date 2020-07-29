package com.bluehub.fastmixer.common.dependencyinjection.presentation

import com.bluehub.fastmixer.common.audio.AudioDeviceChangeListener
import com.bluehub.fastmixer.common.audio.AudioEngineProxy
import com.bluehub.fastmixer.common.utils.DialogManager
import dagger.Module
import dagger.Provides

@Module
class PresentationModule {
    @Provides
    fun dialogManager(): DialogManager = DialogManager.create()

    @Provides
    fun audioDeviceChangeListener(): AudioDeviceChangeListener = AudioDeviceChangeListener()
}