package com.example.bluetoothframework.domain.scan

import com.example.bluetoothframework.domain.BluetoothDeviceDomain

interface BluetoothScanCallback {
    fun onDeviceDiscovered(newDevice: BluetoothDeviceDomain)
    fun onDeviceRemoved(removedDevice: BluetoothDeviceDomain)
    fun onScanFailed()
}