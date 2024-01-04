package com.example.bluetoothframework.model.data

data class BluetoothWriteData(
    val device: BluetoothDeviceInfo,
    val serviceUuid: String,
    val characteristicUuid: String,
    val payload: ByteArray
)