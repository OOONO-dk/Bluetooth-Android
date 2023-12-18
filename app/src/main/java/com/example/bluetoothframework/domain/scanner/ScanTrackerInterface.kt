package com.example.bluetoothframework.domain.scanner

interface ScanTrackerInterface {
    fun isScanningAllowed(): Boolean
    fun incrementScanCount()
}