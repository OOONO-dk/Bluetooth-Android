package com.example.bluetoothframework.implementation_examples.service

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import com.example.bluetoothframework.control.delegates.BluetoothDeviceDelegate
import kotlinx.coroutines.flow.StateFlow

interface ImplementationExampleInterface:
    BluetoothDeviceDelegate {
    val scannedDevices: StateFlow<List<BluetoothDevice>>
    val connectedDevices: StateFlow<List<BluetoothGatt>>
    fun startDiscovery()
    fun stopDiscovery()
    fun connectDevice(device: BluetoothDevice)
    fun disconnectDevice(device: BluetoothDevice)
    fun writeToDevice(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, payload: ByteArray)
}