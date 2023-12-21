package com.example.bluetoothframework.domain.connect

import android.bluetooth.BluetoothDevice

interface BluetoothConnectCallback {
    fun onDeviceConnected(device: BluetoothDevice)
    fun onDeviceDisconnected(device: BluetoothDevice)
    fun onConnectionFail(device: BluetoothDevice)
}