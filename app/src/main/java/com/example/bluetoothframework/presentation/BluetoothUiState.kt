package com.example.bluetoothframework.presentation

import com.example.bluetoothframework.domain.BluetoothDeviceDomain

data class BluetoothUiState(
    val scannedDevices: List<BluetoothDeviceDomain> = emptyList()
)
