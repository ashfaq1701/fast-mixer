package com.bluehub.fastmixer.common.dependencyinjection.modules.presentation

import androidx.lifecycle.ViewModel
import com.bluehub.fastmixer.broadcastReceivers.AudioDeviceChangeListener
import com.bluehub.fastmixer.common.dependencyinjection.ApplicationScope
import com.bluehub.fastmixer.common.dependencyinjection.FragmentScope
import com.bluehub.fastmixer.common.dependencyinjection.ViewModelKey
import com.bluehub.fastmixer.common.repositories.AudioRepository
import com.bluehub.fastmixer.common.utils.DialogManager
import com.bluehub.fastmixer.common.utils.PermissionManager
import com.bluehub.fastmixer.screens.mixing.MixingEngineProxy
import com.bluehub.fastmixer.screens.mixing.MixingRepository
import com.bluehub.fastmixer.screens.mixing.MixingScreenViewModel
import com.bluehub.fastmixer.screens.recording.RecordingEngineProxy
import com.bluehub.fastmixer.screens.recording.RecordingRepository
import com.bluehub.fastmixer.screens.recording.RecordingScreenViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
interface PresentationModule {
    @Binds
    fun dialogManager(dialogManager: DialogManager): DialogManager

    @Binds
    fun permissionManager(permissionManager: PermissionManager): PermissionManager

    @Binds
    @ApplicationScope
    fun getMixingEngine(mixingEngineProxy: MixingEngineProxy): MixingEngineProxy

    @Binds
    @ApplicationScope
    fun getRecordingEngine(recordingEngineProxy: RecordingEngineProxy): RecordingEngineProxy

    @Binds
    fun getRecordingRepository(recordingRepository: RecordingRepository): RecordingRepository

    @Binds
    fun getMixingRepository(mixingRepository: MixingRepository): MixingRepository

    @Binds
    fun getAudioRepository(audioRepository: AudioRepository): AudioRepository

    @Binds
    fun audioDeviceChangeListener(audioDeviceChangeListener: AudioDeviceChangeListener): AudioDeviceChangeListener

    @Binds
    @IntoMap
    @FragmentScope
    @ViewModelKey(MixingScreenViewModel::class)
    fun mixingScreenViewModel(viewModel: MixingScreenViewModel): ViewModel

    @Binds
    @IntoMap
    @FragmentScope
    @ViewModelKey(RecordingScreenViewModel::class)
    fun recordingScreenViewModel(viewModel: RecordingScreenViewModel): ViewModel
}