package com.bluehub.fastmixer.common.di.modules.screens

import androidx.lifecycle.ViewModel
import com.bluehub.fastmixer.common.di.FragmentScope
import com.bluehub.fastmixer.common.di.ViewModelKey
import com.bluehub.fastmixer.screens.recording.RecordingScreenViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
interface RecordingScreenModule {
    @Binds
    @IntoMap
    @FragmentScope
    @ViewModelKey(RecordingScreenViewModel::class)
    fun recordingScreenViewModel(viewModel: RecordingScreenViewModel): ViewModel
}
