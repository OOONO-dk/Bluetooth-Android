package com.example.bluetoothframework.domain.scanner

interface BluetoothScannerInterface {
    fun startDiscovery(scannerConfig: BluetoothScannerConfig)
    fun stopDiscovery()
}