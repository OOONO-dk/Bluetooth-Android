package com.example.bluetoothframework.model.data

data class BluetoothConnectData(
    val device: BluetoothDeviceInfo,
    val services: List<BluetoothService> = emptyList()
)
