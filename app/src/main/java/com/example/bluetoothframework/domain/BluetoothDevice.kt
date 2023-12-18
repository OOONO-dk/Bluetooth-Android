package com.example.bluetoothframework.domain

typealias BluetoothDeviceDomain = BluetoothDevice

data class BluetoothDevice(
    val name: String?,
    val address: String,
    val discoverTimestamp: Long,
    val rssi: Int = 0
)
