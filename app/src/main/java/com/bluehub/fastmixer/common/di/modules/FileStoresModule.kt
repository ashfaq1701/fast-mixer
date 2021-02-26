package com.bluehub.fastmixer.common.di.modules

import com.bluehub.fastmixer.screens.mixing.*
import dagger.Module
import dagger.Provides

@Module
class FileStoresModule {

    @Provides
    fun audioFileStore(): AudioFileStore = AudioFileStore()

    @Provides
    fun playFlagStore(): PlayFlagStore = PlayFlagStore()

    @Provides
    fun fileWaveViewStore(mixingRepository: MixingRepository): FileWaveViewStore = FileWaveViewStore(mixingRepository)
}
