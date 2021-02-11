package com.bluehub.fastmixer.common.di.modules.screens

import androidx.lifecycle.ViewModel
import com.bluehub.fastmixer.common.di.FragmentScope
import com.bluehub.fastmixer.common.di.ViewModelKey
import com.bluehub.fastmixer.screens.recording.RecordingScreen
import com.bluehub.fastmixer.screens.recording.RecordingScreenViewModel
import com.tbruyelle.rxpermissions3.RxPermissions
import dagger.*
import dagger.multibindings.IntoMap

@Module
interface RecordingScreenModule {
    @Binds
    @IntoMap
    @FragmentScope
    @ViewModelKey(RecordingScreenViewModel::class)
    fun recordingScreenViewModel(viewModel: RecordingScreenViewModel): ViewModel

    @Module
    companion object {

        @JvmStatic
        @Provides
        fun rxPermission(fragment: RecordingScreen): RxPermissions = RxPermissions(fragment)
    }
}
