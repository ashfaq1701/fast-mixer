package com.bluehub.fastmixer.broadcastReceivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager


class AudioDeviceChangeListener : BroadcastReceiver() {
    private lateinit var mRestartInputCallback: () -> Unit
    private lateinit var mRestartOutputCallback: () -> Unit

    public fun setRestartInputCallback(restartInputCallback: () -> Unit) {
        mRestartInputCallback = restartInputCallback
    }

    public fun setRestartOutputCallback(restartOutputCallback: () -> Unit) {
        mRestartOutputCallback = restartOutputCallback
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        when(intent?.action) {
            AudioManager.ACTION_HEADSET_PLUG -> {
                mRestartOutputCallback()
                mRestartInputCallback()
            }
            AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED -> {
                mRestartOutputCallback()
                mRestartInputCallback()
            }
        }
    }
}