package com.bluehub.mixi.screens.mixing

import androidx.lifecycle.MutableLiveData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayFlagStore @Inject constructor() {

    val isPlaying: MutableLiveData<Boolean> = MutableLiveData()
    val isGroupPlaying: MutableLiveData<Boolean> = MutableLiveData()
}
