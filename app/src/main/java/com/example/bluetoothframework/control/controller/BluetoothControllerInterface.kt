package com.example.bluetoothframework.control.controller

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import com.example.bluetoothframework.connection.delegates.BluetoothConnectForwarderDelegate
import com.example.bluetoothframework.control.delegates.BluetoothDeviceDelegate
import com.example.bluetoothframework.scanning.delegates.BluetoothScanForwarderDelegate
import com.example.bluetoothframework.model.data.BluetoothScannerConfig

interface BluetoothControllerInterface:
    BluetoothScanForwarderDelegate,
    BluetoothConnectForwarderDelegate {
    fun startDiscovery(scannerConfig: BluetoothScannerConfig)
    fun stopDiscovery()
    fun setBluetoothDeviceListener(listener: BluetoothDeviceDelegate)
    fun connectDevice(device: BluetoothDevice)
    fun disconnectDevice(device: BluetoothDevice)
    fun writeToDevice(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, payload: ByteArray)
}