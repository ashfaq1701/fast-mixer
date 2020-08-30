package com.bluehub.fastmixer.common.dependencyinjection.viewmodel

import com.bluehub.fastmixer.broadcastReceivers.AudioDeviceChangeListener
import com.bluehub.fastmixer.common.audio.AudioEngineProxy
import com.bluehub.fastmixer.common.repositories.AudioRepository
import com.bluehub.fastmixer.common.utils.PermissionManager
import com.bluehub.fastmixer.screens.mixing.MixingRepository
import com.bluehub.fastmixer.screens.recording.RecordingRepository
import dagger.Module
import dagger.Provides

@Module
class ViewModelModule {
    @Provides
    fun permissionManager(): PermissionManager = PermissionManager.create()

    @Provides
    fun getAudioEngine(): AudioEngineProxy = AudioEngineProxy.getInstance()

    @Provides
    fun getRecordingRepository(audioEngineProxy: AudioEngineProxy): RecordingRepository = RecordingRepository(audioEngineProxy)

    @Provides
    fun getMixingRepository(audioEngineProxy: AudioEngineProxy): MixingRepository = MixingRepository(audioEngineProxy)

    @Provides
    fun getAudioRepository(): AudioRepository = AudioRepository()

    @Provides
    fun audioDeviceChangeListener(audioRepository: AudioRepository): AudioDeviceChangeListener = AudioDeviceChangeListener(audioRepository)
}