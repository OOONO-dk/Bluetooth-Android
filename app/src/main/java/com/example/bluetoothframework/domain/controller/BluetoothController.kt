package com.example.bluetoothframework.domain.controller

import com.example.bluetoothframework.domain.scan.BluetoothScanCallback
import com.example.bluetoothframework.domain.scan.BluetoothScannerConfig
import com.example.bluetoothframework.domain.scan.scanner.BluetoothScannerInterface
import javax.inject.Inject

class BluetoothController @Inject constructor(
    private val bluetoothScanner: BluetoothScannerInterface
) : BluetoothControllerInterface {

    override fun startDiscovery(scannerConfig: BluetoothScannerConfig) {
        bluetoothScanner.startDiscovery(scannerConfig)
    }

    override fun stopDiscovery() {
        bluetoothScanner.stopDiscovery()
    }

    override fun setScanCallback(listener: BluetoothScanCallback) {
        bluetoothScanner.setScanCallback(listener)
    }
}