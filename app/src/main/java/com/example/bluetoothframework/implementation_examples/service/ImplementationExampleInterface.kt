package com.example.bluetoothframework.implementation_examples.service

import com.example.bluetoothframework.control.delegates.BluetoothDeviceDelegate
import com.example.bluetoothframework.model.data.BluetoothConnectData
import com.example.bluetoothframework.model.data.BluetoothDeviceInfo
import com.example.bluetoothframework.model.data.BluetoothWriteData
import kotlinx.coroutines.flow.StateFlow

interface ImplementationExampleInterface:
    BluetoothDeviceDelegate {
    val devices: StateFlow<List<BluetoothDeviceInfo>>
    fun startDiscovery()
    fun stopDiscovery()
    fun connectDevices(devices: List<BluetoothConnectData>)
    fun disconnectDevices(devices: List<BluetoothDeviceInfo>)
    fun writeToDevices(devices: List<BluetoothWriteData>)
}