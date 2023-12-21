package com.example.bluetoothframework.domain.connect.connector

data class BluetoothService(
    val serviceUUID: String,
    val characteristics: List<BluetoothCharacteristic>,
)

data class BluetoothCharacteristic(
    val characteristicUUID: String,
    val characteristicTypes: List<CharacteristicType>
)

enum class CharacteristicType {
    READABLE,
    WRITABLE,
    SUBSCRIBABLE,
    NOTIFIABLE,
}