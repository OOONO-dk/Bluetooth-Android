package com.example.bluetoothframework.connection.connector

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import com.example.bluetoothframework.connection.delegates.BluetoothConnectForwarderDelegate

interface BluetoothConnector {
    fun connectDevice(device: BluetoothDevice)
    fun disconnectDevice(gatt: BluetoothGatt)
    fun setConnectionDelegate(listener: BluetoothConnectForwarderDelegate)
    fun getAllCharacteristics(gatt: BluetoothGatt, serviceUuid: String): List<BluetoothGattCharacteristic>
    fun writeToDevice(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, payload: ByteArray)
    fun discoverServices(gatt: BluetoothGatt)
    fun setServiceNotifiers(gatt: BluetoothGatt)
    suspend fun enableNotifications(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic)
    fun getCharacteristicFromUuid(gatt: BluetoothGatt, serviceUuid: String, characteristicUuid: String): BluetoothGattCharacteristic?
}