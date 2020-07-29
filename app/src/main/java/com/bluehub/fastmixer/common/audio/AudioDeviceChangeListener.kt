package com.bluehub.fastmixer.common.audio

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager

class AudioDeviceChangeListener: BroadcastReceiver() {
    private lateinit var mRestartInputCallback: () -> Unit
    private lateinit var mRestartOutputCallback: () -> Unit

    public fun setRestartInputCallback(restartInputCallback: () -> Unit) {
        mRestartInputCallback = restartInputCallback
    }

    public fun setRestartOutputCallback(restartOutputCallback: () -> Unit) {
        mRestartOutputCallback = restartOutputCallback
    }

    override fun onReceive(p0: Context?, p1: Intent?) {
        when(p1?.action) {
            AudioManager.ACTION_HEADSET_PLUG -> {
                val hasMicrophone = p1.getIntExtra("action", 0)
                if (hasMicrophone == 1) {
                    mRestartInputCallback()
                }
            }
            AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED -> {
                val connectionState = p1.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, AudioManager.SCO_AUDIO_STATE_CONNECTING)
                if (connectionState == AudioManager.SCO_AUDIO_STATE_CONNECTED ||
                    connectionState == AudioManager.SCO_AUDIO_STATE_DISCONNECTED) {
                    mRestartInputCallback()
                    mRestartOutputCallback()
                }
            }
            AudioManager.ACTION_AUDIO_BECOMING_NOISY -> {
                mRestartOutputCallback()
            }
        }
    }
}