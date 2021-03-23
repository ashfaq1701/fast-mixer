package com.bluehub.mixi.common.di.screens

import androidx.fragment.app.Fragment
import com.bluehub.mixi.screens.recording.RecordingScreen
import com.tbruyelle.rxpermissions3.RxPermissions
import dagger.*
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent

@Module
@InstallIn(FragmentComponent::class)
object RecordingScreenModule {

    @Provides
    fun bindFragment(fragment: Fragment): RecordingScreen {
        return fragment as RecordingScreen
    }

    @Provides
    fun rxPermission(fragment: RecordingScreen): RxPermissions = RxPermissions(fragment)
}
