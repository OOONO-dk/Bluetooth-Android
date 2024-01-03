package com.example.bluetoothframework.connection.delegates

import android.bluetooth.BluetoothGatt

interface BluetoothConnectForwarderDelegate {
    fun onDeviceConnected(gatt: BluetoothGatt)
    fun onDeviceDisconnected(gatt: BluetoothGatt)
    fun onConnectionFail(gatt: BluetoothGatt)
    fun onCharacteristicChanged(byteArray: ByteArray, gatt: BluetoothGatt, characteristicUuid: String)
}