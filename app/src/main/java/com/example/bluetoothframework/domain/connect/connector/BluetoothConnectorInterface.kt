package com.example.bluetoothframework.domain.connect.connector

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import com.example.bluetoothframework.domain.connect.BluetoothCharacteristicChangeCallback
import com.example.bluetoothframework.domain.connect.BluetoothConnectCallback

interface BluetoothConnectorInterface {
    fun connectDevice(device: BluetoothDevice)
    fun disconnectDevice(device: BluetoothDevice)
    fun setConnectionCallback(listener: BluetoothConnectCallback)
    fun setCharacteristicChangeCallback(listener: BluetoothCharacteristicChangeCallback)
    fun writeToDevice(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, payload: ByteArray)
}