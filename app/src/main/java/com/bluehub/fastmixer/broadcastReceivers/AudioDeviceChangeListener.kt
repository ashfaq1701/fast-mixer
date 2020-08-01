package com.bluehub.fastmixer.broadcastReceivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager


class AudioDeviceChangeListener : BroadcastReceiver() {
    private lateinit var mHandleInputCallback: () -> Unit
    private lateinit var mHandleOutputCallback: () -> Unit

    public fun setHandleInputCallback(restartInputCallback: () -> Unit) {
        mHandleInputCallback = restartInputCallback
    }

    public fun setHandleOutputCallback(restartOutputCallback: () -> Unit) {
        mHandleOutputCallback = restartOutputCallback
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        when(intent?.action) {
            AudioManager.ACTION_HEADSET_PLUG -> {
                mHandleOutputCallback()
                mHandleInputCallback()
            }
            AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED -> {
                mHandleOutputCallback()
                mHandleInputCallback()
            }
        }
    }
}