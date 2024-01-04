package com.example.bluetoothframework.model.data

data class BluetoothService(
    val serviceUuid: String,
    val characteristics: List<String> = emptyList()
)
