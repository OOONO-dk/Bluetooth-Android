package com.example.bluetoothframework.data

import android.annotation.SuppressLint
import android.bluetooth.le.ScanResult
import com.example.bluetoothframework.domain.BluetoothDeviceDomain

@SuppressLint("MissingPermission")
fun ScanResult.toBluetoothDeviceDomain(): BluetoothDeviceDomain {
    return BluetoothDeviceDomain(
        rssi = rssi,
        name = device.name,
        address = device.address,
        discoverTimestamp = System.currentTimeMillis()
    )
}