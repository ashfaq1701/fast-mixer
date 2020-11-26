package com.bluehub.fastmixer.common.dependencyinjection.presentation

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.bluehub.fastmixer.broadcastReceivers.AudioDeviceChangeListener
import com.bluehub.fastmixer.common.repositories.AudioRepository
import com.bluehub.fastmixer.common.utils.DialogManager
import com.bluehub.fastmixer.common.utils.PermissionManager
import com.bluehub.fastmixer.screens.mixing.*
import com.bluehub.fastmixer.screens.recording.*
import dagger.Module
import dagger.Provides

@Module
class PresentationModule(val context: Context, private val fragment: Fragment, private val viewModelStoreOwner: ViewModelStoreOwner) {

    @Provides
    fun dialogManager(): DialogManager = DialogManager.create()

    @Provides
    fun permissionManager(): PermissionManager = PermissionManager.create()

    @Provides
    fun getMixingEngine(): MixingEngineProxy = MixingEngineProxy.getInstance()

    @Provides
    fun getRecordingEngine(): RecordingEngineProxy = RecordingEngineProxy.getInstance()

    @Provides
    fun getRecordingRepository(recordingEngineProxy: RecordingEngineProxy): RecordingRepository = RecordingRepository(recordingEngineProxy)

    @Provides
    fun getMixingRepository(mixingEngineProxy: MixingEngineProxy): MixingRepository = MixingRepository(mixingEngineProxy)

    @Provides
    fun getAudioRepository(): AudioRepository = AudioRepository()

    @Provides
    fun audioDeviceChangeListener(audioRepository: AudioRepository): AudioDeviceChangeListener = AudioDeviceChangeListener(audioRepository)

    @Provides
    fun recordingScreenViewModelFactory(permissionManager: PermissionManager, repository: RecordingRepository, audioRepository: AudioRepository, audioDeviceChangeListener: AudioDeviceChangeListener): RecordingScreenViewModelFactory =
        RecordingScreenViewModelFactory(context, permissionManager, repository, audioRepository, audioDeviceChangeListener)

    @Provides
    fun mixingScreenViewModelFactory(permissionManager: PermissionManager, repository: MixingRepository) = MixingScreenViewModelFactory(context, permissionManager, repository)

    @Provides
    fun recordingScreenViewModel(recordingScreenViewModelFactory: RecordingScreenViewModelFactory): RecordingScreenViewModel =
        ViewModelProvider(fragment, recordingScreenViewModelFactory).get(RecordingScreenViewModel::class.java)

    @Provides
    fun mixingScreenViewModel(mixingScreenViewModelFactory: MixingScreenViewModelFactory): MixingScreenViewModel =
        ViewModelProvider(viewModelStoreOwner, mixingScreenViewModelFactory).get(MixingScreenViewModel::class.java)
}