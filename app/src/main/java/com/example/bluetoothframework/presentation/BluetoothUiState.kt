package com.example.bluetoothframework.presentation

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt

data class BluetoothUiState(
    val scannedDevices: List<BluetoothDevice> = emptyList(),
    val connectedDevices: List<BluetoothGatt> = emptyList()
)
