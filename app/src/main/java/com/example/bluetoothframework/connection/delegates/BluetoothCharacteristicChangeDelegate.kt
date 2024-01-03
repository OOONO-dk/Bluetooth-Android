package com.example.bluetoothframework.connection.delegates

import android.bluetooth.BluetoothDevice

interface BluetoothCharacteristicChangeDelegate {
    fun onCharacteristicChanged(byteArray: ByteArray, device: BluetoothDevice, characteristicUuid: String)
}