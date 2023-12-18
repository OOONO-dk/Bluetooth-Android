package com.example.bluetoothframework.domain

import android.annotation.SuppressLint
import android.bluetooth.le.ScanResult

@SuppressLint("MissingPermission")
fun ScanResult.toBluetoothDeviceDomain(): BluetoothDeviceDomain {
    return BluetoothDeviceDomain(
        rssi = rssi,
        name = device.name,
        address = device.address,
        discoverTimestamp = System.currentTimeMillis()
    )
}