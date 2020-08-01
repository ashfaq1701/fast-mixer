package com.bluehub.fastmixer.common.repositories

import android.media.AudioDeviceInfo
import android.media.AudioManager

class AudioRepository {
    var audioManager: AudioManager? = null

    fun isHeadphoneConnected(): Boolean {
        audioManager?.let { manager ->
            val devices = manager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            val bluetoothTypes = arrayOf(AudioDeviceInfo.TYPE_WIRED_HEADSET,
                AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
                AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
                AudioDeviceInfo.TYPE_BLUETOOTH_SCO
            )
            devices.forEach {
                when(it.type) {
                    in bluetoothTypes -> return@isHeadphoneConnected true
                }
            }
        }
        return false
    }
}