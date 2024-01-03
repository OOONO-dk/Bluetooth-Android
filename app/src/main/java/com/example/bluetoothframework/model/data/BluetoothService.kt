package com.example.bluetoothframework.model.data

data class BluetoothService(
    val serviceUUID: String,
    val characteristics: List<String> = emptyList()
)
