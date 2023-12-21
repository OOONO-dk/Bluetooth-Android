package com.example.bluetoothframework.domain.controller

import android.bluetooth.BluetoothDevice
import com.example.bluetoothframework.domain.connect.BluetoothConnectCallback
import com.example.bluetoothframework.domain.scan.BluetoothScanCallback
import com.example.bluetoothframework.domain.scan.BluetoothScannerConfig

interface BluetoothControllerInterface {
    fun startDiscovery(scannerConfig: BluetoothScannerConfig)
    fun stopDiscovery()
    fun setScanCallback(listener: BluetoothScanCallback)
    fun setConnectCallback(listener: BluetoothConnectCallback)
    fun connectDevice(device: BluetoothDevice)
}