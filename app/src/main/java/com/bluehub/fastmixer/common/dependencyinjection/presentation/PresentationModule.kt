package com.bluehub.fastmixer.common.dependencyinjection.presentation

import com.bluehub.fastmixer.common.viewmodel.ViewModelFactory
import com.bluehub.fastmixer.fragments.recording.VisualizerViewModel
import com.bluehub.fastmixer.screens.mixing.MixingScreenViewModel
import com.bluehub.fastmixer.screens.recording.RecordingScreenViewModel
import dagger.Module
import dagger.Provides

@Module
class PresentationModule {
    @Provides
    fun viewModelFactory(mixingScreenViewModel: MixingScreenViewModel, recordingScreenViewModel: RecordingScreenViewModel, visualizerViewModel: VisualizerViewModel): ViewModelFactory =
        ViewModelFactory(mixingScreenViewModel, recordingScreenViewModel, visualizerViewModel)

    @Provides
    fun mixingScreenViewModel(): MixingScreenViewModel = MixingScreenViewModel()

    @Provides
    fun recordingScreenViewModel(): RecordingScreenViewModel = RecordingScreenViewModel()

    @Provides
    fun visualizerViewModel(): VisualizerViewModel = VisualizerViewModel()
}