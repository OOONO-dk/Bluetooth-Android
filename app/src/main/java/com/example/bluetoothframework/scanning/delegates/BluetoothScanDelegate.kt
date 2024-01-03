package com.example.bluetoothframework.scanning.delegates

import android.bluetooth.BluetoothDevice

interface BluetoothScanDelegate {
    fun onDeviceDiscovered(newDevice: BluetoothDevice)
    fun onDeviceRemoved(removedDeviceAddress: String)
    fun onScanFailed()
}