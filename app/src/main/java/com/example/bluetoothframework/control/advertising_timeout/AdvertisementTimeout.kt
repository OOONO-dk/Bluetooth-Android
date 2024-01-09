package com.example.bluetoothframework.control.advertising_timeout

interface AdvertisementTimeout {
    fun checkDeviceDiscoverTimeout(advertisingUpdateMillis: Long, removeOldDevices: () -> Unit)
    fun stopDeviceDiscoverTimeoutCheck()
}