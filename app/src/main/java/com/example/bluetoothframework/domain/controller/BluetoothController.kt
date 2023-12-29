package com.example.bluetoothframework.domain.controller

import android.bluetooth.BluetoothDevice
import com.example.bluetoothframework.domain.connect.BluetoothCharacteristicChangeCallback
import com.example.bluetoothframework.domain.connect.BluetoothConnectCallback
import com.example.bluetoothframework.domain.connect.connector.BluetoothConnectorInterface
import com.example.bluetoothframework.domain.scan.BluetoothScanCallback
import com.example.bluetoothframework.domain.scan.BluetoothScannerConfig
import com.example.bluetoothframework.domain.scan.scanner.BluetoothScannerInterface
import javax.inject.Inject

class BluetoothController @Inject constructor(
    private val bluetoothScanner: BluetoothScannerInterface,
    private val bluetoothConnector: BluetoothConnectorInterface
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

    override fun setConnectCallback(listener: BluetoothConnectCallback) {
        bluetoothConnector.setConnectionCallback(listener)
    }

    override fun setCharacteristicChangeCallback(listener: BluetoothCharacteristicChangeCallback) {
        bluetoothConnector.setCharacteristicChangeCallback(listener)
    }

    override fun connectDevice(device: BluetoothDevice) {
        bluetoothConnector.connectDevice(device)
    }
}