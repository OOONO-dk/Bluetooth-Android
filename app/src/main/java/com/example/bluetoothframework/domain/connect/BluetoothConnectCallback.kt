package com.example.bluetoothframework.domain.connect

import android.bluetooth.BluetoothGatt

interface BluetoothConnectCallback {
    fun onDeviceConnected(gatt: BluetoothGatt)
    fun onDeviceDisconnected(gatt: BluetoothGatt)
    fun onConnectionFail(gatt: BluetoothGatt)
}