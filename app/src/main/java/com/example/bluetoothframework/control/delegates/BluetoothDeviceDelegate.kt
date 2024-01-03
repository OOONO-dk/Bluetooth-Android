package com.example.bluetoothframework.control.delegates

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import com.example.bluetoothframework.model.enums.ConnectionState

interface BluetoothDeviceDelegate {
    fun onConnectionStateUpdate(gatt: BluetoothGatt, newState: ConnectionState)
    fun onDeviceDiscovered(newDevice: BluetoothDevice)
    fun onDeviceRemoved(address: String)
    fun onScanFailed()
    fun onDeviceConnected(gatt: BluetoothGatt)
    fun onDeviceDisconnected(gatt: BluetoothGatt)
    fun onConnectionFail(gatt: BluetoothGatt)
    fun onCharacteristicChanged(byteArray: ByteArray, gatt: BluetoothGatt, characteristicUuid: String)
}