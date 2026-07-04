package com.yuandao.music.playback

import android.content.Context
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class OutputDeviceManager internal constructor(
    private val adapter: OutputDeviceAdapter,
) {
    private val changeCallback = OutputDeviceChangeCallback { refresh() }
    private val _devices = MutableStateFlow(adapter.readDevices())
    val devices: StateFlow<List<OutputDevice>> = _devices

    constructor(context: Context) : this(
        AndroidOutputDeviceAdapter(context.getSystemService(AudioManager::class.java))
    )

    init {
        adapter.registerCallback(changeCallback)
    }

    fun refresh() {
        _devices.value = adapter.readDevices()
    }

    fun release() {
        adapter.unregisterCallback(changeCallback)
    }
}

internal fun interface OutputDeviceChangeCallback {
    fun onChanged()
}

internal interface OutputDeviceAdapter {
    fun readDevices(): List<OutputDevice>
    fun registerCallback(callback: OutputDeviceChangeCallback)
    fun unregisterCallback(callback: OutputDeviceChangeCallback)
}

private class AndroidOutputDeviceAdapter(
    private val audioManager: AudioManager,
) : OutputDeviceAdapter {
    private val handler = Handler(Looper.getMainLooper())
    private val callbacks = mutableMapOf<OutputDeviceChangeCallback, AudioDeviceCallback>()

    override fun readDevices(): List<OutputDevice> =
        audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS).map { info ->
            OutputDevice(
                id = info.id.toString(),
                name = info.productName?.toString().orEmpty().ifBlank { info.typeName() },
                type = info.typeName(),
                isUsb = info.type == AudioDeviceInfo.TYPE_USB_DEVICE ||
                    info.type == AudioDeviceInfo.TYPE_USB_HEADSET,
            )
        }

    override fun registerCallback(callback: OutputDeviceChangeCallback) {
        if (callbacks.containsKey(callback)) return
        val audioCallback = object : AudioDeviceCallback() {
            override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>) {
                callback.onChanged()
            }

            override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>) {
                callback.onChanged()
            }
        }
        callbacks[callback] = audioCallback
        audioManager.registerAudioDeviceCallback(audioCallback, handler)
    }

    override fun unregisterCallback(callback: OutputDeviceChangeCallback) {
        val audioCallback = callbacks.remove(callback) ?: return
        audioManager.unregisterAudioDeviceCallback(audioCallback)
    }
}

data class OutputDevice(
    val id: String,
    val name: String,
    val type: String,
    val isUsb: Boolean,
)

private fun AudioDeviceInfo.typeName(): String =
    when (type) {
        AudioDeviceInfo.TYPE_BUILTIN_SPEAKER -> "Built-in Speaker"
        AudioDeviceInfo.TYPE_WIRED_HEADPHONES -> "Wired Headphones"
        AudioDeviceInfo.TYPE_WIRED_HEADSET -> "Wired Headset"
        AudioDeviceInfo.TYPE_BLUETOOTH_A2DP -> "Bluetooth"
        AudioDeviceInfo.TYPE_USB_DEVICE -> "USB DAC"
        AudioDeviceInfo.TYPE_USB_HEADSET -> "USB Headset"
        AudioDeviceInfo.TYPE_HDMI -> "HDMI"
        else -> "System Output"
    }
