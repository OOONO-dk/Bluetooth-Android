package com.example.bluetoothframework.domain.connect

import android.bluetooth.BluetoothDevice

interface BluetoothCharacteristicChangeCallback {
    fun onCharacteristicChanged(byteArray: ByteArray, device: BluetoothDevice, characteristicUuid: String)
}