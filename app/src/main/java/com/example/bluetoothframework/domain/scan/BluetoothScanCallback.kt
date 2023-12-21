package com.example.bluetoothframework.domain.scan

import android.bluetooth.BluetoothDevice
import com.example.bluetoothframework.domain.BluetoothDeviceDomain

interface BluetoothScanCallback {
    fun onDeviceDiscovered(newDevice: BluetoothDevice)
    fun onDeviceRemoved(removedDeviceAddress: String)
    fun onScanFailed()
}