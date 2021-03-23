package com.bluehub.mixi.common.repositories

import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import javax.inject.Inject

class AudioRepository @Inject constructor() {
    var audioManager: AudioManager? = null

    fun isHeadphoneConnected(): Boolean {
        val usbHeadsetType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            arrayOf(AudioDeviceInfo.TYPE_USB_HEADSET)
        } else {
            emptyArray()
        }
        audioManager?.let { manager ->
            val devices = manager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            val deviceTypes = arrayOf(AudioDeviceInfo.TYPE_WIRED_HEADSET,
                AudioDeviceInfo.TYPE_WIRED_HEADPHONES
            ).plus(usbHeadsetType)
            devices.forEach {
                when(it.type) {
                    in deviceTypes -> return@isHeadphoneConnected true
                }
            }
        }
        return false
    }

    fun isBluetoothHeadsetConnected(): Boolean {
        audioManager?.let { manager ->
            val devices = manager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            val deviceTypes = arrayOf(AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
                AudioDeviceInfo.TYPE_BLUETOOTH_SCO)
            devices.forEach {
                when(it.type) {
                    in deviceTypes -> return@isBluetoothHeadsetConnected true
                }
            }
        }
        return false
    }
}
