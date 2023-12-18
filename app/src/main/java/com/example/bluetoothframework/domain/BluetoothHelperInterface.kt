package com.example.bluetoothframework.domain

import kotlinx.coroutines.flow.StateFlow

interface BluetoothHelperInterface {
    val scannedDevices: StateFlow<List<BluetoothDeviceDomain>>
}