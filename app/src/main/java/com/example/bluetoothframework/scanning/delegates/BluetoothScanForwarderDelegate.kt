package com.example.bluetoothframework.scanning.delegates

import android.bluetooth.le.ScanResult

interface BluetoothScanForwarderDelegate {
    fun onDeviceDiscovered(scanResult: ScanResult)
    fun onScanFailed()
}