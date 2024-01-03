package com.example.bluetoothframework.control.advertising_timeout

interface DeviceAdvertisementTimeoutInterface {
    fun checkDeviceDiscoverTimeout(advertisingUpdateMillis: Long, removeOldDevices: () -> Unit)
    fun stopDeviceDiscoverTimeoutCheck()
}