package com.example.bluetoothframework.domain.scan

import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanSettings

data class BluetoothScannerConfig(
    val scanSettings: ScanSettings,
    val scanFilter: List<ScanFilter>,
    val advertisingCheckIntervalMillis: Long,
    val advertisingExpirationIntervalMillis: Long
)
