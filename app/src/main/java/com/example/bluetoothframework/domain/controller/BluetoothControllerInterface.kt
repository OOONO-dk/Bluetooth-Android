package com.example.bluetoothframework.domain.controller

import com.example.bluetoothframework.domain.scanner.BluetoothScannerConfig

interface BluetoothControllerInterface {
    fun startDiscovery(scannerConfig: BluetoothScannerConfig)
    fun stopDiscovery()
}