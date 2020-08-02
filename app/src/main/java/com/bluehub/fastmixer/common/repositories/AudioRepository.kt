package com.bluehub.fastmixer.common.repositories

import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import androidx.annotation.RequiresApi

class AudioRepository {
    var audioManager: AudioManager? = null

    fun isHeadphoneConnected(): Boolean {
        val usbHeadsetType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            arrayOf(AudioDeviceInfo.TYPE_USB_HEADSET)
        } else {
            emptyArray()
        }
        audioManager?.let { manager ->
            val devices = manager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            val bluetoothTypes = arrayOf(AudioDeviceInfo.TYPE_WIRED_HEADSET,
                AudioDeviceInfo.TYPE_WIRED_HEADPHONES
            ).plus(usbHeadsetType)
            devices.forEach {
                when(it.type) {
                    in bluetoothTypes -> return@isHeadphoneConnected true
                }
            }
        }
        return false
    }
}