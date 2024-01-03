package com.example.bluetoothframework.scanning.utils.advertising_timeout

interface DeviceDiscoverTimeoutInterface {
    fun checkDeviceDiscoverTimeout(advertisingUpdateMillis: Long, removeOldDevices: () -> Unit)
    fun stopDeviceDiscoverTimeoutCheck()
}