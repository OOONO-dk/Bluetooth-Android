package com.example.bluetoothframework.control.controller

import com.example.bluetoothframework.connection.delegates.BluetoothConnectForwarderDelegate
import com.example.bluetoothframework.control.delegates.BluetoothDeviceDelegate
import com.example.bluetoothframework.model.data.BluetoothConnectData
import com.example.bluetoothframework.model.data.BluetoothDeviceInfo
import com.example.bluetoothframework.model.data.BluetoothScannerConfig
import com.example.bluetoothframework.model.data.BluetoothWriteData
import com.example.bluetoothframework.scanning.delegates.BluetoothScanForwarderDelegate

interface BluetoothControllerInterface:
    BluetoothScanForwarderDelegate,
    BluetoothConnectForwarderDelegate {
    fun startDiscovery(scannerConfig: BluetoothScannerConfig)
    fun stopDiscovery()
    fun setBluetoothDeviceListener(listener: BluetoothDeviceDelegate)
    fun connectDevices(devices: List<BluetoothConnectData>)
    fun disconnectDevices(devices: List<BluetoothDeviceInfo>)
    fun writeToDevices(devices: List<BluetoothWriteData>)
}