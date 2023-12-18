package com.example.bluetoothframework.domain.scan.tracker

interface ScanTrackerInterface {
    fun isScanningAllowed(): Boolean
    fun incrementScanCount()
}