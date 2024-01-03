package com.example.bluetoothframework.connection.connector

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import com.example.bluetoothframework.connection.delegates.BluetoothCharacteristicChangeDelegate
import com.example.bluetoothframework.connection.delegates.BluetoothConnectDelegate

interface BluetoothConnectorInterface {
    fun connectDevice(device: BluetoothDevice)
    fun disconnectDevice(device: BluetoothDevice)
    fun setConnectionDelegate(listener: BluetoothConnectDelegate)
    fun setCharacteristicChangeDelegate(listener: BluetoothCharacteristicChangeDelegate)
    fun writeToDevice(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, payload: ByteArray)
}