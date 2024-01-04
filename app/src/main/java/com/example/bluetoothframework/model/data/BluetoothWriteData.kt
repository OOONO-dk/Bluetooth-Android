package com.example.bluetoothframework.model.data

import android.bluetooth.BluetoothGattCharacteristic

data class BluetoothWriteData(
    val device: BluetoothDeviceInfo,
    val characteristic: BluetoothGattCharacteristic,
    val payLoad: ByteArray
)