package com.example.bluetoothframework.implementation_examples.service

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import com.example.bluetoothframework.connection.delegates.BluetoothCharacteristicChangeDelegate
import com.example.bluetoothframework.connection.delegates.BluetoothConnectDelegate
import com.example.bluetoothframework.scanning.delegates.BluetoothScanDelegate
import kotlinx.coroutines.flow.StateFlow

interface ImplementationExampleInterface:
    BluetoothScanDelegate,
    BluetoothConnectDelegate,
    BluetoothCharacteristicChangeDelegate {
    val scannedDevices: StateFlow<List<BluetoothDevice>>
    val connectedDevices: StateFlow<List<BluetoothGatt>>
    fun startDiscovery()
    fun stopDiscovery()
    fun connectDevice(device: BluetoothDevice)
    fun disconnectDevice(device: BluetoothDevice)
    fun writeToDevice(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, payload: ByteArray)
}