package com.example.bluetoothframework.domain.controller

import com.example.bluetoothframework.domain.scan.BluetoothScanCallback
import com.example.bluetoothframework.domain.scan.BluetoothScannerConfig

interface BluetoothControllerInterface {
    fun startDiscovery(scannerConfig: BluetoothScannerConfig)
    fun stopDiscovery()
    fun setScanCallback(listener: BluetoothScanCallback)
}