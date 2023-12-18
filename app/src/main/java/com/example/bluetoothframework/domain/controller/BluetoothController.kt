package com.example.bluetoothframework.domain.controller

import com.example.bluetoothframework.domain.scanner.BluetoothScannerConfig
import com.example.bluetoothframework.domain.scanner.BluetoothScannerInterface
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
}