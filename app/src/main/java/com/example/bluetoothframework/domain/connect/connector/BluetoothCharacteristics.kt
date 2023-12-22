package com.example.bluetoothframework.domain.connect.connector

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService

data class BluetoothService(
    val service: BluetoothGattService,
    val serviceUUID: String,
    val characteristics: List<BluetoothCharacteristic>,
)

data class BluetoothCharacteristic(
    val characteristicUUID: String,
    val characteristic: BluetoothGattCharacteristic,
    val characteristicTypes: List<CharacteristicType>
)

enum class CharacteristicType {
    READABLE,
    WRITABLE,
    NOTIFIABLE,
    INDICATABLE,
    WRITABLE_WITHOUT_RESPONSE,
    EMPTY
}