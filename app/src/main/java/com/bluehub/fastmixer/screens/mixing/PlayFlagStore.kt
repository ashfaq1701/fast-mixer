package com.bluehub.fastmixer.screens.mixing

import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.scopes.ActivityRetainedScoped
import javax.inject.Inject

@ActivityRetainedScoped
class PlayFlagStore @Inject constructor() {

    val isPlaying: MutableLiveData<Boolean> = MutableLiveData()
    val isGroupPlaying: MutableLiveData<Boolean> = MutableLiveData()
}
