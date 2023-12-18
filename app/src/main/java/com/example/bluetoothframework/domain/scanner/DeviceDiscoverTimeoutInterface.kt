package com.example.bluetoothframework.domain.scanner

interface DeviceDiscoverTimeoutInterface {
    fun checkDeviceDiscoverTimeout(advertisingUpdateMillis: Long, removeOldDevices: () -> Unit)
    fun stopDeviceDiscoverTimeoutCheck()
}