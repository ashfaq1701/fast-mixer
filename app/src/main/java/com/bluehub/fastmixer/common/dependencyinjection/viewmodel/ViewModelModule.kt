package com.bluehub.fastmixer.common.dependencyinjection.viewmodel

import com.bluehub.fastmixer.common.utils.PermissionManager
import dagger.Module
import dagger.Provides

@Module
class ViewModelModule {
    @Provides
    fun permissionManager(): PermissionManager = PermissionManager.create()
}