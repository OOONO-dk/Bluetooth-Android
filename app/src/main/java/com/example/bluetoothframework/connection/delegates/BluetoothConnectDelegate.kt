package com.example.bluetoothframework.connection.delegates

import android.bluetooth.BluetoothGatt

interface BluetoothConnectDelegate {
    fun onDeviceConnected(gatt: BluetoothGatt)
    fun onDeviceDisconnected(gatt: BluetoothGatt)
    fun onConnectionFail(gatt: BluetoothGatt)
}