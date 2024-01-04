package com.example.bluetoothframework.control.delegates

import com.example.bluetoothframework.model.data.BluetoothDeviceInfo
import com.example.bluetoothframework.model.enums.ConnectionState

interface BluetoothDeviceDelegate {
    fun onConnectionStateUpdate(device: BluetoothDeviceInfo, newState: ConnectionState)
    fun onDeviceDiscovered(device: BluetoothDeviceInfo)
    fun onDeviceStoppedAdvertising(device: BluetoothDeviceInfo)
    fun onScanFailed()
    fun onDeviceDisconnected(device: BluetoothDeviceInfo)
    fun onConnectionFail(device: BluetoothDeviceInfo)
    fun onCharacteristicChanged(byteArray: ByteArray, device: BluetoothDeviceInfo, characteristicUuid: String)
}