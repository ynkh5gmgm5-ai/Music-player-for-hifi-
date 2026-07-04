package com.yuandao.music.playback

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

class OutputDeviceManagerTest {
    @Test
    fun refreshesDeviceListWhenAdapterReportsChange() {
        val speaker = OutputDevice("1", "Speaker", "Built-in Speaker", isUsb = false)
        val usb = OutputDevice("2", "USB DAC", "USB DAC", isUsb = true)
        val adapter = FakeOutputDeviceAdapter(listOf(speaker))
        val manager = OutputDeviceManager(adapter)

        assertEquals(listOf(speaker), manager.devices.value)

        adapter.devices = listOf(usb)
        adapter.emitChanged()

        assertEquals(listOf(usb), manager.devices.value)
    }

    @Test
    fun releaseUnregistersDeviceCallback() {
        val adapter = FakeOutputDeviceAdapter(emptyList())
        val manager = OutputDeviceManager(adapter)
        val registered = adapter.registeredCallback

        manager.release()

        assertSame(registered, adapter.unregisteredCallback)
        assertNull(adapter.registeredCallback)
    }

    private class FakeOutputDeviceAdapter(
        var devices: List<OutputDevice>,
    ) : OutputDeviceAdapter {
        var registeredCallback: OutputDeviceChangeCallback? = null
        var unregisteredCallback: OutputDeviceChangeCallback? = null

        override fun readDevices(): List<OutputDevice> = devices

        override fun registerCallback(callback: OutputDeviceChangeCallback) {
            registeredCallback = callback
        }

        override fun unregisterCallback(callback: OutputDeviceChangeCallback) {
            unregisteredCallback = callback
            if (registeredCallback == callback) {
                registeredCallback = null
            }
        }

        fun emitChanged() {
            registeredCallback?.onChanged()
        }
    }
}
