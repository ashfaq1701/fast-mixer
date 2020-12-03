package com.bluehub.fastmixer.broadcastReceivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import com.bluehub.fastmixer.common.repositories.AudioRepository
import javax.inject.Inject


class AudioDeviceChangeListener @Inject constructor (val audioRepository: AudioRepository) : BroadcastReceiver() {
    private var isHeadphoneConnected = audioRepository.isHeadphoneConnected()
    private var isBluetoothHeadsetConnected = audioRepository.isBluetoothHeadsetConnected()
    private lateinit var mHeadphoneConnectedCallback: () -> Unit
    private lateinit var mHandleInputCallback: () -> Unit
    private lateinit var mHandleOutputCallback: () -> Unit

    public fun setHandleInputCallback(restartInputCallback: () -> Unit) {
        mHandleInputCallback = restartInputCallback
    }

    public fun setHandleOutputCallback(restartOutputCallback: () -> Unit) {
        mHandleOutputCallback = restartOutputCallback
    }

    public fun setHeadphoneConnectedCallback(headphoneConnectedCallback: () -> Unit) {
        mHeadphoneConnectedCallback = headphoneConnectedCallback
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        when(intent?.action) {
            AudioManager.ACTION_HEADSET_PLUG -> {
                if (intent.hasExtra("state")) {
                    val isConnected = intent.getIntExtra("state", 0) == 1
                    if (isConnected != isHeadphoneConnected) {
                        mHeadphoneConnectedCallback()
                        mHandleOutputCallback()
                        mHandleInputCallback()
                        isHeadphoneConnected = isConnected
                    }
                }
            }
            AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED -> {
                if (intent.hasExtra(AudioManager.EXTRA_SCO_AUDIO_STATE)) {
                    val state = intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, AudioManager.SCO_AUDIO_STATE_DISCONNECTED)
                    if (state == AudioManager.SCO_AUDIO_STATE_CONNECTING) {
                        return
                    }
                    val isConnected =  state == AudioManager.SCO_AUDIO_STATE_DISCONNECTED
                    if (isConnected != isBluetoothHeadsetConnected) {
                        mHeadphoneConnectedCallback()
                        mHandleOutputCallback()
                        mHandleInputCallback()
                        isBluetoothHeadsetConnected = isConnected
                    }
                }
            }
        }
    }
}