package com.example.bluetoothframework.domain.scan.scanner

import com.example.bluetoothframework.domain.scan.BluetoothScanCallback
import com.example.bluetoothframework.domain.scan.BluetoothScannerConfig

interface BluetoothScannerInterface {
    fun startDiscovery(scannerConfig: BluetoothScannerConfig)
    fun stopDiscovery()
    fun setScanCallback(listener: BluetoothScanCallback)
}