package com.example.bluetoothframework.domain.implementation_examples

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import com.example.bluetoothframework.domain.connect.BluetoothCharacteristicChangeCallback
import com.example.bluetoothframework.domain.connect.BluetoothConnectCallback
import com.example.bluetoothframework.domain.scan.BluetoothScanCallback
import kotlinx.coroutines.flow.StateFlow

interface ScannerExampleInterface:
    BluetoothScanCallback,
    BluetoothConnectCallback,
    BluetoothCharacteristicChangeCallback {
    val scannedDevices: StateFlow<List<BluetoothDevice>>
    val connectedDevices: StateFlow<List<BluetoothGatt>>
    fun startDiscovery()
    fun stopDiscovery()
    fun connectDevice(device: BluetoothDevice)
    fun disconnectDevice(device: BluetoothDevice)
    fun writeToDevice(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, payload: ByteArray)
}