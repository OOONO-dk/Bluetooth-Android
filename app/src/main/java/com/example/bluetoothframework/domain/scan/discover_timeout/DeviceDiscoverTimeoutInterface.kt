package com.example.bluetoothframework.domain.scan.discover_timeout

interface DeviceDiscoverTimeoutInterface {
    fun checkDeviceDiscoverTimeout(advertisingUpdateMillis: Long, removeOldDevices: () -> Unit)
    fun stopDeviceDiscoverTimeoutCheck()
}