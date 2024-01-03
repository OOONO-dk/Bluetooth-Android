package com.example.bluetoothframework.controller

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import com.example.bluetoothframework.connection.delegates.BluetoothCharacteristicChangeDelegate
import com.example.bluetoothframework.connection.delegates.BluetoothConnectDelegate
import com.example.bluetoothframework.scanning.delegates.BluetoothScanDelegate
import com.example.bluetoothframework.model.BluetoothScannerConfig

interface BluetoothControllerInterface {
    fun startDiscovery(scannerConfig: BluetoothScannerConfig)
    fun stopDiscovery()
    fun setScanDelegate(listener: BluetoothScanDelegate)
    fun setConnectDelegate(listener: BluetoothConnectDelegate)
    fun setCharacteristicChangeDelegate(listener: BluetoothCharacteristicChangeDelegate)
    fun connectDevice(device: BluetoothDevice)
    fun disconnectDevice(device: BluetoothDevice)
    fun writeToDevice(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, payload: ByteArray)
}