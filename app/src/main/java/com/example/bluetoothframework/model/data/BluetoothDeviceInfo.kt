package com.example.bluetoothframework.model.data

import android.bluetooth.BluetoothDevice
import com.example.bluetoothframework.model.enums.ConnectionState

data class BluetoothDeviceInfo(
    val device: BluetoothDevice,
    var connectionState: ConnectionState,
    var lastSeen: Long,
    var rssi: Int,
    val services: List<BluetoothService> = emptyList()
)
