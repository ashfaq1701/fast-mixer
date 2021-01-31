package com.bluehub.fastmixer.screens.mixing

import androidx.lifecycle.MutableLiveData

class PlayFlagStore {

    val isPlaying: MutableLiveData<Boolean> = MutableLiveData()
    val isGroupPlaying: MutableLiveData<Boolean> = MutableLiveData()
}
