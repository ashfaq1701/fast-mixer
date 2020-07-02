package com.bluehub.fastmixer.common.dependencyinjection.presentation

import com.bluehub.fastmixer.common.viewmodel.ViewModelFactory
import com.bluehub.fastmixer.fragments.recording.RecordingViewModel
import com.bluehub.fastmixer.screens.mixing.MixingViewModel
import dagger.Module
import dagger.Provides

@Module
class PresentationModule {
    @Provides
    fun viewModelFactory(mixingViewModel: MixingViewModel, recordingViewModel: RecordingViewModel): ViewModelFactory =
        ViewModelFactory(mixingViewModel, recordingViewModel)

    @Provides
    fun mixingViewModel(): MixingViewModel = MixingViewModel()

    @Provides
    fun recordingViewModel(): RecordingViewModel = RecordingViewModel()
}