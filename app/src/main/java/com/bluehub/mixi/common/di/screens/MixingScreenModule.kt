package com.bluehub.mixi.common.di.screens

import androidx.fragment.app.Fragment
import com.bluehub.mixi.screens.mixing.MixingScreen
import com.tbruyelle.rxpermissions3.RxPermissions
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import javax.inject.Qualifier

@Qualifier
annotation class MixingScreenRxPermission

@Module
@InstallIn(FragmentComponent::class)
object MixingScreenModule {

    @Provides
    fun bindFragment(fragment: Fragment): MixingScreen {
        return fragment as MixingScreen
    }

    @MixingScreenRxPermission
    @Provides
    fun rxPermission(fragment: MixingScreen): RxPermissions = RxPermissions(fragment)
}
