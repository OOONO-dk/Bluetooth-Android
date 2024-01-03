package com.example.bluetoothframework.scanning.scanner

import com.example.bluetoothframework.scanning.delegates.BluetoothScanDelegate
import com.example.bluetoothframework.model.BluetoothScannerConfig

interface BluetoothScannerInterface {
    fun startDiscovery(scannerConfig: BluetoothScannerConfig)
    fun stopDiscovery()
    fun setScanDelegate(listener: BluetoothScanDelegate)
}