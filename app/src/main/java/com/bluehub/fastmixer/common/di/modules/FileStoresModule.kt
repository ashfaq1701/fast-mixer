package com.bluehub.fastmixer.common.di.modules

import com.bluehub.fastmixer.common.di.ApplicationScope
import com.bluehub.fastmixer.screens.mixing.*
import dagger.Module
import dagger.Provides

@Module
class FileStoresModule {

    @ApplicationScope
    @Provides
    fun audioFileStore(): AudioFileStore = AudioFileStore()

    @ApplicationScope
    @Provides
    fun playFlagStore(): PlayFlagStore = PlayFlagStore()

    @ApplicationScope
    @Provides
    fun fileWaveViewStore(mixingRepository: MixingRepository): FileWaveViewStore = FileWaveViewStore(mixingRepository)
}
