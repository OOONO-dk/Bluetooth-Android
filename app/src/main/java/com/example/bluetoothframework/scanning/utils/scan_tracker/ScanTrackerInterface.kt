package com.example.bluetoothframework.scanning.utils.scan_tracker

interface ScanTrackerInterface {
    fun isScanningAllowed(): Boolean
    fun incrementScanCount()
}