package com.example.bluetoothframework.domain.implementation_examples

import com.example.bluetoothframework.domain.BluetoothDeviceDomain
import com.example.bluetoothframework.domain.scan.BluetoothScanCallback
import kotlinx.coroutines.flow.StateFlow

interface ScannerExampleInterface: BluetoothScanCallback {
    val scannedDevices: StateFlow<List<BluetoothDeviceDomain>>
    fun startDiscovery()
    fun stopDiscovery()
}