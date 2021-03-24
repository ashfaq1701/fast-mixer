package com.bluehub.mixi.screens.splash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bluehub.mixi.common.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.*
import javax.inject.Inject
import kotlin.concurrent.schedule

@HiltViewModel
class SplashViewModel @Inject constructor() : BaseViewModel() {

    private val _eventNavigateToMixingScreen = MutableLiveData(false)
    val eventNavigateToMixingScreen: LiveData<Boolean>
        get() = _eventNavigateToMixingScreen

    fun startSplashScreenTimer() {
        Timer().schedule(SPLASH_SCREEN_DURATION) {
            _eventNavigateToMixingScreen.postValue(true)
        }
    }

    fun resetNavigateToMixingScreen() {
        _eventNavigateToMixingScreen.value = false
    }
}

const val SPLASH_SCREEN_DURATION = 2000L
