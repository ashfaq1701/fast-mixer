package com.bluehub.fastmixer.broadcastReceivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager


class AudioDeviceChangeListener : BroadcastReceiver() {
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
                mHeadphoneConnectedCallback()
                mHandleOutputCallback()
                mHandleInputCallback()
            }
            AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED -> {
                mHeadphoneConnectedCallback()
                mHandleOutputCallback()
                mHandleInputCallback()
            }
        }
    }
}