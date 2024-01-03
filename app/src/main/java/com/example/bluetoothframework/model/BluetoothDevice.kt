package com.example.bluetoothframework.model

typealias DiscoveredBluetoothDevice = BluetoothDevice
data class BluetoothDevice(
    val name: String?,
    val address: String,
    val discoverTimestamp: Long,
    val rssi: Int = 0
)