package com.example.bluetoothframework.controller

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import com.example.bluetoothframework.connection.delegates.BluetoothCharacteristicChangeDelegate
import com.example.bluetoothframework.connection.delegates.BluetoothConnectDelegate
import com.example.bluetoothframework.connection.connector.BluetoothConnectorInterface
import com.example.bluetoothframework.scanning.delegates.BluetoothScanDelegate
import com.example.bluetoothframework.model.BluetoothScannerConfig
import com.example.bluetoothframework.scanning.scanner.BluetoothScannerInterface
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

    override fun setScanDelegate(listener: BluetoothScanDelegate) {
        bluetoothScanner.setScanDelegate(listener)
    }

    override fun setConnectDelegate(listener: BluetoothConnectDelegate) {
        bluetoothConnector.setConnectionDelegate(listener)
    }

    override fun setCharacteristicChangeDelegate(listener: BluetoothCharacteristicChangeDelegate) {
        bluetoothConnector.setCharacteristicChangeDelegate(listener)
    }

    override fun connectDevice(device: BluetoothDevice) {
        bluetoothConnector.connectDevice(device)
    }

    override fun disconnectDevice(device: BluetoothDevice) {
        bluetoothConnector.disconnectDevice(device)
    }

    override fun writeToDevice(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        payload: ByteArray
    ) {
        bluetoothConnector.writeToDevice(gatt, characteristic, payload)
    }
}