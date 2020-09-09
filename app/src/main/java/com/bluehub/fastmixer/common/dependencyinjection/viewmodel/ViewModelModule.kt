package com.bluehub.fastmixer.common.dependencyinjection.viewmodel

import com.bluehub.fastmixer.broadcastReceivers.AudioDeviceChangeListener
import com.bluehub.fastmixer.screens.recording.RecordingEngineProxy
import com.bluehub.fastmixer.common.repositories.AudioRepository
import com.bluehub.fastmixer.common.utils.PermissionManager
import com.bluehub.fastmixer.screens.mixing.MixingEngineProxy
import com.bluehub.fastmixer.screens.mixing.MixingRepository
import com.bluehub.fastmixer.screens.recording.RecordingRepository
import dagger.Module
import dagger.Provides

@Module
class ViewModelModule {
    @Provides
    fun permissionManager(): PermissionManager = PermissionManager.create()

    @Provides
    fun getRecordingEngine(): RecordingEngineProxy = RecordingEngineProxy.getInstance()

    @Provides
    fun getMixingEngine(): MixingEngineProxy = MixingEngineProxy.getInstance()

    @Provides
    fun getRecordingRepository(recordingEngineProxy: RecordingEngineProxy): RecordingRepository = RecordingRepository(recordingEngineProxy)

    @Provides
    fun getMixingRepository(mixingEngineProxy: MixingEngineProxy): MixingRepository = MixingRepository(mixingEngineProxy)

    @Provides
    fun getAudioRepository(): AudioRepository = AudioRepository()

    @Provides
    fun audioDeviceChangeListener(audioRepository: AudioRepository): AudioDeviceChangeListener = AudioDeviceChangeListener(audioRepository)
}