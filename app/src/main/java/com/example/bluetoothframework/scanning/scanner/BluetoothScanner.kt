package com.example.bluetoothframework.scanning.scanner

import com.example.bluetoothframework.scanning.delegates.BluetoothScanForwarderDelegate
import com.example.bluetoothframework.model.data.BluetoothScannerConfig

interface BluetoothScanner {
    fun startDiscovery(scannerConfig: BluetoothScannerConfig)
    fun stopDiscovery()
    fun setScanDelegate(listener: BluetoothScanForwarderDelegate)
}