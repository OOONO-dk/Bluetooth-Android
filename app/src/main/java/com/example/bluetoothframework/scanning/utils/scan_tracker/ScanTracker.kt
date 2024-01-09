package com.example.bluetoothframework.scanning.utils.scan_tracker

interface ScanTracker {
    fun isScanningAllowed(): Boolean
    fun incrementScanCount()
}