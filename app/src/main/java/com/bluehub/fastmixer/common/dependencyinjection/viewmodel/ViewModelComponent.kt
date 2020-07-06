package com.bluehub.fastmixer.common.dependencyinjection.viewmodel

import com.bluehub.fastmixer.common.permissions.PermissionViewModel
import com.bluehub.fastmixer.screens.mixing.MixingScreenViewModel
import com.bluehub.fastmixer.screens.recording.RecordingScreenViewModel
import dagger.Component

@Component(modules = [ViewModelModule::class])
interface ViewModelComponent {
    fun inject(recordingScreenViewModel: RecordingScreenViewModel)

    fun inject(mixingScreenViewModel: MixingScreenViewModel)
}